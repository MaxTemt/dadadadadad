package com.airport.client.ui.dialog;

import com.airport.client.service.AirportClientService;
import com.airport.client.ui.table.ReportTableModel;
import com.airport.client.util.Theme;
import com.airport.client.util.UiUtils;
import com.airport.common.dto.ReportInfo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
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
 * Список текстовых отчётов, сохранённых на сервере, с возможностью просмотра.
 */
public class ReportsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final AirportClientService service;
    private final ReportTableModel model = new ReportTableModel();
    private final JTable table = new JTable(model);
    private final JLabel counter = new JLabel();

    private ReportsDialog(Window owner, AirportClientService service) {
        super(owner, "Отчёты на сервере", ModalityType.APPLICATION_MODAL);
        this.service = service;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUi();
        setSize(760, 480);
        setMinimumSize(new Dimension(600, 360));
        setLocationRelativeTo(owner);
        load();
    }

    public static void showDialog(Component parent, AirportClientService service) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        new ReportsDialog(owner, service).setVisible(true);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        root.add(Theme.header("Отчёты на сервере",
                "Текстовые отчёты, сформированные в каталоге reports"), BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    open();
                }
            }
        });
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton open = Theme.primaryButton("Открыть");
        JButton refresh = Theme.secondaryButton("Обновить");
        JButton close = Theme.secondaryButton("Закрыть");
        open.addActionListener(event -> open());
        refresh.addActionListener(event -> load());
        close.addActionListener(event -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(open);
        buttons.add(refresh);
        buttons.add(close);

        counter.setFont(Theme.FONT_SMALL);
        counter.setForeground(Theme.MUTED);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(counter, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void load() {
        UiUtils.runAsync(this, service::listReports, reports -> {
            model.setRows(reports);
            counter.setText("Всего отчётов: " + reports.size());
        });
    }

    private void open() {
        ReportInfo report = selected();
        if (report == null) {
            UiUtils.info(this, "Выберите отчёт в таблице");
            return;
        }
        UiUtils.runAsync(this, () -> service.readReport(report.getFileName()),
                result -> ReportViewerDialog.showDialog(this, result));
    }

    private ReportInfo selected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return model.getRow(table.convertRowIndexToModel(viewRow));
    }
}
