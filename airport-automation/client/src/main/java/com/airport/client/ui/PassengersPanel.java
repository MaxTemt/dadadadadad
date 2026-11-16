package com.airport.client.ui;

import com.airport.client.service.AirportClientService;
import com.airport.client.ui.dialog.PassengerDialog;
import com.airport.client.ui.table.PassengerTableModel;
import com.airport.client.util.Theme;
import com.airport.client.util.UiUtils;
import com.airport.common.model.Passenger;
import com.airport.common.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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

/**
 * Экран работы с карточками пассажиров.
 */
public class PassengersPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final AirportClientService service;
    private final User user;
    private final PassengerTableModel model = new PassengerTableModel();
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField(22);

    public PassengersPanel(AirportClientService service, User user) {
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
        top.add(Theme.header("Пассажиры", "Персональные данные, документы и уровень лояльности"),
                BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        JButton add = Theme.primaryButton("Добавить");
        JButton edit = Theme.secondaryButton("Изменить");
        JButton delete = Theme.dangerButton("Удалить");
        JButton refresh = Theme.secondaryButton("Обновить");
        delete.setVisible(user.isAdmin());
        add.addActionListener(event -> add());
        edit.addActionListener(event -> edit());
        delete.addActionListener(event -> delete());
        refresh.addActionListener(event -> refreshData());
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        actions.add(refresh);
        top.add(actions, BorderLayout.EAST);

        JButton find = Theme.primaryButton("Найти");
        JButton reset = Theme.secondaryButton("Сбросить");
        find.addActionListener(event -> refreshData());
        reset.addActionListener(event -> {
            searchField.setText("");
            refreshData();
        });
        searchField.addActionListener(event -> refreshData());

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filters.add(new JLabel("Поиск (ФИО, паспорт, телефон):"));
        filters.add(searchField);
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
        String search = searchField.getText();
        UiUtils.runAsync(this, () -> service.listPassengers(search), model::setRows);
    }

    private void add() {
        PassengerDialog.showDialog(this, null).ifPresent(passenger ->
                UiUtils.runAsync(this, () -> service.savePassenger(passenger), saved -> {
                    refreshData();
                    UiUtils.info(this, "Пассажир сохранён");
                }));
    }

    private void edit() {
        Passenger passenger = selected();
        if (passenger == null) {
            UiUtils.info(this, "Выберите пассажира в таблице");
            return;
        }
        PassengerDialog.showDialog(this, passenger).ifPresent(edited ->
                UiUtils.runAsync(this, () -> service.savePassenger(edited), saved -> {
                    refreshData();
                    UiUtils.info(this, "Изменения сохранены");
                }));
    }

    private void delete() {
        Passenger passenger = selected();
        if (passenger == null) {
            UiUtils.info(this, "Выберите пассажира в таблице");
            return;
        }
        if (!UiUtils.confirm(this, "Удалить пассажира " + passenger.getFullName() + "?",
                "Удаление пассажира")) {
            return;
        }
        UiUtils.runAsync(this, () -> {
            service.deletePassenger(passenger.getId());
            return null;
        }, ignored -> {
            refreshData();
            UiUtils.info(this, "Пассажир удалён");
        });
    }

    private Passenger selected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return model.getRow(table.convertRowIndexToModel(viewRow));
    }
}
