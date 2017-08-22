package de.dieploegers.develop.idea.shellfilter;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import de.dieploegers.develop.idea.shellfilter.beans.CommandBean;
import de.dieploegers.develop.idea.shellfilter.beans.ConfigurationBean;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@State(name = "ShellFilter", storages = {
    @Storage("shellfilter.xml")
})
public class Settings implements ApplicationComponent,
    PersistentStateComponent<Element>
{
    @NonNls
    private String shellCommand;
    private List<CommandBean> commands;
    private CommandBean lastCustomCommand;
    private String lastSelectedCommand;

    public Settings() {
        this.shellCommand = "/bin/sh %s";
        this.commands = new ArrayList<>();
        this.lastCustomCommand = new CommandBean("Custom", "", true);
        this.lastSelectedCommand = null;
    }

    public static Settings getInstance() {
        return ApplicationManager.getApplication()
            .getComponent(Settings.class);
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "ShellFilter";
    }

    public String getShellCommand() {
        return shellCommand;
    }

    public void setShellCommand(final String shellCommand) {
        this.shellCommand = shellCommand;
    }

    public List<CommandBean> getCommands() {
        return commands;
    }

    public void setCommands(final List<CommandBean> commands) {
        this.commands = commands;
    }

    @Nullable
    @Override
    public Element getState() {
        @NonNls final Element element = new Element("ShellFilterSettings");
        element.setAttribute("shellCommand", shellCommand);
        if (lastSelectedCommand != null) {
            element.setAttribute("lastSelectedCommand", lastSelectedCommand);
        }
        for (final CommandBean commandBean : commands) {
            @NonNls final Element beanElement = new Element("Command");
            beanElement.setAttribute("name", commandBean.getName());
            if (commandBean.isRemoveTrailingNewline()) {
                beanElement.setAttribute("removeTrailingNewline", "true");
            } else {
                beanElement.setAttribute("removeTrailingNewline", "true");
            }
            beanElement.setText(commandBean.getCommand());
            element.addContent(beanElement);
        }
        @NonNls final Element
            lastCustomCommandElement =
            new Element("LastCustomCommand");
        if (lastCustomCommand.isRemoveTrailingNewline()) {
            lastCustomCommandElement.setAttribute("removeTrailingNewline",
                "true");
        } else {
            lastCustomCommandElement.setAttribute("removeTrailingNewline",
                "false");
        }
        lastCustomCommandElement.setText(lastCustomCommand.getCommand());
        element.addContent(lastCustomCommandElement);

        return element;
    }

    @Override
    public void loadState(final Element state) {
        this.shellCommand = state.getAttributeValue("shellCommand"); //NON-NLS
        if (state.getAttributeValue("lastSelectedCommand") != null) { //NON-NLS
            this.lastSelectedCommand =
                state.getAttributeValue("lastSelectedCommand"); //NON-NLS
        }
        this.commands = new ArrayList<>();

        for (final Element element : state.getChildren()) {
            if (element.getName().equals("Command")) { //NON-NLS
                final CommandBean commandBean = new CommandBean();
                commandBean.setName(element.getAttributeValue("name"));
                commandBean.setRemoveTrailingNewline(
                    element.getAttributeValue("removeTrailingNewline") //NON-NLS
                        .equalsIgnoreCase("true") //NON-NLS
                );
                commandBean.setCommand(element.getText());
                this.commands.add(commandBean);
            } else if (element.getName().equals("LastCustomCommand")) { //NON-NLS
                lastCustomCommand = new CommandBean();
                lastCustomCommand.setName("Custom"); //NON-NLS
                lastCustomCommand.setCommand(element.getText());

                lastCustomCommand.setRemoveTrailingNewline(false);

                if (
                    element.getAttributeValue("removeTrailingNewline") //NON-NLS
                        .equalsIgnoreCase("true") //NON-NLS
                    )
                {
                    lastCustomCommand.setRemoveTrailingNewline(true);
                }
            }
        }
    }

    public void setFromConfigurationBean(final ConfigurationBean bean) {
        this.shellCommand = bean.getShellCommand();
        this.commands = bean.getCommands();
    }

    public ConfigurationBean getConfigurationBean() {
        final ConfigurationBean configurationBean = new ConfigurationBean();
        configurationBean.setShellCommand(this.shellCommand);
        configurationBean.setCommands(this.commands);
        return configurationBean;
    }

    public CommandBean getLastCustomCommand() {
        return lastCustomCommand;
    }

    public void setLastCustomCommand(final CommandBean lastCustomCommand) {
        this.lastCustomCommand = lastCustomCommand;
    }

    public String getLastSelectedCommand() {
        return lastSelectedCommand;
    }

    public void setLastSelectedCommand(final String lastSelectedCommand) {
        this.lastSelectedCommand = lastSelectedCommand;
    }
}
