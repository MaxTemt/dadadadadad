package com.airport.client.ui.table;

import com.airport.common.model.Airline;

public class AirlineTableModel extends AbstractEntityTableModel<Airline> {

    private static final long serialVersionUID = 1L;

    public AirlineTableModel() {
        super("Название", "Код IATA", "Страна", "Телефон");
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Airline airline = getRow(rowIndex);
        return switch (columnIndex) {
            case 0 -> airline.getName();
            case 1 -> airline.getIataCode();
            case 2 -> airline.getCountry();
            case 3 -> airline.getContactPhone();
            default -> "";
        };
    }
}
