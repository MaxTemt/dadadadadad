package com.airport.client.ui.table;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Базовый класс табличных моделей Swing.
 */
public abstract class AbstractEntityTableModel<T> extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final String[] columns;
    private List<T> rows = new ArrayList<>();

    protected AbstractEntityTableModel(String... columns) {
        this.columns = columns;
    }

    public void setRows(List<T> rows) {
        this.rows = rows == null ? new ArrayList<>() : new ArrayList<>(rows);
        fireTableDataChanged();
    }

    public List<T> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public T getRow(int index) {
        return rows.get(index);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
