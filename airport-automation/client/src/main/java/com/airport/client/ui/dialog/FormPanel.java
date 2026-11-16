package com.airport.client.ui.dialog;

import com.airport.client.util.Theme;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Панель формы: подпись слева, поле справа.
 */
public class FormPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final GridBagLayout layout = new GridBagLayout();
    private int row;

    public FormPanel() {
        setLayout(layout);
        setBackground(Theme.SURFACE);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 4, 8, 4));
    }

    public void addRow(String label, JComponent field) {
        JLabel caption = new JLabel(label);
        caption.setFont(Theme.FONT_BOLD);
        caption.setForeground(Theme.TEXT);

        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = row;
        left.anchor = GridBagConstraints.WEST;
        left.insets = new Insets(6, 4, 6, 14);
        add(caption, left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = row;
        right.weightx = 1;
        right.fill = GridBagConstraints.HORIZONTAL;
        right.anchor = GridBagConstraints.WEST;
        right.insets = new Insets(6, 0, 6, 4);
        add(field, right);
        row++;
    }

    public void addFullRow(JComponent component) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row++;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(4, 4, 4, 4);
        add(component, constraints);
    }
}
