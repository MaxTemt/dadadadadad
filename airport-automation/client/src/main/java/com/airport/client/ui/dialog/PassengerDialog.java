package com.airport.client.ui.dialog;

import com.airport.client.util.Theme;
import com.airport.client.util.UiUtils;
import com.airport.common.model.Passenger;
import com.airport.common.util.Formats;
import com.airport.common.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Диалог добавления и редактирования пассажира.
 */
public class PassengerDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String[] LEVELS = {"NONE", "SILVER", "GOLD", "PLATINUM"};

    private final JTextField firstNameField = new JTextField();
    private final JTextField lastNameField = new JTextField();
    private final JTextField passportField = new JTextField();
    private final JTextField birthDateField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JComboBox<String> levelCombo = new JComboBox<>(new String[]{
            "Нет", "Серебряный", "Золотой", "Платиновый"});

    private final Passenger source;
    private Passenger result;

    private PassengerDialog(Window owner, Passenger source) {
        super(owner, source == null ? "Новый пассажир" : "Редактирование пассажира",
                ModalityType.APPLICATION_MODAL);
        this.source = source;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUi();
        fill(source);
        pack();
        setMinimumSize(new Dimension(520, getHeight()));
        setLocationRelativeTo(owner);
    }

    public static Optional<Passenger> showDialog(Component parent, Passenger source) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        PassengerDialog dialog = new PassengerDialog(owner, source);
        dialog.setVisible(true);
        return Optional.ofNullable(dialog.result);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        root.add(Theme.header(source == null ? "Новый пассажир" : "Карточка пассажира",
                "Персональные данные и документ, удостоверяющий личность"), BorderLayout.NORTH);

        FormPanel form = new FormPanel();
        form.addRow("Фамилия *", lastNameField);
        form.addRow("Имя *", firstNameField);
        form.addRow("Номер паспорта *", passportField);
        form.addRow("Дата рождения (дд.мм.гггг) *", birthDateField);
        form.addRow("Телефон", phoneField);
        form.addRow("E-mail", emailField);
        form.addRow("Уровень лояльности", levelCombo);
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

    private void fill(Passenger passenger) {
        if (passenger == null) {
            levelCombo.setSelectedIndex(0);
            return;
        }
        lastNameField.setText(passenger.getLastName());
        firstNameField.setText(passenger.getFirstName());
        passportField.setText(passenger.getPassportNumber());
        birthDateField.setText(Formats.date(passenger.getDateOfBirth()));
        phoneField.setText(passenger.getPhone());
        emailField.setText(passenger.getEmail());
        levelCombo.setSelectedIndex(levelIndex(passenger.getFrequentFlyerLevel()));
    }

    private void onSave() {
        try {
            Passenger passenger = source == null ? new Passenger() : source;
            passenger.setLastName(ValidationUtil.requireText(lastNameField.getText(), "Фамилия", 50));
            passenger.setFirstName(ValidationUtil.requireText(firstNameField.getText(), "Имя", 50));
            passenger.setPassportNumber(ValidationUtil.requirePassport(passportField.getText()));
            LocalDate birthDate = Formats.parseDate(birthDateField.getText());
            ValidationUtil.requireDateOfBirth(birthDate);
            passenger.setDateOfBirth(birthDate);
            passenger.setPhone(ValidationUtil.optionalPhone(phoneField.getText(), "Телефон"));
            passenger.setEmail(ValidationUtil.optionalEmail(emailField.getText(), "E-mail"));
            passenger.setFrequentFlyerLevel(LEVELS[levelCombo.getSelectedIndex()]);
            this.result = passenger;
            dispose();
        } catch (Exception exception) {
            UiUtils.error(this, exception.getMessage());
        }
    }

    private int levelIndex(String level) {
        for (int i = 0; i < LEVELS.length; i++) {
            if (LEVELS[i].equals(level)) {
                return i;
            }
        }
        return 0;
    }
}
