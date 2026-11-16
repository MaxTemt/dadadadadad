package com.airport.client.ui.table;

import com.airport.common.model.User;
import com.airport.common.util.Formats;

public class UserTableModel extends AbstractEntityTableModel<User> {

    private static final long serialVersionUID = 1L;

    public UserTableModel() {
        super("Логин", "Фамилия", "Имя", "Роль", "E-mail", "Телефон", "Активен", "Создан");
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        User user = getRow(rowIndex);
        return switch (columnIndex) {
            case 0 -> user.getUsername();
            case 1 -> user.getLastName();
            case 2 -> user.getFirstName();
            case 3 -> user.getRole().getTitle();
            case 4 -> user.getEmail();
            case 5 -> user.getPhone();
            case 6 -> user.isActive() ? "Да" : "Нет";
            case 7 -> Formats.dateTime(user.getCreatedAt());
            default -> "";
        };
    }
}
