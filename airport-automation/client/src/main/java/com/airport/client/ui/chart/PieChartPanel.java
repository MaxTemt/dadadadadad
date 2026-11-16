package com.airport.client.ui.chart;

import com.airport.client.util.Theme;
import com.airport.common.dto.ChartData;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Круговая диаграмма с легендой.
 */
public class PieChartPanel extends ChartPanel {

    private static final long serialVersionUID = 1L;

    @Override
    protected void drawChart(Graphics2D g2, Rectangle2D area) {
        ChartData chart = getData();
        List<String> labels = chart.getLabels();
        List<Double> values = chart.getValues();
        double total = chart.getTotal();
        if (total <= 0) {
            drawEmpty(g2, area);
            return;
        }

        double legendWidth = Math.min(300, area.getWidth() * 0.42);
        double diameter = Math.min(area.getHeight(), area.getWidth() - legendWidth) - 10;
        diameter = Math.max(40, diameter);
        double pieX = area.getX() + (area.getWidth() - legendWidth - diameter) / 2;
        double pieY = area.getY() + (area.getHeight() - diameter) / 2;

        double start = 90;
        for (int i = 0; i < values.size(); i++) {
            double angle = -360.0 * values.get(i) / total;
            g2.setColor(PALETTE[i % PALETTE.length]);
            g2.fill(new Arc2D.Double(pieX, pieY, diameter, diameter, start, angle, Arc2D.PIE));
            g2.setColor(Theme.SURFACE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new Arc2D.Double(pieX, pieY, diameter, diameter, start, angle, Arc2D.PIE));
            if (Math.abs(angle) > 18) {
                double middle = Math.toRadians(start + angle / 2);
                double radius = diameter * 0.32;
                double tx = pieX + diameter / 2 + Math.cos(middle) * radius;
                double ty = pieY + diameter / 2 - Math.sin(middle) * radius;
                String percent = Math.round(values.get(i) * 100 / total) + "%";
                g2.setFont(Theme.FONT_BOLD);
                g2.setColor(Color.WHITE);
                int width = g2.getFontMetrics().stringWidth(percent);
                g2.drawString(percent, (float) (tx - width / 2.0), (float) (ty + 4));
            }
            start += angle;
        }

        double hole = diameter * 0.44;
        g2.setColor(Theme.SURFACE);
        g2.fill(new Ellipse2D.Double(pieX + (diameter - hole) / 2,
                pieY + (diameter - hole) / 2, hole, hole));
        g2.setColor(Theme.PRIMARY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        String totalText = formatShort(total);
        int totalWidth = g2.getFontMetrics().stringWidth(totalText);
        g2.drawString(totalText, (float) (pieX + diameter / 2 - totalWidth / 2.0),
                (float) (pieY + diameter / 2 + 2));
        g2.setColor(Theme.MUTED);
        g2.setFont(Theme.FONT_SMALL);
        String suffix = chart.getValueSuffix();
        int suffixWidth = g2.getFontMetrics().stringWidth(suffix);
        g2.drawString(suffix, (float) (pieX + diameter / 2 - suffixWidth / 2.0),
                (float) (pieY + diameter / 2 + 18));

        double legendX = pieX + diameter + 24;
        double legendY = area.getY() + 12;
        g2.setFont(Theme.FONT);
        for (int i = 0; i < labels.size(); i++) {
            if (legendY + 14 > area.getMaxY()) {
                break;
            }
            g2.setColor(PALETTE[i % PALETTE.length]);
            g2.fillRoundRect((int) legendX, (int) legendY - 10, 12, 12, 4, 4);
            g2.setColor(Theme.TEXT);
            double percent = values.get(i) * 100 / total;
            String text = fit(labels.get(i), 30) + " — " + formatValue(values.get(i),
                    chart.getValueSuffix()) + " (" + Math.round(percent) + "%)";
            g2.drawString(text, (float) (legendX + 20), (float) legendY);
            legendY += 24;
        }
    }
}
