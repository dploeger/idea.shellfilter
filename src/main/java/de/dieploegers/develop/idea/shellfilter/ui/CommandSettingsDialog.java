package de.dieploegers.develop.idea.shellfilter.ui;

import com.intellij.openapi.ui.DialogWrapper;
import de.dieploegers.develop.idea.shellfilter.beans.CommandBean;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CommandSettingsDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextArea command;
    private JTextField commandName;
    private JCheckBox trimLeadingNewlinesFromCheckBox;
    private final boolean isNew;
    private CommandBean originalData;

    public CommandSettingsDialog() {
        super(false);
        isNew = true;
        init();
    }

    public CommandSettingsDialog(final CommandBean commandBean)
    {
        super(false);
        isNew = false;
        originalData = commandBean;
        init();
        this.setData(commandBean);
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

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
