package com.airport.client.ui.dialog;

import com.airport.client.util.Theme;
import com.airport.client.util.UiUtils;
import com.airport.common.model.Airline;
import com.airport.common.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Optional;

/**
 * Диалог редактирования авиакомпании.
 */
public class AirlineEditDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JTextField nameField = new JTextField();
    private final JTextField iataField = new JTextField();
    private final JTextField countryField = new JTextField();
    private final JTextField phoneField = new JTextField();

    private final Airline source;
    private Airline result;

    private AirlineEditDialog(Window owner, Airline source) {
        super(owner, source == null ? "Новая авиакомпания" : "Редактирование авиакомпании",
                ModalityType.APPLICATION_MODAL);
        this.source = source;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUi();
        fill(source);
        pack();
        setMinimumSize(new Dimension(480, getHeight()));
        setLocationRelativeTo(owner);
    }

    public static Optional<Airline> showDialog(Component parent, Airline source) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        AirlineEditDialog dialog = new AirlineEditDialog(owner, source);
        dialog.setVisible(true);
        return Optional.ofNullable(dialog.result);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        root.add(Theme.header(source == null ? "Новая авиакомпания" : "Авиакомпания",
                "Код IATA уникален и используется в расписании"), BorderLayout.NORTH);

        FormPanel form = new FormPanel();
        form.addRow("Название *", nameField);
        form.addRow("Код IATA *", iataField);
        form.addRow("Страна *", countryField);
        form.addRow("Телефон", phoneField);
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

    private void fill(Airline airline) {
        if (airline == null) {
            return;
        }
        nameField.setText(airline.getName());
        iataField.setText(airline.getIataCode());
        countryField.setText(airline.getCountry());
        phoneField.setText(airline.getContactPhone());
    }

    private void onSave() {
        try {
            Airline airline = source == null ? new Airline() : source;
            airline.setName(ValidationUtil.requireText(nameField.getText(), "Название", 100));
            airline.setIataCode(ValidationUtil.requireIataCode(iataField.getText()));
            airline.setCountry(ValidationUtil.requireText(countryField.getText(), "Страна", 60));
            airline.setContactPhone(ValidationUtil.optionalPhone(phoneField.getText(), "Телефон"));
            this.result = airline;
            dispose();
        } catch (Exception exception) {
            UiUtils.error(this, exception.getMessage());
        }
    }
}
