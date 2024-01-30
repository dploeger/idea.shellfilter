package de.dieploegers.develop.idea.shellfilter;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import de.dieploegers.develop.idea.shellfilter.beans.CommandBean;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Service
@State(name = "ShellFilter", storages = {
    @Storage("shellfilter.xml")
})
public final class LegacySettings implements PersistentStateComponent<Element>
{
    @NonNls
    private String shellCommand;
    private List<CommandBean> commands;
    private CommandBean lastCustomCommand;
    private String lastSelectedCommand;

    public LegacySettings() {
        this.shellCommand = "/bin/sh %s";
        this.commands = new ArrayList<>();
        this.lastCustomCommand = new CommandBean("Custom", "", true);
        this.lastSelectedCommand = null;
    }

    public static LegacySettings getInstance() {
        return ApplicationManager.getApplication()
            .getService(LegacySettings.class);
    }

    @Nullable
    @Override
    public Element getState() {
        return null;
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

    public String getShellCommand() {
        return shellCommand;
    }

    public String getLastSelectedCommand() {
        return lastSelectedCommand;
    }

    public CommandBean getLastCustomCommand() {
        return lastCustomCommand;
    }

    public List<CommandBean> getCommands() {
        return commands;
    }
}
