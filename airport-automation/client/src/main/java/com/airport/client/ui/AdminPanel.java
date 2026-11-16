package com.airport.client.ui;

import com.airport.client.service.AirportClientService;
import com.airport.client.ui.dialog.UserDialog;
import com.airport.client.ui.table.BackupTableModel;
import com.airport.client.ui.table.UserTableModel;
import com.airport.client.util.Theme;
import com.airport.client.util.UiUtils;
import com.airport.common.dto.BackupInfo;
import com.airport.common.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Административная панель: учётные записи и резервное копирование.
 */
public class AdminPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final AirportClientService service;
    private final User user;
    private final Runnable onDataChanged;
    private final UserTableModel userModel = new UserTableModel();
    private final JTable userTable = new JTable(userModel);
    private final BackupTableModel backupModel = new BackupTableModel();
    private final JTable backupTable = new JTable(backupModel);

    public AdminPanel(AirportClientService service, User user, Runnable onDataChanged) {
        this.service = service;
        this.user = user;
        this.onDataChanged = onDataChanged == null ? () -> { } : onDataChanged;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        buildUi();
        refreshData();
    }

    private void buildUi() {
        JPanel top = new JPanel(new BorderLayout(10, 8));
        top.setOpaque(false);
        top.add(Theme.header("Администрирование", "Управление доступом и резервными копиями данных"),
                BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_BOLD);
        tabs.addTab("Пользователи", buildUsersTab());
        tabs.addTab("Резервные копии", buildBackupTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        Theme.styleTable(userTable);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setAutoCreateRowSorter(true);
        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    editUser();
                }
            }
        });
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JButton add = Theme.primaryButton("Добавить");
        JButton edit = Theme.secondaryButton("Изменить");
        JButton delete = Theme.dangerButton("Удалить");
        JButton refresh = Theme.secondaryButton("Обновить");
        add.addActionListener(event -> addUser());
        edit.addActionListener(event -> editUser());
        delete.addActionListener(event -> deleteUser());
        refresh.addActionListener(event -> refreshUsers());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(add);
        buttons.add(edit);
        buttons.add(delete);
        buttons.add(refresh);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildBackupTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        Theme.styleTable(backupTable);
        backupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        backupTable.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(backupTable), BorderLayout.CENTER);

        JButton create = Theme.primaryButton("Создать копию");
        JButton restore = Theme.secondaryButton("Восстановить");
        JButton refresh = Theme.secondaryButton("Обновить");
        create.addActionListener(event -> createBackup());
        restore.addActionListener(event -> restoreBackup());
        refresh.addActionListener(event -> refreshBackups());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(create);
        buttons.add(restore);
        buttons.add(refresh);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    @Override
    public void refreshData() {
        refreshUsers();
        refreshBackups();
    }

    private void refreshUsers() {
        UiUtils.runAsync(this, service::listUsers, userModel::setRows);
    }

    private void refreshBackups() {
        UiUtils.runAsync(this, service::listBackups, backupModel::setRows);
    }

    private void addUser() {
        UserDialog.showDialog(this, null).ifPresent(result ->
                UiUtils.runAsync(this, () -> service.saveUser(result.getUser(), result.getPassword()),
                        saved -> {
                            refreshUsers();
                            UiUtils.info(this, "Пользователь создан");
                        }));
    }

    private void editUser() {
        User selected = selectedUser();
        if (selected == null) {
            UiUtils.info(this, "Выберите пользователя в таблице");
            return;
        }
        UserDialog.showDialog(this, selected).ifPresent(result ->
                UiUtils.runAsync(this, () -> service.saveUser(result.getUser(), result.getPassword()),
                        saved -> {
                            refreshUsers();
                            UiUtils.info(this, "Данные пользователя сохранены");
                        }));
    }

    private void deleteUser() {
        User selected = selectedUser();
        if (selected == null) {
            UiUtils.info(this, "Выберите пользователя в таблице");
            return;
        }
        if (selected.getId().equals(user.getId())) {
            UiUtils.info(this, "Нельзя удалить собственную учётную запись");
            return;
        }
        if (!UiUtils.confirm(this, "Удалить пользователя «" + selected.getUsername() + "»?",
                "Удаление пользователя")) {
            return;
        }
        UiUtils.runAsync(this, () -> {
            service.deleteUser(selected.getId());
            return null;
        }, ignored -> {
            refreshUsers();
            UiUtils.info(this, "Пользователь удалён");
        });
    }

    private void createBackup() {
        UiUtils.runAsync(this, service::createBackup, info -> {
            refreshBackups();
            UiUtils.info(this, "Резервная копия создана: " + info.getFileName());
        });
    }

    private void restoreBackup() {
        BackupInfo selected = selectedBackup();
        if (selected == null) {
            UiUtils.info(this, "Выберите резервную копию в таблице");
            return;
        }
        if (!UiUtils.confirm(this, "Восстановить данные из файла " + selected.getFileName()
                + "?\nТекущие данные будут полностью заменены.",
                "Восстановление данных")) {
            return;
        }
        UiUtils.runAsync(this, () -> service.restoreBackup(selected.getFileName()), message -> {
            refreshBackups();
            onDataChanged.run();
            UiUtils.info(this, message);
        });
    }

    private User selectedUser() {
        int viewRow = userTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return userModel.getRow(userTable.convertRowIndexToModel(viewRow));
    }

    private BackupInfo selectedBackup() {
        int viewRow = backupTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return backupModel.getRow(backupTable.convertRowIndexToModel(viewRow));
    }
}
