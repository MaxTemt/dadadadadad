package com.airport.client.ui.table;

import com.airport.common.dto.BackupInfo;
import com.airport.common.util.Formats;

public class BackupTableModel extends AbstractEntityTableModel<BackupInfo> {

    private static final long serialVersionUID = 1L;

    public BackupTableModel() {
        super("Файл", "Размер", "Создан");
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        BackupInfo backup = getRow(rowIndex);
        return switch (columnIndex) {
            case 0 -> backup.getFileName();
            case 1 -> size(backup.getSizeBytes());
            case 2 -> Formats.dateTime(backup.getCreatedAt());
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
