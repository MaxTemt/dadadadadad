package com.airport.client.util;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.LayoutManager;

/**
 * Единое визуальное оформление клиентского приложения.
 */
public final class Theme {

    public static final Color PRIMARY = new Color(0x15395B);
    public static final Color PRIMARY_LIGHT = new Color(0x245C87);
    public static final Color ACCENT = new Color(0x2A9D8F);
    public static final Color BACKGROUND = new Color(0xF3F6F9);
    public static final Color SURFACE = Color.WHITE;
    public static final Color TEXT = new Color(0x1E2A36);
    public static final Color MUTED = new Color(0x637381);
    public static final Color BORDER = new Color(0xDCE3EA);
    public static final Color DANGER = new Color(0xB23A48);
    public static final Color SUCCESS = new Color(0x2E7D32);
    public static final Color WARNING = new Color(0xC77700);

    public static final Font FONT = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.PLAIN, 12);

    private Theme() {
    }

    public static void install() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // остаётся системный Look and Feel
        }
        UIManager.put("control", BACKGROUND);
        UIManager.put("info", SURFACE);
        UIManager.put("nimbusBase", PRIMARY);
        UIManager.put("nimbusBlueGrey", new Color(0xD9E2EA));
        UIManager.put("nimbusLightBackground", SURFACE);
        UIManager.put("text", TEXT);
        UIManager.put("Table.font", FONT);
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("Table.showGrid", false);
        UIManager.put("TableHeader.font", FONT_BOLD);
        UIManager.put("TableHeader.background", new Color(0xE7EDF3));
        UIManager.put("TableHeader.foreground", PRIMARY);
        UIManager.put("Button.font", FONT);
        UIManager.put("Label.font", FONT);
        UIManager.put("TextField.font", FONT);
        UIManager.put("ComboBox.font", FONT);
        UIManager.put("TabbedPane.font", FONT_BOLD);
        UIManager.put("OptionPane.messageFont", FONT);
        UIManager.put("OptionPane.buttonFont", FONT);
    }

    public static JButton primaryButton(String text) {
        return button(text, PRIMARY, Color.WHITE);
    }

    public static JButton secondaryButton(String text) {
        return button(text, new Color(0xE7EDF3), PRIMARY);
    }

    public static JButton dangerButton(String text) {
        return button(text, new Color(0xF7E3E5), DANGER);
    }

    private static JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker()),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        return button;
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(PRIMARY);
        return label;
    }

    public static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(MUTED);
        return label;
    }

    public static JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        return panel;
    }

    public static JPanel header(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(0, 2));
        panel.setOpaque(false);
        panel.add(title(title), BorderLayout.NORTH);
        panel.add(subtitle(subtitle), BorderLayout.SOUTH);
        return panel;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setSelectionBackground(new Color(0xD6E8F5));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 32));
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(new Color(0xE7EDF3));
        headerRenderer.setForeground(PRIMARY);
        headerRenderer.setFont(FONT_BOLD);
        table.getTableHeader().setDefaultRenderer(headerRenderer);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable source, Object value,
                                                                   boolean selected, boolean focused,
                                                                   int row, int column) {
                java.awt.Component component = super.getTableCellRendererComponent(source, value,
                        selected, focused, row, column);
                component.setFont(FONT);
                if (!selected) {
                    component.setBackground(row % 2 == 0 ? SURFACE : new Color(0xF8FAFC));
                    component.setForeground(TEXT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return component;
            }
        });
    }
}
