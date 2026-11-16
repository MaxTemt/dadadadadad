package com.airport.client.ui.dialog;

import com.airport.client.service.AirportClientService;
import com.airport.client.ui.table.AirlineTableModel;
import com.airport.client.util.Theme;
import com.airport.client.util.UiUtils;
import com.airport.common.model.Airline;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Справочник авиакомпаний с возможностью добавления, изменения и удаления.
 */
public class AirlinesDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final AirportClientService service;
    private final AirlineTableModel model = new AirlineTableModel();
    private final JTable table = new JTable(model);

    private AirlinesDialog(Window owner, AirportClientService service) {
        super(owner, "Авиакомпании", ModalityType.APPLICATION_MODAL);
        this.service = service;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUi();
        setSize(720, 460);
        setMinimumSize(new Dimension(600, 360));
        setLocationRelativeTo(owner);
        load();
    }

    public static void showDialog(Component parent, AirportClientService service) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        new AirlinesDialog(owner, service).setVisible(true);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        root.add(Theme.header("Авиакомпании", "Справочник перевозчиков, выполняющих рейсы"), BorderLayout.NORTH);

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
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton add = Theme.primaryButton("Добавить");
        JButton edit = Theme.secondaryButton("Изменить");
        JButton delete = Theme.dangerButton("Удалить");
        JButton refresh = Theme.secondaryButton("Обновить");
        JButton close = Theme.secondaryButton("Закрыть");
        add.addActionListener(event -> add());
        edit.addActionListener(event -> edit());
        delete.addActionListener(event -> delete());
        refresh.addActionListener(event -> load());
        close.addActionListener(event -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(add);
        buttons.add(edit);
        buttons.add(delete);
        buttons.add(refresh);
        buttons.add(close);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void load() {
        UiUtils.runAsync(this, service::listAirlines, model::setRows);
    }

    private void add() {
        AirlineEditDialog.showDialog(this, null).ifPresent(airline ->
                UiUtils.runAsync(this, () -> service.saveAirline(airline), saved -> {
                    load();
                    UiUtils.info(this, "Авиакомпания сохранена");
                }));
    }

    private void edit() {
        Airline airline = selected();
        if (airline == null) {
            UiUtils.info(this, "Выберите авиакомпанию в таблице");
            return;
        }
        AirlineEditDialog.showDialog(this, airline).ifPresent(edited ->
                UiUtils.runAsync(this, () -> service.saveAirline(edited), saved -> {
                    load();
                    UiUtils.info(this, "Авиакомпания сохранена");
                }));
    }

    private void delete() {
        Airline airline = selected();
        if (airline == null) {
            UiUtils.info(this, "Выберите авиакомпанию в таблице");
            return;
        }
        if (!UiUtils.confirm(this, "Удалить авиакомпанию «" + airline.getName() + "»?",
                "Удаление")) {
            return;
        }
        UiUtils.runAsync(this, () -> {
            service.deleteAirline(airline.getId());
            return null;
        }, ignored -> {
            load();
            UiUtils.info(this, "Авиакомпания удалена");
        });
    }

    private Airline selected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return model.getRow(table.convertRowIndexToModel(viewRow));
    }
}
