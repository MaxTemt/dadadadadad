package com.airport.client.ui.chart;

import com.airport.client.util.Theme;
import com.airport.common.dto.ChartData;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Столбчатая диаграмма.
 */
public class BarChartPanel extends ChartPanel {

    private static final long serialVersionUID = 1L;

    @Override
    protected void drawChart(Graphics2D g2, Rectangle2D area) {
        ChartData chart = getData();
        List<String> labels = chart.getLabels();
        List<Double> values = chart.getValues();
        double max = chart.getMaxValue();

        double leftMargin = 64;
        double bottomMargin = Math.min(110, area.getHeight() * 0.34);
        Rectangle2D plot = new Rectangle2D.Double(area.getX() + leftMargin,
                area.getY() + 8,
                Math.max(20, area.getWidth() - leftMargin - 12),
                Math.max(20, area.getHeight() - bottomMargin - 8));

        drawGrid(g2, plot, max <= 0 ? 1 : max, 4, chart.getValueSuffix());

        int count = values.size();
        double slot = plot.getWidth() / Math.max(1, count);
        double barWidth = Math.min(64, slot * 0.62);
        g2.setFont(Theme.FONT_SMALL);

        for (int i = 0; i < count; i++) {
            double value = values.get(i);
            double ratio = max <= 0 ? 0 : value / max;
            double height = Math.max(value > 0 ? 2 : 0, plot.getHeight() * ratio);
            double x = plot.getX() + slot * i + (slot - barWidth) / 2;
            double y = plot.getMaxY() - height;
            Color color = PALETTE[i % PALETTE.length];
            g2.setPaint(new GradientPaint((float) x, (float) y, color.brighter(),
                    (float) x, (float) (y + height), color));
            g2.fill(new java.awt.geom.RoundRectangle2D.Double(x, y, barWidth, height,
                    Math.min(10, barWidth), Math.min(10, barWidth)));
            g2.setColor(color.darker());
            g2.setStroke(new BasicStroke(0.5f));
            g2.draw(new java.awt.geom.RoundRectangle2D.Double(x, y, barWidth, height,
                    Math.min(10, barWidth), Math.min(10, barWidth)));

            if (count <= 14) {
                String valueText = formatValue(value, null);
                int textWidth = g2.getFontMetrics().stringWidth(valueText);
                g2.setColor(Theme.TEXT);
                g2.setFont(Theme.FONT_BOLD);
                g2.drawString(valueText, (float) (x + barWidth / 2 - textWidth / 2.0),
                        (float) (y - 6));
                g2.setFont(Theme.FONT_SMALL);
            }

            AffineTransform old = g2.getTransform();
            String label = fit(labels.get(i), 13);
            double labelX = x + barWidth / 2;
            double labelY = plot.getMaxY() + 16;
            g2.rotate(Math.toRadians(-35), labelX, labelY);
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            g2.setColor(Theme.MUTED);
            g2.drawString(label, (float) (labelX - labelWidth * 0.20), (float) labelY);
            g2.setTransform(old);
        }

        g2.setColor(new Color(0xE1E8EE));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new java.awt.geom.Line2D.Double(plot.getX(), plot.getMaxY(),
                plot.getMaxX(), plot.getMaxY()));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
    }
}
