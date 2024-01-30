package de.dieploegers.develop.idea.shellfilter.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NonNls;

public class CommandBean {
    @JsonProperty
    private String name;
    @JsonProperty
    private String command;
    @JsonProperty
    private boolean removeTrailingNewline;

    public CommandBean() {
    }

    public CommandBean(@NonNls final String name,
                       @NonNls final String command,
                       final boolean removeTrailingNewline)
    {
        this.name = name;
        this.command = command;
        this.removeTrailingNewline = removeTrailingNewline;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(final String command) {
        this.command = command;
    }

    public boolean isRemoveTrailingNewline() {
        return removeTrailingNewline;
    }

    public void setRemoveTrailingNewline(final boolean removeTrailingNewline) {
        this.removeTrailingNewline = removeTrailingNewline;
    }

    @Override
    public String toString() {
        return name;
    }
}
