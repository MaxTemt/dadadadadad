package com.airport.client.ui;

import com.airport.client.service.AirportClientService;
import com.airport.client.ui.dialog.BookingDialog;
import com.airport.client.ui.dialog.ReportViewerDialog;
import com.airport.client.ui.table.BookingTableModel;
import com.airport.client.util.Theme;
import com.airport.client.util.UiUtils;
import com.airport.common.model.Booking;
import com.airport.common.model.BookingStatus;
import com.airport.common.model.Flight;
import com.airport.common.model.Passenger;
import com.airport.common.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Экран оформления бронирований, регистрации и отмены билетов.
 */
public class BookingsPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final AirportClientService service;
    private final User user;
    private final BookingTableModel model = new BookingTableModel();
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<String> statusFilter = new JComboBox<>();

    public BookingsPanel(AirportClientService service, User user) {
        this.service = service;
        this.user = user;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        buildUi();
        refreshData();
    }

    private void buildUi() {
        JPanel top = new JPanel(new BorderLayout(10, 8));
        top.setOpaque(false);
        top.add(Theme.header("Бронирования", "Билеты, регистрация на рейс и отмена броней"),
                BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        JButton add = Theme.primaryButton("Оформить");
        JButton edit = Theme.secondaryButton("Изменить");
        JButton checkIn = Theme.secondaryButton("Регистрация");
        JButton cancel = Theme.secondaryButton("Отменить бронь");
        JButton delete = Theme.dangerButton("Удалить");
        JButton report = Theme.secondaryButton("Отчёт");
        JButton refresh = Theme.secondaryButton("Обновить");
        delete.setVisible(user.isAdmin());
        add.addActionListener(event -> add());
        edit.addActionListener(event -> edit());
        checkIn.addActionListener(event -> checkIn());
        cancel.addActionListener(event -> cancel());
        delete.addActionListener(event -> delete());
        report.addActionListener(event -> report());
        refresh.addActionListener(event -> refreshData());
        actions.add(add);
        actions.add(edit);
        actions.add(checkIn);
        actions.add(cancel);
        actions.add(delete);
        actions.add(report);
        actions.add(refresh);
        top.add(actions, BorderLayout.EAST);

        statusFilter.addItem("Все статусы");
        for (BookingStatus status : BookingStatus.values()) {
            statusFilter.addItem(status.getTitle());
        }
        statusFilter.addActionListener(event -> refreshData());

        JButton find = Theme.primaryButton("Найти");
        JButton reset = Theme.secondaryButton("Сбросить");
        find.addActionListener(event -> refreshData());
        reset.addActionListener(event -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            refreshData();
        });
        searchField.addActionListener(event -> refreshData());

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filters.add(new JLabel("Поиск (рейс, пассажир, место):"));
        filters.add(searchField);
        filters.add(statusFilter);
        filters.add(find);
        filters.add(reset);

        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);
        header.add(top, BorderLayout.NORTH);
        header.add(filters, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    edit();
                }
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        BookingStatus status = statusFilter.getSelectedIndex() <= 0
                ? null : BookingStatus.values()[statusFilter.getSelectedIndex() - 1];
        String search = searchField.getText();
        UiUtils.runAsync(this, () -> service.listBookings(search, status), model::setRows);
    }

    private void add() {
        loadDialogData(data -> BookingDialog.showDialog(this, null, data).ifPresent(booking ->
                UiUtils.runAsync(this, () -> service.saveBooking(booking), saved -> {
                    refreshData();
                    UiUtils.info(this, "Бронирование оформлено");
                })));
    }

    private void edit() {
        Booking booking = selected();
        if (booking == null) {
            UiUtils.info(this, "Выберите бронирование в таблице");
            return;
        }
        loadDialogData(data -> BookingDialog.showDialog(this, booking, data).ifPresent(edited ->
                UiUtils.runAsync(this, () -> service.saveBooking(edited), saved -> {
                    refreshData();
                    UiUtils.info(this, "Изменения сохранены");
                })));
    }

    private void checkIn() {
        Booking booking = selected();
        if (booking == null) {
            UiUtils.info(this, "Выберите бронирование в таблице");
            return;
        }
        if (!UiUtils.confirm(this, "Зарегистрировать пассажира " + booking.getPassengerName()
                + " на рейс " + booking.getFlightNumber() + "?", "Регистрация на рейс")) {
            return;
        }
        UiUtils.runAsync(this, () -> service.checkIn(booking.getId()), result -> {
            refreshData();
            UiUtils.info(this, "Регистрация выполнена. Место: " + result.getSeatNumber());
        });
    }

    private void cancel() {
        Booking booking = selected();
        if (booking == null) {
            UiUtils.info(this, "Выберите бронирование в таблице");
            return;
        }
        if (!UiUtils.confirm(this, "Отменить бронирование №" + booking.getId() + "?",
                "Отмена бронирования")) {
            return;
        }
        UiUtils.runAsync(this, () -> service.cancelBooking(booking.getId()), result -> {
            refreshData();
            UiUtils.info(this, "Бронирование отменено");
        });
    }

    private void delete() {
        Booking booking = selected();
        if (booking == null) {
            UiUtils.info(this, "Выберите бронирование в таблице");
            return;
        }
        if (!UiUtils.confirm(this, "Удалить бронирование №" + booking.getId() + " без возможности "
                + "восстановления?", "Удаление бронирования")) {
            return;
        }
        UiUtils.runAsync(this, () -> {
            service.deleteBooking(booking.getId());
            return null;
        }, ignored -> {
            refreshData();
            UiUtils.info(this, "Бронирование удалено");
        });
    }

    private void report() {
        UiUtils.runAsync(this, service::generateBookingsReport,
                report -> ReportViewerDialog.showDialog(this, report));
    }

    private void loadDialogData(java.util.function.Consumer<BookingDialog.Data> action) {
        UiUtils.runAsync(this, () -> {
            List<Flight> flights = service.listAllFlights();
            List<Passenger> passengers = service.listPassengers("");
            return new BookingDialog.Data(flights, passengers);
        }, action::accept);
    }

    private Booking selected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return model.getRow(table.convertRowIndexToModel(viewRow));
    }
}
