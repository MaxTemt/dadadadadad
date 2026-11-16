package com.airport.client.util;

import com.airport.common.dto.ReportResult;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Вспомогательные методы пользовательского интерфейса.
 */
public final class UiUtils {

    private UiUtils() {
    }

    public static void error(Component parent, Throwable throwable) {
        error(parent, messageOf(throwable));
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Сообщение", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message, String title) {
        return JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static String prompt(Component parent, String message, String title) {
        return JOptionPane.showInputDialog(parent, message, title, JOptionPane.QUESTION_MESSAGE);
    }

    public static <T> void runAsync(Component parent, Callable<T> task, Consumer<T> onSuccess) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    error(parent, "Операция прервана");
                } catch (ExecutionException exception) {
                    error(parent, exception.getCause() == null ? exception : exception.getCause());
                }
            }
        }.execute();
    }

    public static void saveTextFile(Component parent, ReportResult report) {
        saveTextFile(parent, report.getFileName(), report.getContent());
    }

    public static boolean saveTextFile(Component parent, String suggestedName, String content) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить отчёт");
        chooser.setSelectedFile(new File(suggestedName == null ? "report.txt" : suggestedName));
        chooser.setFileFilter(new FileNameExtensionFilter("Текстовые файлы (*.txt)", "txt"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".txt")) {
            file = new File(file.getParentFile(), file.getName() + ".txt");
        }
        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            info(parent, "Отчёт сохранён:\n" + file.getAbsolutePath());
            return true;
        } catch (Exception exception) {
            error(parent, "Не удалось сохранить файл: " + exception.getMessage());
            return false;
        }
    }

    public static void center(Component component) {
        if (component instanceof java.awt.Window window) {
            window.setLocationRelativeTo(null);
        } else {
            SwingUtilities.getWindowAncestor(component);
        }
    }

    private static String messageOf(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && (current.getMessage() == null
                || current.getMessage().isBlank())) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? throwable.toString() : message;
    }
}
