package com.airport.client.ui;

import com.airport.client.service.AirportClientService;
import com.airport.client.ui.dialog.ReportsDialog;
import com.airport.client.ui.dialog.ReportViewerDialog;
import com.airport.client.util.Theme;
import com.airport.client.util.UiUtils;
import com.airport.common.model.User;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Главное окно приложения. Собирает экраны в зависимости от роли пользователя.
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final AirportClientService service;
    private final User user;
    private final List<Refreshable> refreshables = new ArrayList<>();
    private final JTabbedPane tabs = new JTabbedPane();

    public MainFrame(AirportClientService service, User user) {
        super("Система автоматизации работы аэропорта");
        this.service = service;
        this.user = user;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 680));
        setSize(1280, 820);
        buildUi();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                exit();
            }
        });
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        setJMenuBar(buildMenu());

        FlightsPanel flights = new FlightsPanel(service, user);
        PassengersPanel passengers = new PassengersPanel(service, user);
        BookingsPanel bookings = new BookingsPanel(service, user);
        AnalyticsPanel analytics = new AnalyticsPanel(service);
        refreshables.add(flights);
        refreshables.add(passengers);
        refreshables.add(bookings);
        refreshables.add(analytics);

        tabs.setFont(Theme.FONT_BOLD);
        tabs.addTab("Рейсы", flights);
        tabs.addTab("Пассажиры", passengers);
        tabs.addTab("Бронирования", bookings);
        tabs.addTab("Аналитика", analytics);
        if (user.isAdmin()) {
            AdminPanel admin = new AdminPanel(service, user, this::refreshAll);
            refreshables.add(admin);
            tabs.addTab("Администрирование", admin);
        }
        tabs.addChangeListener(event -> {
            if (tabs.getSelectedComponent() instanceof Refreshable refreshable) {
                refreshable.refreshData();
            }
        });
        add(tabs, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("Файл");
        file.setFont(Theme.FONT);
        JMenuItem refresh = new JMenuItem("Обновить данные");
        refresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        refresh.addActionListener(event -> refreshAll());
        JMenuItem exit = new JMenuItem("Выход");
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exit.addActionListener(event -> exit());
        file.add(refresh);
        file.addSeparator();
        file.add(exit);

        JMenu reports = new JMenu("Отчёты");
        reports.setFont(Theme.FONT);
        JMenuItem flightsReport = new JMenuItem("Отчёт по рейсам");
        flightsReport.addActionListener(event -> generateReport(true));
        JMenuItem bookingsReport = new JMenuItem("Отчёт по бронированиям");
        bookingsReport.addActionListener(event -> generateReport(false));
        JMenuItem serverReports = new JMenuItem("Отчёты на сервере");
        serverReports.addActionListener(event -> ReportsDialog.showDialog(this, service));
        reports.add(flightsReport);
        reports.add(bookingsReport);
        reports.addSeparator();
        reports.add(serverReports);

        JMenu help = new JMenu("Справка");
        help.setFont(Theme.FONT);
        JMenuItem about = new JMenuItem("О программе");
        about.addActionListener(event -> JOptionPane.showMessageDialog(this,
                "Система автоматизации работы аэропорта\n"
                        + "Клиент-серверная архитектура: Swing + TCP + MySQL\n\n"
                        + "Роли: администратор и пользователь\n"
                        + "Отчёты и резервные копии формируются на сервере.",
                "О программе", JOptionPane.INFORMATION_MESSAGE));
        help.add(about);

        bar.add(file);
        bar.add(reports);
        bar.add(help);
        return bar;
    }

    private JPanel buildStatusBar() {
        JPanel status = new JPanel(new BorderLayout(10, 0));
        status.setBackground(Theme.PRIMARY);
        status.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        JLabel who = new JLabel("Пользователь: " + user.getFullName() + " · " + user.getRole().getTitle());
        who.setFont(Theme.FONT_SMALL);
        who.setForeground(java.awt.Color.WHITE);
        JLabel where = new JLabel("Сервер: " + service.getRemote());
        where.setFont(Theme.FONT_SMALL);
        where.setForeground(new java.awt.Color(0xC9D8E4));
        status.add(who, BorderLayout.WEST);
        status.add(where, BorderLayout.EAST);
        return status;
    }

    private void refreshAll() {
        refreshables.forEach(Refreshable::refreshData);
    }

    private void generateReport(boolean flights) {
        UiUtils.runAsync(this, flights ? service::generateFlightsReport : service::generateBookingsReport,
                report -> ReportViewerDialog.showDialog(this, report));
    }

    private void exit() {
        if (!UiUtils.confirm(this, "Завершить работу и отключиться от сервера?", "Выход")) {
            return;
        }
        service.disconnect();
        dispose();
        System.exit(0);
    }
}
