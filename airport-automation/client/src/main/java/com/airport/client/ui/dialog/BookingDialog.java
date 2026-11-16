package com.airport.client.ui.dialog;

import com.airport.client.util.Theme;
import com.airport.client.util.UiUtils;
import com.airport.common.model.Booking;
import com.airport.common.model.BookingStatus;
import com.airport.common.model.Flight;
import com.airport.common.model.Passenger;
import com.airport.common.model.TicketClass;
import com.airport.common.util.Formats;
import com.airport.common.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Диалог оформления и редактирования бронирования.
 */
public class BookingDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    public static final class Data {
        private final List<Flight> flights;
        private final List<Passenger> passengers;

        public Data(List<Flight> flights, List<Passenger> passengers) {
            this.flights = flights;
            this.passengers = passengers;
        }

        public List<Flight> getFlights() {
            return flights;
        }

        public List<Passenger> getPassengers() {
            return passengers;
        }
    }

    private final JComboBox<Flight> flightCombo = new JComboBox<>();
    private final JComboBox<Passenger> passengerCombo = new JComboBox<>();
    private final JTextField seatField = new JTextField();
    private final JComboBox<TicketClass> classCombo = new JComboBox<>(TicketClass.values());
    private final JComboBox<BookingStatus> statusCombo = new JComboBox<>(BookingStatus.values());
    private final JLabel priceLabel = new JLabel("—");

    private final Booking source;
    private Booking result;

    private BookingDialog(Window owner, Booking source, Data data) {
        super(owner, source == null ? "Новое бронирование" : "Редактирование бронирования",
                ModalityType.APPLICATION_MODAL);
        this.source = source;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUi(data);
        fill(source);
        pack();
        setMinimumSize(new Dimension(560, getHeight()));
        setLocationRelativeTo(owner);
    }

    public static Optional<Booking> showDialog(Component parent, Booking source, Data data) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        BookingDialog dialog = new BookingDialog(owner, source, data);
        dialog.setVisible(true);
        return Optional.ofNullable(dialog.result);
    }

    private void buildUi(Data data) {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        root.add(Theme.header(source == null ? "Новое бронирование" : "Бронирование №" + source.getId(),
                "Стоимость рассчитывается сервером по базовой цене рейса и классу обслуживания"),
                BorderLayout.NORTH);

        for (Flight flight : data.getFlights()) {
            flightCombo.addItem(flight);
        }
        for (Passenger passenger : data.getPassengers()) {
            passengerCombo.addItem(passenger);
        }
        flightCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                if (value instanceof Flight flight) {
                    setText(flight.getFlightNumber() + " · " + Formats.dateTime(flight.getDepartureTime())
                            + " · " + flight.getDestinationCity());
                }
                return this;
            }
        });
        passengerCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                if (value instanceof Passenger passenger) {
                    setText(passenger.getFullName() + " · " + passenger.getPassportNumber());
                }
                return this;
            }
        });

        FormPanel form = new FormPanel();
        form.addRow("Рейс *", flightCombo);
        form.addRow("Пассажир *", passengerCombo);
        form.addRow("Место (например, 12A) *", seatField);
        form.addRow("Класс обслуживания", classCombo);
        form.addRow("Статус", statusCombo);
        form.addRow("Стоимость, руб.", priceLabel);
        root.add(form, BorderLayout.CENTER);

        JButton cancel = Theme.secondaryButton("Отмена");
        JButton save = Theme.primaryButton("Сохранить");
        cancel.addActionListener(event -> dispose());
        save.addActionListener(event -> onSave());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(save);
        root.add(buttons, BorderLayout.SOUTH);

        flightCombo.addActionListener(event -> updatePrice());
        classCombo.addActionListener(event -> updatePrice());

        setContentPane(root);
        getRootPane().setDefaultButton(save);

        if (data.getFlights().isEmpty() || data.getPassengers().isEmpty()) {
            save.setEnabled(false);
        }
    }

    private void fill(Booking booking) {
        if (booking == null) {
            classCombo.setSelectedItem(TicketClass.ECONOMY);
            statusCombo.setSelectedItem(BookingStatus.CONFIRMED);
            updatePrice();
            return;
        }
        selectFlight(booking.getFlightId());
        selectPassenger(booking.getPassengerId());
        seatField.setText(booking.getSeatNumber());
        classCombo.setSelectedItem(booking.getTicketClass());
        statusCombo.setSelectedItem(booking.getStatus());
        updatePrice();
    }

    private void selectFlight(Long id) {
        for (int i = 0; i < flightCombo.getItemCount(); i++) {
            if (flightCombo.getItemAt(i).getId().equals(id)) {
                flightCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectPassenger(Long id) {
        for (int i = 0; i < passengerCombo.getItemCount(); i++) {
            if (passengerCombo.getItemAt(i).getId().equals(id)) {
                passengerCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void updatePrice() {
        Flight flight = (Flight) flightCombo.getSelectedItem();
        TicketClass ticketClass = (TicketClass) classCombo.getSelectedItem();
        if (flight == null || ticketClass == null) {
            priceLabel.setText("—");
            return;
        }
        BigDecimal price = flight.getBasePrice().multiply(ticketClass.getPriceMultiplier())
                .setScale(2, RoundingMode.HALF_UP);
        priceLabel.setText(Formats.money(price) + " руб.");
    }

    private void onSave() {
        try {
            Flight flight = (Flight) flightCombo.getSelectedItem();
            Passenger passenger = (Passenger) passengerCombo.getSelectedItem();
            if (flight == null) {
                throw new IllegalArgumentException("Выберите рейс");
            }
            if (passenger == null) {
                throw new IllegalArgumentException("Выберите пассажира");
            }
            Booking booking = source == null ? new Booking() : source;
            booking.setFlightId(flight.getId());
            booking.setPassengerId(passenger.getId());
            booking.setSeatNumber(ValidationUtil.requireSeatNumber(seatField.getText()));
            booking.setTicketClass((TicketClass) classCombo.getSelectedItem());
            booking.setStatus((BookingStatus) statusCombo.getSelectedItem());
            booking.setPrice(flight.getBasePrice()
                    .multiply(booking.getTicketClass().getPriceMultiplier())
                    .setScale(2, RoundingMode.HALF_UP));
            if (booking.getBookingTime() == null) {
                booking.setBookingTime(java.time.LocalDateTime.now());
            }
            this.result = booking;
            dispose();
        } catch (Exception exception) {
            UiUtils.error(this, exception.getMessage());
        }
    }
}
