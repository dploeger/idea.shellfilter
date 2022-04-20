package de.dieploegers.develop.idea.shellfilter;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Pair;
import de.dieploegers.develop.idea.shellfilter.beans.CommandBean;
import org.jetbrains.annotations.NonNls;

public class CustomFilterAction extends FilterAction {

    @NonNls
    private final Logger LOG = Logger.getInstance(CustomFilterAction.class);

    @Override
    public void actionPerformed(final AnActionEvent anActionEvent) {

        LOG.debug("Custom Shell Filter was invoked. Fetching data from event.");

        final Editor editor =
                anActionEvent.getRequiredData(CommonDataKeys.EDITOR);

        LOG.debug("Custom command selected. Show input dialog");

        final CommandBean lastCustomCommand =
            Settings.getInstance().getLastCustomCommand();

        final Pair<String, Boolean> customCommand = getCustomCommand(lastCustomCommand);
        if (customCommand == null) return;

        final CommandBean command = new CommandBean("Custom",
                customCommand.getFirst(),
                customCommand.getSecond()
        );

        this.runCommand(command, editor);

    }

}
