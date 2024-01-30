package de.dieploegers.develop.idea.shellfilter;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.annotations.OptionTag;
import de.dieploegers.develop.idea.shellfilter.beans.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Service
@State(name = "ShellFilterConfig", storages = {
    @Storage("shellfilter-config.xml")
})
public final class Settings implements PersistentStateComponent<Settings.State> {
    public static class State {
        public String shellCommand;

        public List<CommandBean> commands;

        @OptionTag(converter = CommandBeanConverter.class)
        public CommandBean lastCustomCommand;

        public String lastSelectedCommand;

        public State() {
            this.shellCommand = "/bin/sh %s";
            this.commands = new ArrayList<>();
            this.lastCustomCommand = new CommandBean("Custom", "", true);
            this.lastSelectedCommand = null;
        }
    }

    private State state = new State();

    public static Settings getInstance() {
        return ApplicationManager.getApplication()
            .getService(Settings.class);
    }

    public String getShellCommand() {
        return this.state.shellCommand;
    }

    public List<CommandBean> getCommands() {
        return this.state.commands;
    }

    public State getState() {
        return this.state;
    }

    public void loadState(final @NotNull State state) {
        this.state = state;
    }

    @Override
    public void noStateLoaded() {
        final var legacySettings = LegacySettings.getInstance();
        // convert from old version
        this.state.shellCommand = legacySettings.getShellCommand();
        this.state.commands = legacySettings.getCommands();
        this.state.lastCustomCommand = legacySettings.getLastCustomCommand();
        this.state.lastSelectedCommand = legacySettings.getLastSelectedCommand();
    }

    public void setFromConfigurationBean(final ConfigurationBean bean) {
        this.state.shellCommand = bean.getShellCommand();
        this.state.commands = bean.getCommands();
    }

    public ConfigurationBean getConfigurationBean() {
        final ConfigurationBean configurationBean = new ConfigurationBean();
        configurationBean.setShellCommand(this.state.shellCommand);
        configurationBean.setCommands(this.state.commands);
        return configurationBean;
    }

    public CommandBean getLastCustomCommand() {
        return state.lastCustomCommand;
    }

    public String getLastSelectedCommand() {
        return state.lastSelectedCommand;
    }

    public void setLastSelectedCommand(final String lastSelectedCommand) {
        this.state.lastSelectedCommand = lastSelectedCommand;
    }
}
