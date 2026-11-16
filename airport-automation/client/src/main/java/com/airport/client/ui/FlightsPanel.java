package com.airport.client.ui;

import com.airport.client.service.AirportClientService;
import com.airport.client.ui.dialog.AirlinesDialog;
import com.airport.client.ui.dialog.FlightDialog;
import com.airport.client.ui.dialog.ReportViewerDialog;
import com.airport.client.ui.table.FlightTableModel;
import com.airport.client.util.Theme;
import com.airport.client.util.UiUtils;
import com.airport.common.model.Flight;
import com.airport.common.model.FlightStatus;
import com.airport.common.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

/**
 * Экран расписания рейсов.
 */
public class FlightsPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final AirportClientService service;
    private final User user;
    private final FlightTableModel model = new FlightTableModel();
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<String> statusFilter = new JComboBox<>();

    public FlightsPanel(AirportClientService service, User user) {
        this.service = service;
        this.user = user;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        buildUi();
        refreshData();
    }

    private void buildUi() {
        JPanel top = new JPanel(new BorderLayout(10, 8));
        top.setOpaque(false);
        top.add(Theme.header("Рейсы", "Расписание вылетов, наличие мест и статусы рейсов"),
                BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        JButton add = Theme.primaryButton("Добавить");
        JButton edit = Theme.secondaryButton("Изменить");
        JButton delete = Theme.dangerButton("Удалить");
        JButton airlines = Theme.secondaryButton("Авиакомпании");
        JButton report = Theme.secondaryButton("Отчёт");
        JButton refresh = Theme.secondaryButton("Обновить");
        add.setVisible(user.isAdmin());
        edit.setVisible(user.isAdmin());
        delete.setVisible(user.isAdmin());
        airlines.setVisible(user.isAdmin());
        add.addActionListener(event -> add());
        edit.addActionListener(event -> edit());
        delete.addActionListener(event -> delete());
        airlines.addActionListener(event -> AirlinesDialog.showDialog(this, service));
        report.addActionListener(event -> report());
        refresh.addActionListener(event -> refreshData());
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        actions.add(airlines);
        actions.add(report);
        actions.add(refresh);
        top.add(actions, BorderLayout.EAST);

        statusFilter.addItem("Все статусы");
        for (FlightStatus status : FlightStatus.values()) {
            statusFilter.addItem(status.getTitle());
        }

        JButton find = Theme.primaryButton("Найти");
        JButton reset = Theme.secondaryButton("Сбросить");
        find.addActionListener(event -> refreshData());
        reset.addActionListener(event -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            refreshData();
        });
        searchField.addActionListener(event -> refreshData());

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filters.add(new javax.swing.JLabel("Поиск:"));
        filters.add(searchField);
        filters.add(statusFilter);
        filters.add(find);
        filters.add(reset);

        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);
        header.add(top, BorderLayout.NORTH);
        header.add(filters, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && user.isAdmin()) {
                    edit();
                }
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        FlightStatus status = statusFilter.getSelectedIndex() <= 0
                ? null : FlightStatus.values()[statusFilter.getSelectedIndex() - 1];
        String search = searchField.getText();
        UiUtils.runAsync(this, () -> service.listFlights(search, status, null), model::setRows);
    }

    private void add() {
        loadAirlines(airlines -> FlightDialog.showDialog(this, null, airlines).ifPresent(flight ->
                UiUtils.runAsync(this, () -> service.saveFlight(flight), saved -> {
                    refreshData();
                    UiUtils.info(this, "Рейс сохранён");
                })));
    }

    private void edit() {
        Flight flight = selected();
        if (flight == null) {
            UiUtils.info(this, "Выберите рейс в таблице");
            return;
        }
        loadAirlines(airlines -> FlightDialog.showDialog(this, flight, airlines).ifPresent(edited ->
                UiUtils.runAsync(this, () -> service.saveFlight(edited), saved -> {
                    refreshData();
                    UiUtils.info(this, "Изменения сохранены");
                })));
    }

    private void delete() {
        Flight flight = selected();
        if (flight == null) {
            UiUtils.info(this, "Выберите рейс в таблице");
            return;
        }
        if (!UiUtils.confirm(this, "Удалить рейс " + flight.getFlightNumber() + "?",
                "Удаление рейса")) {
            return;
        }
        UiUtils.runAsync(this, () -> {
            service.deleteFlight(flight.getId());
            return null;
        }, ignored -> {
            refreshData();
            UiUtils.info(this, "Рейс удалён");
        });
    }

    private void report() {
        UiUtils.runAsync(this, service::generateFlightsReport,
                report -> ReportViewerDialog.showDialog(this, report));
    }

    private void loadAirlines(java.util.function.Consumer<List<com.airport.common.model.Airline>> action) {
        UiUtils.runAsync(this, service::listAirlines, action::accept);
    }

    private Flight selected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return model.getRow(table.convertRowIndexToModel(viewRow));
    }
}
