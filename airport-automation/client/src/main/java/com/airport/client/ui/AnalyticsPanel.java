package com.airport.client.ui;

import com.airport.client.service.AirportClientService;
import com.airport.client.ui.chart.BarChartPanel;
import com.airport.client.ui.chart.PieChartPanel;
import com.airport.client.util.Theme;
import com.airport.client.util.UiUtils;
import com.airport.common.dto.ChartData;
import com.airport.common.dto.DashboardStats;
import com.airport.common.util.Formats;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Панель аналитики: сводные показатели и диаграммы, рассчитанные на сервере.
 */
public class AnalyticsPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private static final class AnalyticsData {
        private final DashboardStats stats;
        private final ChartData occupancy;
        private final ChartData statuses;
        private final ChartData revenue;

        private AnalyticsData(DashboardStats stats, ChartData occupancy, ChartData statuses,
                              ChartData revenue) {
            this.stats = stats;
            this.occupancy = occupancy;
            this.statuses = statuses;
            this.revenue = revenue;
        }
    }

    private final AirportClientService service;
    private final JLabel flightsValue = valueLabel();
    private final JLabel todayValue = valueLabel();
    private final JLabel passengersValue = valueLabel();
    private final JLabel bookingsValue = valueLabel();
    private final JLabel revenueValue = valueLabel();
    private final JLabel occupancyValue = valueLabel();
    private final JLabel cancelledValue = valueLabel();
    private final BarChartPanel occupancyChart = new BarChartPanel();
    private final PieChartPanel statusChart = new PieChartPanel();
    private final BarChartPanel revenueChart = new BarChartPanel();

    public AnalyticsPanel(AirportClientService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        buildUi();
        refreshData();
    }

    private void buildUi() {
        JPanel top = new JPanel(new BorderLayout(10, 8));
        top.setOpaque(false);
        top.add(Theme.header("Аналитика", "Показатели работы аэропорта и структура продаж"),
                BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        JButton refresh = Theme.secondaryButton("Обновить");
        refresh.addActionListener(event -> refreshData());
        actions.add(refresh);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);

        JPanel cards = new JPanel(new GridLayout(0, 4, 12, 12));
        cards.setOpaque(false);
        cards.add(card("Всего рейсов", flightsValue));
        cards.add(card("Рейсов сегодня", todayValue));
        cards.add(card("Пассажиров", passengersValue));
        cards.add(card("Активных броней", bookingsValue));
        cards.add(card("Выручка", revenueValue));
        cards.add(card("Средняя загрузка", occupancyValue));
        cards.add(card("Отменённых рейсов", cancelledValue));
        content.add(cards, BorderLayout.NORTH);

        occupancyChart.setSubtitle("Занятость мест по ближайшим рейсам, %");
        statusChart.setSubtitle("Распределение бронирований по статусам");
        revenueChart.setSubtitle("Выручка по авиакомпаниям, руб.");

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_BOLD);
        tabs.addTab("Загрузка рейсов", occupancyChart);
        tabs.addTab("Статусы броней", statusChart);
        tabs.addTab("Выручка по авиакомпаниям", revenueChart);
        content.add(tabs, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel card(String title, JLabel value) {
        JPanel card = Theme.card(new BorderLayout(0, 4));
        JLabel caption = new JLabel(title);
        caption.setFont(Theme.FONT_SMALL);
        caption.setForeground(Theme.MUTED);
        card.add(caption, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(190, 82));
        return card;
    }

    private static JLabel valueLabel() {
        JLabel label = new JLabel("—");
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setForeground(Theme.PRIMARY);
        return label;
    }

    @Override
    public void refreshData() {
        UiUtils.runAsync(this, () -> new AnalyticsData(service.dashboard(), service.occupancyChart(),
                service.statusChart(), service.revenueChart()), this::apply);
    }

    private void apply(AnalyticsData data) {
        DashboardStats stats = data.stats;
        flightsValue.setText(String.valueOf(stats.getTotalFlights()));
        todayValue.setText(String.valueOf(stats.getTodayFlights()));
        passengersValue.setText(String.valueOf(stats.getTotalPassengers()));
        bookingsValue.setText(String.valueOf(stats.getActiveBookings()));
        revenueValue.setText(String.format(Formats.LOCALE, "%,.2f", stats.getRevenue()) + " ₽");
        occupancyValue.setText(String.format(Formats.LOCALE, "%.1f%%", stats.getAverageOccupancy()));
        cancelledValue.setText(String.valueOf(stats.getCancelledFlights()));
        cancelledValue.setForeground(stats.getCancelledFlights() > 0 ? Theme.DANGER : Theme.PRIMARY);
        revenueValue.setForeground(new Color(0x1B6B4A));

        occupancyChart.setData(data.occupancy);
        statusChart.setData(data.statuses);
        revenueChart.setData(data.revenue);
    }
}
