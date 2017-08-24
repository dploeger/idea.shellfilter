package de.dieploegers.develop.idea.shellfilter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.ListSpeedSearch;
import com.intellij.ui.components.JBList;
import de.dieploegers.develop.idea.shellfilter.beans.CommandBean;
import de.dieploegers.develop.idea.shellfilter.error.ShellCommandErrorException;
import de.dieploegers.develop.idea.shellfilter.error.ShellCommandNoOutputException;
import icons.ShellFilterIcons;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FilterAction extends AnAction {

    @NonNls
    private static final String CUSTOM_ID = "_custom";
    private final Logger LOG = Logger.getInstance(FilterAction.class);
    @NonNls
    private final ResourceBundle resourceBundle =
        ResourceBundle.getBundle("i18n.shellfilter");

    private String cat(final InputStream is) throws IOException {
        LOG.debug("Converting stdout stream into a string"); // NON-NLS
        byte[] buf = new byte[65536];
        int offset = 0;
        int bytesRead;
        while ((bytesRead = is.read(buf, offset, 8192)) != -1) {
            offset += bytesRead;
            if (offset + 8192 >= buf.length) {
                final byte[] newBuf = new byte[buf.length * 3];
                System.arraycopy(buf, 0, newBuf, 0, offset);
                buf = newBuf;
            }
        }
        return new String(buf, 0, offset);
    }

    @Override
    public void actionPerformed(final AnActionEvent anActionEvent) {

        LOG.debug("Shell Filter was invoked. Fetching data from event.");

        final Editor editor =
            anActionEvent.getRequiredData(CommonDataKeys.EDITOR);

        LOG.debug("Building up command list from settings and custom command");

        final Settings settings = Settings.getInstance();
        final List<CommandBean> commands = new ArrayList<>(
            settings.getCommands()
        );

        commands.add(0,
            new CommandBean(resourceBundle.getString("custom"),
                CUSTOM_ID,
                true));

        LOG.debug("Building up command list popup");

        final JList<CommandBean> commandList = new JBList<>(commands);

        final String lastSelectedCommand =
            Settings.getInstance().getLastSelectedCommand();

        if (lastSelectedCommand != null) {

            for (int i = 0; i < commands.size(); i++) {
                if (commands.get(i).getName().equals(lastSelectedCommand)) {
                    commandList.setSelectedIndex(i);
                }
            }
        }

        final ListSpeedSearch<CommandBean> commmandListSpeedSearch =
            new ListSpeedSearch<>(commandList);

        final JBPopup popup = JBPopupFactory.getInstance()
            .createListPopupBuilder(
                commmandListSpeedSearch.getComponent()
            ).createPopup();

        LOG.debug("Adding popup listener");

        popup.addListener(new JBPopupListener() {
            @Override
            public void beforeShown(final LightweightWindowEvent e) {
                // nothing
            }

            @Override
            public void onClosed(final LightweightWindowEvent e) {
                if (e.isOk()) {

                    final CommandBean selectedCommand =
                        commandList.getSelectedValue();

                    Settings.getInstance().setLastSelectedCommand(
                        selectedCommand.getName()
                    );

                    final CommandBean command;
                    if (selectedCommand.getCommand().equals(CUSTOM_ID)) {
                        LOG.debug("Custom command selected. Show input dialog");

                        final CommandBean lastCustomCommand =
                            Settings.getInstance().getLastCustomCommand();

                        final Pair<String, Boolean> customCommand =
                            Messages.showInputDialogWithCheckBox(
                                resourceBundle.getString("dialog.custom.message"),
                                resourceBundle.getString("dialog.custom.title"),
                                resourceBundle.getString(
                                    "configuration.command.trim"),
                                lastCustomCommand.isRemoveTrailingNewline(),
                                true,
                                ShellFilterIcons.TOOLBAR_ICON,
                                lastCustomCommand.getCommand(),
                                null
                            );

                        if (StringUtils.isEmpty(customCommand.getFirst())) {
                            LOG.debug("No custom command specified. Canceling");
                            return;
                        }

                        command = new CommandBean("Custom",
                            customCommand.getFirst(),
                            customCommand.getSecond()
                        );

                        LOG.debug("Storing last custom command");

                        lastCustomCommand.setCommand(
                            customCommand.getFirst()
                        );
                        lastCustomCommand.setRemoveTrailingNewline(
                            customCommand.getSecond()
                        );
                    } else {
                        LOG.debug(
                            String.format(
                                "Command %s selected.", //NON-NLS
                                selectedCommand.getName()
                            )
                        );

                        if (StringUtils.isEmpty(selectedCommand.getCommand())) {
                            LOG.debug("Command is empty. Canceling");
                            return;
                        }

                        command = selectedCommand;
                    }

                    runCommand(command, editor);
                } else {
                    LOG.debug("No command was selected. Canceling");
                }
            }
        });
        LOG.debug("Showing popup");
        popup.showInBestPositionFor(editor);
    }

    private void runCommand(final CommandBean command, final Editor editor) {

        LOG.debug("Running command");

        String selectedText = "";

        if (editor.getSelectionModel().hasSelection()) {
            selectedText =
                editor.getSelectionModel().getSelectedText();
            LOG.debug("Providing this text as input: ", selectedText); //NON-NLS
        } else {
            LOG.debug(
                "No text was selected. Won't provide an input to the command.");
        }

        String replacement = null;
        try {
            replacement = process(command.getCommand(), selectedText);
        } catch (final ShellCommandErrorException e) {
            LOG.warn(e);
            Messages.showErrorDialog(
                e.getMessage(),
                resourceBundle.getString("dialog.error.commanderror.title")
            );
            return;
        } catch (final ShellCommandNoOutputException e) {
            LOG.warn(e);
            Messages.showErrorDialog(
                e.getMessage(),
                resourceBundle.getString("dialog.error.nooutput.title")
            );
            return;
        } catch (final IOException e) {
            final String error = String.format(
                resourceBundle.getString("dialog.error.executing.message"),
                e.getMessage()
            );
            LOG.error(error, e);
            Messages.showErrorDialog(
                error,
                resourceBundle.getString("dialog.error.executing.title")
            );
        } catch (final InterruptedException e) {
            final String error = String.format(
                resourceBundle.getString("dialog.error.interrupted.message"),
                e.getMessage()
            );
            LOG.error(error, e);
            Messages.showErrorDialog(
                error,
                resourceBundle.getString("dialog.error.interrupted.title")
            );
        }

        if (replacement != null) {
            if (command.isRemoveTrailingNewline()) {
                replacement = replacement.trim();
            }

            final String replacementText = replacement;

            final Runnable filterRunnable;
            if (editor.getSelectionModel().hasSelection()) {
                LOG.debug(
                    "Replacing selection with this text: ", //NON-NLS
                    replacement
                );
                filterRunnable =
                    () -> editor.getDocument().replaceString(
                        editor.getSelectionModel()
                            .getSelectionStart(),
                        editor.getSelectionModel()
                            .getSelectionEnd(),
                        replacementText
                    );
            } else {
                LOG.debug(
                    "Inserting this text into the editor: ", //NON-NLS
                    replacement
                );
                filterRunnable =
                    () -> editor.getDocument().insertString(
                        editor.getCaretModel()
                            .getCurrentCaret()
                            .getOffset(),
                        replacementText
                    );
            }

            WriteCommandAction.runWriteCommandAction(
                editor.getProject(),
                filterRunnable
            );
        } else {
            LOG.debug("Did not get anything back from shell. Canceling");
        }
    }

    @Override
    public void update(final AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            LOG.debug("No editor selected, disabling action.");
        } else {
            LOG.debug("Editor selected, enabling action");
        }
        e.getPresentation().setVisible(editor != null);
    }

    private String process(final String command,
                           final String filterContent)
        throws
        ShellCommandErrorException,
        ShellCommandNoOutputException,
        IOException, InterruptedException
    {
        LOG.debug("Writing command into a temporary file");

        final File commandFile;
        final FileWriter commandFileWriter;
        try {
            commandFile = File.createTempFile("shellfilter", "command");
            commandFileWriter = new FileWriter(commandFile);
            commandFileWriter.write(command);
            commandFileWriter.close();
        } catch (final IOException e) {
            final String error = String.format(
                resourceBundle.getString(
                    "dialog.error.writingcommandfile.message"),
                e.getMessage()
            );
            LOG.error(error, e);
            Messages.showErrorDialog(
                error,
                resourceBundle.getString("dialog.error.writingcommandfile.title")
            );
            return null;
        }

        LOG.debug("Building up command");

        final String shellCommand = Settings.getInstance().getShellCommand();
        final String cmd;

        if (shellCommand.contains("%s")) { //NON-NLS
            cmd = String.format(shellCommand, commandFile.getPath());
        } else {
            cmd = shellCommand + " " + commandFile.getPath();
        }

        LOG.debug("Running command ", shellCommand); //NON-NLS

        final Process p;
        final DataInputStream pStdout;
        final DataInputStream pStdErr;
        final DataOutputStream pStdin;
        final String output;

        try {
            p = Runtime.getRuntime().exec(cmd);

            pStdout = new DataInputStream(p.getInputStream());
            if (filterContent.length() > 0) {
                LOG.debug("Piping selected text into stdin of the shell");
                pStdin = new DataOutputStream(p.getOutputStream());
                pStdin.write(filterContent.getBytes());
                pStdin.close();
            }
            output = cat(pStdout);

            p.waitFor();

            final int exitValue = p.exitValue();
            if (exitValue != 0) {
                pStdErr = new DataInputStream(p.getErrorStream());
                final String commandError = cat(pStdErr);
                final String error = String.format(
                    resourceBundle.getString("error.commanderror"),
                    cmd,
                    exitValue,
                    output,
                    commandError
                );
                throw new ShellCommandErrorException(error);
            }

            if (StringUtils.isEmpty(output)) {
                final String error = String.format(
                    resourceBundle.getString("error.nooutput"),
                    cmd
                );
                throw new ShellCommandNoOutputException(error);
            }

        } finally {
            if (!commandFile.delete()) {
                LOG.warn(
                    String.format(
                        "Temporary file %s can not be deleted.", //NON-NLS
                        commandFile.getPath()
                    )
                );
            }
        }

        return output;

    }
}
