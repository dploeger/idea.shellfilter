package de.dieploegers.develop.idea.shellfilter.ui;

import de.dieploegers.develop.idea.shellfilter.beans.CommandBean;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CommandSettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea command;
    private JTextField commandName;
    private JCheckBox trimLeadingNewlinesFromCheckBox;
    private final ConfigurationUI
        configurationUI;
    private final boolean isNew;
    private CommandBean originalData;

    public CommandSettingsDialog(final ConfigurationUI configurationUI) {
        this.configurationUI =
            configurationUI;
        isNew = true;
        createUI();
    }

    public CommandSettingsDialog(final ConfigurationUI configurationUI,
                                 final CommandBean commandBean)
    {
        isNew = false;
        originalData = commandBean;
        this.configurationUI =
            configurationUI;
        createUI();
        this.setData(commandBean);
    }

    private void createUI() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.setSize(400, 400);
    }

    private void onOK() {
        final CommandBean currentSettings = new CommandBean();
        getData(currentSettings);
        if (isNew) {
            this.configurationUI.addNewCommand(
                currentSettings
            );
        } else {
            this.configurationUI.updateCommand(
                originalData,
                currentSettings
            );
        }
        dispose();

    }

    private void onCancel() {
        dispose();
    }

    public void setData(final CommandBean data) {
        command.setText(data.getCommand());
        commandName.setText(data.getName());
        trimLeadingNewlinesFromCheckBox.setSelected(data.isRemoveTrailingNewline());
    }

    public void getData(final CommandBean data) {
        data.setCommand(command.getText());
        data.setName(commandName.getText());
        data.setRemoveTrailingNewline(trimLeadingNewlinesFromCheckBox.isSelected());
    }

}
