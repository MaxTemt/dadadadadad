package com.airport.client.ui.chart;

import com.airport.client.util.Theme;
import com.airport.common.dto.ChartData;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

/**
 * Базовый класс диаграммы. Реализует шаблонный метод отрисовки:
 * наследники определяют только способ изображения данных.
 */
public abstract class ChartPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    protected static final Color[] PALETTE = {
            new Color(0x2A9D8F), new Color(0x264653), new Color(0xE9C46A),
            new Color(0xF4A261), new Color(0xE76F51), new Color(0x6D597A),
            new Color(0x457B9D), new Color(0xA8DADC), new Color(0x8AB17D)
    };

    private ChartData data;
    private String subtitle = "Данные формируются на сервере";

    protected ChartPanel() {
        setOpaque(true);
        setBackground(Theme.SURFACE);
        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Theme.BORDER),
                javax.swing.BorderFactory.createEmptyBorder(16, 18, 16, 18)));
    }

    public void setData(ChartData data) {
        this.data = data;
        repaint();
    }

    public ChartData getData() {
        return data;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    protected final void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int top = 12;
            g2.setColor(Theme.PRIMARY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString(data == null ? "Диаграмма" : data.getTitle(), 4, top + 14);
            g2.setColor(Theme.MUTED);
            g2.setFont(Theme.FONT_SMALL);
            g2.drawString(subtitle, 4, top + 32);

            Rectangle2D area = new Rectangle2D.Double(4, top + 46,
                    Math.max(10, getWidth() - 8), Math.max(10, getHeight() - top - 54));
            if (data == null || data.isEmpty()) {
                drawEmpty(g2, area);
            } else {
                drawChart(g2, area);
            }
        } finally {
            g2.dispose();
        }
    }

    protected abstract void drawChart(Graphics2D g2, Rectangle2D area);

    protected void drawEmpty(Graphics2D g2, Rectangle2D area) {
        g2.setColor(Theme.MUTED);
        g2.setFont(Theme.FONT);
        String text = "Недостаточно данных для построения диаграммы";
        int width = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (float) (area.getCenterX() - width / 2.0),
                (float) area.getCenterY());
    }

    protected void drawGrid(Graphics2D g2, Rectangle2D plot, double max, int steps, String suffix) {
        g2.setFont(Theme.FONT_SMALL);
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= steps; i++) {
            double value = max * i / steps;
            double y = plot.getMaxY() - plot.getHeight() * i / steps;
            g2.setColor(i == 0 ? Theme.BORDER.darker() : new Color(0xEDF1F5));
            g2.draw(new java.awt.geom.Line2D.Double(plot.getX(), y, plot.getMaxX(), y));
            g2.setColor(Theme.MUTED);
            String label = formatShort(value) + suffix;
            int width = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, (float) (plot.getX() - width - 8), (float) (y + 4));
        }
    }

    protected String formatShort(double value) {
        if (value >= 1_000_000) {
            return String.format("%.1f млн", value / 1_000_000).replace('.', ',');
        }
        if (value >= 1_000) {
            return String.format("%.1f тыс.", value / 1_000).replace('.', ',');
        }
        if (Math.abs(value - Math.rint(value)) < 0.01) {
            return String.valueOf((long) value);
        }
        return String.format("%.1f", value).replace('.', ',');
    }

    protected String formatValue(double value, String suffix) {
        String number = formatShort(value);
        return suffix == null || suffix.isBlank() ? number : number + " " + suffix;
    }

    protected String fit(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }
}
