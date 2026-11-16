package com.airport.client.ui.dialog;

import com.airport.client.util.Theme;
import com.airport.client.util.UiUtils;
import com.airport.common.dto.ReportResult;
import com.airport.common.util.Formats;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;

/**
 * Просмотр текстового отчёта и сохранение его в локальный файл.
 */
public class ReportViewerDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final ReportResult report;

    private ReportViewerDialog(Window owner, ReportResult report) {
        super(owner, "Текстовый отчёт", ModalityType.APPLICATION_MODAL);
        this.report = report;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUi();
        setSize(1060, 680);
        setMinimumSize(new Dimension(760, 480));
        setLocationRelativeTo(owner);
    }

    public static void showDialog(Component parent, ReportResult report) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        new ReportViewerDialog(owner, report).setVisible(true);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(Theme.header("Текстовый отчёт", "Файл сохранён на сервере: " + report.getFileName()
                + " · " + Formats.dateTime(report.getCreatedAt())), BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea(report.getContent());
        textArea.setEditable(false);
        textArea.setFont(new Font("Menlo", Font.PLAIN, 12));
        textArea.setLineWrap(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        root.add(scroll, BorderLayout.CENTER);

        JButton save = Theme.primaryButton("Сохранить в файл");
        JButton close = Theme.secondaryButton("Закрыть");
        save.addActionListener((ActionEvent event) -> UiUtils.saveTextFile(this, report));
        close.addActionListener(event -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(save);
        buttons.add(close);

        JLabel hint = new JLabel("Отчёт также доступен на сервере в каталоге reports");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.MUTED);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(hint, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);
    }
}
