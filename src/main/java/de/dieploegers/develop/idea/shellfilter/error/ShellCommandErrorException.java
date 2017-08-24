package de.dieploegers.develop.idea.shellfilter.error;

/**
 * The shell command exited with an error
 */
public class ShellCommandErrorException extends Exception {
    private static final long serialVersionUID = 7033532565240424095L;

    public ShellCommandErrorException(String message) {
        super(message);
    }
}
