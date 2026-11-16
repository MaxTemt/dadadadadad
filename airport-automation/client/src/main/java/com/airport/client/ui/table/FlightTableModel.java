package com.airport.client.ui.table;

import com.airport.common.model.Flight;
import com.airport.common.util.Formats;

public class FlightTableModel extends AbstractEntityTableModel<Flight> {

    private static final long serialVersionUID = 1L;

    public FlightTableModel() {
        super("Рейс", "Авиакомпания", "Откуда", "Куда", "Вылет", "Прилёт",
                "Выход", "Места", "Статус", "Базовая цена");
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Flight flight = getRow(rowIndex);
        return switch (columnIndex) {
            case 0 -> flight.getFlightNumber();
            case 1 -> flight.getAirlineName();
            case 2 -> flight.getOriginCity();
            case 3 -> flight.getDestinationCity();
            case 4 -> Formats.dateTime(flight.getDepartureTime());
            case 5 -> Formats.dateTime(flight.getArrivalTime());
            case 6 -> flight.getGate();
            case 7 -> flight.getBookedSeats() + " / " + flight.getTotalSeats();
            case 8 -> flight.getStatus().getTitle();
            case 9 -> Formats.money(flight.getBasePrice());
            default -> "";
        };
    }
}
