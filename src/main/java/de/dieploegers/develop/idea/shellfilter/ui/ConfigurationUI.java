package de.dieploegers.develop.idea.shellfilter.ui;

import com.intellij.ui.CollectionListModel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import de.dieploegers.develop.idea.shellfilter.beans.CommandBean;
import de.dieploegers.develop.idea.shellfilter.beans.ConfigurationBean;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.util.ResourceBundle;

public class ConfigurationUI {
    @NonNls
    private final ResourceBundle resourceBundle =
        ResourceBundle.getBundle("i18n.shellfilter");
    private JTextField shellCommand;
    private JPanel configurationPanel;
    private JPanel commandListPanel;
    private JPanel shellCommandPanel;
    private JPanel commandPanel;
    private final JBList<CommandBean> commandList;
    private final CollectionListModel<CommandBean> commandListModel;

    public ConfigurationUI() {
        this.commandListModel = new CollectionListModel<>();

        commandList = new JBList<>(commandListModel);
        final ToolbarDecorator decorator =
            getDecorator();

        commandListPanel.add(decorator
            .createPanel());

        shellCommandPanel.setBorder(IdeBorderFactory.createTitledBorder(
            resourceBundle.getString("configuration.title.shellcommand")));
        commandPanel.setBorder(IdeBorderFactory.createTitledBorder(
            resourceBundle.getString("configuration.border.commands")));
    }

    private ToolbarDecorator getDecorator() {
        final ToolbarDecorator decorator =
            ToolbarDecorator.createDecorator(commandList);

        decorator.setAddAction(anActionButton -> {
            final CommandSettingsDialog
                commandSettingsDialog =
                new CommandSettingsDialog();
            if (commandSettingsDialog.showAndGet()) {
                final CommandBean newCommand = new CommandBean();
                commandSettingsDialog.getData(newCommand);
                addNewCommand(newCommand);
            }
        });
        decorator.setEditAction(anActionButton -> {
            final CommandBean selectedCommandBean =
                commandList.getSelectedValue();
            final CommandSettingsDialog commandSettingsDialog =
                new CommandSettingsDialog(selectedCommandBean);
            if (commandSettingsDialog.showAndGet()) {
                final CommandBean updatedCommand = new CommandBean();
                commandSettingsDialog.getData(updatedCommand);
                updateCommand(selectedCommandBean, updatedCommand);
            }
        });

        decorator.disableDownAction();
        decorator.disableUpAction();

        return decorator;
    }

    public JPanel getConfigurationPanel() {
        return configurationPanel;
    }

    public void addNewCommand(final CommandBean commandBean) {
        this.commandListModel.add(commandBean);
    }

    public void setData(final ConfigurationBean data) {
        shellCommand.setText(data.getShellCommand());
        this.commandListModel.removeAll();
        this.commandListModel.add(data.getCommands());
    }

    public void getData(final ConfigurationBean data) {
        data.setShellCommand(shellCommand.getText());
        data.setCommands(commandListModel.toList());
    }

    public boolean isModified(final ConfigurationBean data) {
        if (shellCommand.getText() != null ?
            !shellCommand.getText().equals(data.getShellCommand()) :
            data.getShellCommand() != null) return true;
        if (!this.commandListModel.getItems().equals(data.getCommands())) {
            return true;
        }
        return false;
    }

    public void updateCommand(final CommandBean originalData,
                              final CommandBean currentSettings)
    {

        final int index = commandListModel.getElementIndex(originalData);
        commandListModel.remove(originalData);
        commandListModel.add(index, currentSettings);
    }
}
