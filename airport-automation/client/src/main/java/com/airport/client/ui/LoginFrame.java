package com.airport.client.ui;

import com.airport.client.config.ClientConfig;
import com.airport.client.service.AirportClientService;
import com.airport.client.ui.dialog.FormPanel;
import com.airport.client.util.Theme;
import com.airport.common.model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;

/**
 * Окно подключения к серверу и авторизации.
 */
public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final AirportClientService service;
    private final JTextField hostField = new JTextField();
    private final JTextField portField = new JTextField();
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton loginButton = Theme.primaryButton("Войти в систему");

    public LoginFrame(ClientConfig config, AirportClientService service) {
        super("Аэропорт — вход в систему");
        this.service = service;
        hostField.setText(config.getHost());
        portField.setText(String.valueOf(config.getPort()));
        usernameField.setText("admin");
        passwordField.setText("admin123");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        buildUi();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                service.disconnect();
                dispose();
                System.exit(0);
            }
        });
        setSize(880, 540);
        setMinimumSize(new Dimension(820, 500));
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.SURFACE);
        root.add(new BrandPanel(), BorderLayout.WEST);
        root.add(buildForm(), BorderLayout.CENTER);
        setContentPane(root);
        getRootPane().setDefaultButton(loginButton);
    }

    private JPanel buildForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.SURFACE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(40, 46, 36, 46));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = Theme.title("Подключение к серверу");
        JLabel subtitle = Theme.subtitle("Укажите адрес сервера и учётные данные сотрудника");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        wrapper.add(header, BorderLayout.NORTH);

        FormPanel form = new FormPanel();
        form.addRow("Сервер", hostField);
        form.addRow("Порт", portField);
        form.addRow("Логин", usernameField);
        form.addRow("Пароль", passwordField);
        wrapper.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginButton.addActionListener(event -> login());
        passwordField.addActionListener(event -> login());

        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel("Демо-доступ: admin / admin123 (администратор), "
                + "agent / agent123 (пользователь)");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        footer.add(statusLabel);
        footer.add(Box.createVerticalStrut(8));
        footer.add(loginButton);
        footer.add(Box.createVerticalStrut(10));
        footer.add(hint);
        wrapper.add(footer, BorderLayout.SOUTH);
        return wrapper;
    }

    private void login() {
        String host = hostField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
            if (port < 1 || port > 65_535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            showStatus("Некорректный номер порта");
            return;
        }
        if (host.isEmpty()) {
            showStatus("Укажите адрес сервера");
            return;
        }
        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Введите логин и пароль");
            return;
        }

        setBusy(true);
        showStatus("Подключение к " + host + ":" + port + "…");
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                service.connect(host, port);
                return service.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    MainFrame frame = new MainFrame(service, user);
                    frame.setVisible(true);
                    dispose();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    fail("Операция прервана");
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                    service.disconnect();
                    fail(cause.getMessage() == null || cause.getMessage().isBlank()
                            ? cause.toString() : cause.getMessage());
                }
            }
        }.execute();
    }

    private void fail(String message) {
        setBusy(false);
        showStatus(message);
    }

    private void setBusy(boolean busy) {
        hostField.setEnabled(!busy);
        portField.setEnabled(!busy);
        usernameField.setEnabled(!busy);
        passwordField.setEnabled(!busy);
        loginButton.setEnabled(!busy);
        loginButton.setText(busy ? "Подключение…" : "Войти в систему");
        setCursor(java.awt.Cursor.getPredefinedCursor(busy
                ? java.awt.Cursor.WAIT_CURSOR : java.awt.Cursor.DEFAULT_CURSOR));
    }

    private void showStatus(String message) {
        statusLabel.setText(message == null ? " " : message);
        statusLabel.setForeground(Theme.DANGER);
    }

    /**
     * Декоративная панель с названием системы.
     */
    private static final class BrandPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private BrandPanel() {
            setPreferredSize(new Dimension(330, 0));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(56, 40, 40, 40));
            setOpaque(false);

            JLabel name = new JLabel("AIRPORT");
            name.setFont(new Font("SansSerif", Font.BOLD, 40));
            name.setForeground(Color.WHITE);
            name.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel system = new JLabel("Система автоматизации");
            system.setFont(new Font("SansSerif", Font.PLAIN, 17));
            system.setForeground(new Color(0xC9D8E4));
            system.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel desc = new JLabel("<html>Управление рейсами, пассажирами<br>"
                    + "и бронированием билетов.<br><br>"
                    + "Аналитика, отчёты и резервное<br>копирование на сервере.</html>");
            desc.setFont(Theme.FONT);
            desc.setForeground(new Color(0xE6EEF5));
            desc.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel version = new JLabel("Курсовой проект · Java 17 · TCP/MySQL");
            version.setFont(Theme.FONT_SMALL);
            version.setForeground(new Color(0x8FB0C9));
            version.setAlignmentX(Component.LEFT_ALIGNMENT);

            add(Box.createVerticalGlue());
            add(name);
            add(Box.createVerticalStrut(6));
            add(system);
            add(Box.createVerticalStrut(26));
            add(desc);
            add(Box.createVerticalGlue());
            add(version);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(0x102C47),
                        0, getHeight(), new Color(0x245C87)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            } finally {
                g2.dispose();
            }
        }
    }
}
