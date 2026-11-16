package com.airport.client.ui.table;

import com.airport.common.model.Booking;
import com.airport.common.util.Formats;

public class BookingTableModel extends AbstractEntityTableModel<Booking> {

    private static final long serialVersionUID = 1L;

    public BookingTableModel() {
        super("Рейс", "Направление", "Пассажир", "Место", "Класс", "Стоимость",
                "Статус", "Оформлено");
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Booking booking = getRow(rowIndex);
        return switch (columnIndex) {
            case 0 -> booking.getFlightNumber();
            case 1 -> booking.getDestinationCity();
            case 2 -> booking.getPassengerName();
            case 3 -> booking.getSeatNumber();
            case 4 -> booking.getTicketClass().getTitle();
            case 5 -> Formats.money(booking.getPrice());
            case 6 -> booking.getStatus().getTitle();
            case 7 -> Formats.dateTime(booking.getBookingTime());
            default -> "";
        };
    }
}
