package com.airport.client;

import com.airport.client.config.ClientConfig;
import com.airport.client.net.ServerConnection;
import com.airport.client.service.AirportClientService;
import com.airport.client.ui.LoginFrame;
import com.airport.client.util.Theme;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;

/**
 * Точка входа клиентского приложения.
 * Параметры подключения: client.properties, затем аргументы --host и --port.
 */
public final class ClientMain {

    private ClientMain() {
    }

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Airport Automation");
        final ClientConfig config;
        try {
            config = ClientConfig.load(args);
        } catch (IOException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(null,
                    "Не удалось загрузить настройки клиента: " + exception.getMessage(),
                    "Ошибка запуска", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            AirportClientService service = new AirportClientService(new ServerConnection());
            new LoginFrame(config, service).setVisible(true);
        });
    }
}
