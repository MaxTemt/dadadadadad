package com.airport.client.ui.table;

import com.airport.common.model.Passenger;
import com.airport.common.util.Formats;

public class PassengerTableModel extends AbstractEntityTableModel<Passenger> {

    private static final long serialVersionUID = 1L;

    public PassengerTableModel() {
        super("Фамилия", "Имя", "Паспорт", "Дата рождения", "Телефон", "E-mail", "Уровень");
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Passenger passenger = getRow(rowIndex);
        return switch (columnIndex) {
            case 0 -> passenger.getLastName();
            case 1 -> passenger.getFirstName();
            case 2 -> passenger.getPassportNumber();
            case 3 -> Formats.date(passenger.getDateOfBirth());
            case 4 -> passenger.getPhone();
            case 5 -> passenger.getEmail();
            case 6 -> level(passenger.getFrequentFlyerLevel());
            default -> "";
        };
    }

    private String level(String value) {
        if (value == null) {
            return "Нет";
        }
        return switch (value) {
            case "SILVER" -> "Серебряный";
            case "GOLD" -> "Золотой";
            case "PLATINUM" -> "Платиновый";
            default -> "Нет";
        };
    }
}
