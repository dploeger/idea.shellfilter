package de.dieploegers.develop.idea.shellfilter.beans;

import java.util.List;

public class ConfigurationBean {
    private String shellCommand;
    private List<CommandBean> commands;

    public ConfigurationBean() {
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
}