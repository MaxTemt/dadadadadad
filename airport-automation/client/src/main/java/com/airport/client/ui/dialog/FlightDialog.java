package com.airport.client.ui.dialog;

import com.airport.client.util.Theme;
import com.airport.client.util.UiUtils;
import com.airport.common.model.Airline;
import com.airport.common.model.Flight;
import com.airport.common.model.FlightStatus;
import com.airport.common.util.Formats;
import com.airport.common.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Диалог добавления и редактирования рейса.
 */
public class FlightDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JTextField numberField = new JTextField();
    private final JComboBox<Airline> airlineCombo = new JComboBox<>();
    private final JTextField originField = new JTextField();
    private final JTextField destinationField = new JTextField();
    private final JTextField departureField = new JTextField();
    private final JTextField arrivalField = new JTextField();
    private final JTextField gateField = new JTextField();
    private final JTextField aircraftField = new JTextField();
    private final JSpinner seatsSpinner = new JSpinner(new SpinnerNumberModel(180, 1, 1000, 1));
    private final JComboBox<FlightStatus> statusCombo = new JComboBox<>(FlightStatus.values());
    private final JTextField priceField = new JTextField();

    private final Flight source;
    private Flight result;

    private FlightDialog(Window owner, Flight source, List<Airline> airlines) {
        super(owner, source == null ? "Новый рейс" : "Редактирование рейса",
                ModalityType.APPLICATION_MODAL);
        this.source = source;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUi(airlines);
        fill(source);
        pack();
        setMinimumSize(new Dimension(520, getHeight()));
        setLocationRelativeTo(owner);
    }

    public static Optional<Flight> showDialog(Component parent, Flight source, List<Airline> airlines) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        FlightDialog dialog = new FlightDialog(owner, source, airlines);
        dialog.setVisible(true);
        return Optional.ofNullable(dialog.result);
    }

    private void buildUi(List<Airline> airlines) {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        root.add(Theme.header(source == null ? "Новый рейс" : "Рейс " + source.getFlightNumber(),
                "Заполните расписание, характеристики и стоимость рейса"), BorderLayout.NORTH);

        for (Airline airline : airlines) {
            airlineCombo.addItem(airline);
        }

        FormPanel form = new FormPanel();
        form.addRow("Номер рейса *", numberField);
        form.addRow("Авиакомпания *", airlineCombo);
        form.addRow("Город вылета *", originField);
        form.addRow("Город прилёта *", destinationField);
        form.addRow("Вылет (дд.мм.гггг чч:мм) *", departureField);
        form.addRow("Прилёт (дд.мм.гггг чч:мм) *", arrivalField);
        form.addRow("Выход на посадку", gateField);
        form.addRow("Тип ВС", aircraftField);
        form.addRow("Количество мест *", seatsSpinner);
        form.addRow("Статус", statusCombo);
        form.addRow("Базовая цена, руб. *", priceField);
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

        setContentPane(root);
        getRootPane().setDefaultButton(save);
    }

    private void fill(Flight flight) {
        LocalDateTime now = LocalDateTime.now();
        if (flight == null) {
            departureField.setText(Formats.dateTime(now.plusDays(2).withMinute(0).withSecond(0)));
            arrivalField.setText(Formats.dateTime(now.plusDays(2).plusHours(2).withMinute(0).withSecond(0)));
            priceField.setText("5000.00");
            statusCombo.setSelectedItem(FlightStatus.SCHEDULED);
            return;
        }
        numberField.setText(flight.getFlightNumber());
        for (int i = 0; i < airlineCombo.getItemCount(); i++) {
            if (airlineCombo.getItemAt(i).getId().equals(flight.getAirlineId())) {
                airlineCombo.setSelectedIndex(i);
                break;
            }
        }
        originField.setText(flight.getOriginCity());
        destinationField.setText(flight.getDestinationCity());
        departureField.setText(Formats.dateTime(flight.getDepartureTime()));
        arrivalField.setText(Formats.dateTime(flight.getArrivalTime()));
        gateField.setText(flight.getGate());
        aircraftField.setText(flight.getAircraftType());
        seatsSpinner.setValue(flight.getTotalSeats());
        statusCombo.setSelectedItem(flight.getStatus());
        priceField.setText(Formats.money(flight.getBasePrice()));
    }

    private void onSave() {
        try {
            if (airlineCombo.getSelectedItem() == null) {
                throw new IllegalArgumentException("Выберите авиакомпанию");
            }
            Flight flight = source == null ? new Flight() : source;
            flight.setFlightNumber(ValidationUtil.requireFlightNumber(numberField.getText()));
            flight.setAirlineId(((Airline) airlineCombo.getSelectedItem()).getId());
            flight.setOriginCity(ValidationUtil.requireText(originField.getText(), "Город вылета", 80));
            flight.setDestinationCity(ValidationUtil.requireText(destinationField.getText(),
                    "Город прилёта", 80));
            LocalDateTime departure = Formats.parseDateTime(departureField.getText());
            LocalDateTime arrival = Formats.parseDateTime(arrivalField.getText());
            ValidationUtil.requireDateTimeRange(departure, arrival);
            flight.setDepartureTime(departure);
            flight.setArrivalTime(arrival);
            flight.setGate(ValidationUtil.optionalText(gateField.getText(), "Выход", 10));
            flight.setAircraftType(ValidationUtil.optionalText(aircraftField.getText(),
                    "Тип ВС", 50));
            flight.setTotalSeats((Integer) seatsSpinner.getValue());
            ValidationUtil.requireRange(flight.getTotalSeats(), 1, 1000, "Количество мест");
            flight.setStatus((FlightStatus) statusCombo.getSelectedItem());
            BigDecimal price;
            try {
                price = new BigDecimal(priceField.getText().trim().replace(',', '.'));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Некорректная базовая цена");
            }
            ValidationUtil.requirePositive(price, "Базовая цена");
            flight.setBasePrice(price);
            this.result = flight;
            dispose();
        } catch (Exception exception) {
            UiUtils.error(this, exception.getMessage());
        }
    }
}
