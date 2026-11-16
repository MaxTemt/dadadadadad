package com.airport.client.ui.table;

import com.airport.common.dto.ReportInfo;
import com.airport.common.util.Formats;

/**
 * Табличная модель списка текстовых отчётов на сервере.
 */
public class ReportTableModel extends AbstractEntityTableModel<ReportInfo> {

    private static final long serialVersionUID = 1L;

    public ReportTableModel() {
        super("Файл", "Размер", "Создан");
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ReportInfo report = getRow(rowIndex);
        return switch (columnIndex) {
            case 0 -> report.getFileName();
            case 1 -> size(report.getSizeBytes());
            case 2 -> Formats.dateTime(report.getCreatedAt());
            default -> "";
        };
    }

    private String size(long bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format("%.1f МБ", bytes / 1024.0 / 1024.0).replace('.', ',');
        }
        if (bytes >= 1024) {
            return String.format("%.1f КБ", bytes / 1024.0).replace('.', ',');
        }
        return bytes + " Б";
    }
}
