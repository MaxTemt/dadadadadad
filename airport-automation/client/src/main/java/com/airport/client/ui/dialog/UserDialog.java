package com.airport.client.ui.dialog;

import com.airport.client.util.Theme;
import com.airport.client.util.UiUtils;
import com.airport.common.model.Role;
import com.airport.common.model.User;
import com.airport.common.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Optional;

/**
 * Диалог создания и редактирования учётной записи.
 */
public class UserDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    public static final class Result {
        private final User user;
        private final String password;

        public Result(User user, String password) {
            this.user = user;
            this.password = password;
        }

        public User getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }
    }

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JTextField firstNameField = new JTextField();
    private final JTextField lastNameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
    private final JCheckBox activeCheck = new JCheckBox("Учётная запись активна", true);

    private final User source;
    private Result result;

    private UserDialog(Window owner, User source) {
        super(owner, source == null ? "Новый пользователь" : "Редактирование пользователя",
                ModalityType.APPLICATION_MODAL);
        this.source = source;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUi();
        fill(source);
        pack();
        setMinimumSize(new Dimension(540, getHeight()));
        setLocationRelativeTo(owner);
    }

    public static Optional<Result> showDialog(Component parent, User source) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        UserDialog dialog = new UserDialog(owner, source);
        dialog.setVisible(true);
        return Optional.ofNullable(dialog.result);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        root.add(Theme.header(source == null ? "Новый пользователь" : "Учётная запись",
                "Роль определяет доступные операции в системе"), BorderLayout.NORTH);

        FormPanel form = new FormPanel();
        form.addRow("Логин *", usernameField);
        form.addRow(source == null ? "Пароль *" : "Новый пароль", passwordField);
        form.addRow("Фамилия *", lastNameField);
        form.addRow("Имя *", firstNameField);
        form.addRow("E-mail", emailField);
        form.addRow("Телефон", phoneField);
        form.addRow("Роль", roleCombo);
        form.addRow("", activeCheck);
        if (source != null) {
            passwordField.setToolTipText("Оставьте пустым, чтобы сохранить текущий пароль");
        }
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

    private void fill(User user) {
        if (user == null) {
            roleCombo.setSelectedItem(Role.USER);
            return;
        }
        usernameField.setText(user.getUsername());
        lastNameField.setText(user.getLastName());
        firstNameField.setText(user.getFirstName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        roleCombo.setSelectedItem(user.getRole());
        activeCheck.setSelected(user.isActive());
    }

    private void onSave() {
        try {
            User user = source == null ? new User() : source;
            user.setUsername(ValidationUtil.requireUsername(usernameField.getText()));
            user.setLastName(ValidationUtil.requireText(lastNameField.getText(), "Фамилия", 50));
            user.setFirstName(ValidationUtil.requireText(firstNameField.getText(), "Имя", 50));
            user.setEmail(ValidationUtil.optionalEmail(emailField.getText(), "E-mail"));
            user.setPhone(ValidationUtil.optionalPhone(phoneField.getText(), "Телефон"));
            user.setRole((Role) roleCombo.getSelectedItem());
            user.setActive(activeCheck.isSelected());
            String password = new String(passwordField.getPassword());
            if (source == null && password.isBlank()) {
                throw new IllegalArgumentException("Для нового пользователя пароль обязателен");
            }
            if (!password.isBlank()) {
                ValidationUtil.requirePassword(password);
            }
            this.result = new Result(user, password);
            dispose();
        } catch (Exception exception) {
            UiUtils.error(this, exception.getMessage());
        }
    }
}
