package com.airport.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Данные для построения диаграммы на клиенте.
 */
public class ChartData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String title;
    private final String valueSuffix;
    private final List<String> labels;
    private final List<Double> values;

    public ChartData(String title, String valueSuffix) {
        this(title, valueSuffix, new ArrayList<>(), new ArrayList<>());
    }

    public ChartData(String title, String valueSuffix, List<String> labels, List<Double> values) {
        if (labels.size() != values.size()) {
            throw new IllegalArgumentException("Количество подписей и значений должно совпадать");
        }
        this.title = title;
        this.valueSuffix = valueSuffix;
        this.labels = new ArrayList<>(labels);
        this.values = new ArrayList<>(values);
    }

    public void add(String label, double value) {
        labels.add(label);
        values.add(value);
    }

    public String getTitle() {
        return title;
    }

    public String getValueSuffix() {
        return valueSuffix;
    }

    public List<String> getLabels() {
        return Collections.unmodifiableList(labels);
    }

    public List<Double> getValues() {
        return Collections.unmodifiableList(values);
    }

    public boolean isEmpty() {
        return labels.isEmpty();
    }

    public double getMaxValue() {
        return values.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    }

    public double getTotal() {
        return values.stream().mapToDouble(Double::doubleValue).sum();
    }
}
