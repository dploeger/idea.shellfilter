package de.dieploegers.develop.idea.shellfilter.error;

/**
 * Shell command produced no output
 */
public class ShellCommandNoOutputException extends Exception {
    private static final long serialVersionUID = -6645513124637254208L;

    public ShellCommandNoOutputException(String message) {
        super(message);
    }
}
