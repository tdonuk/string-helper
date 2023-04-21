package github.tdonuk.stringhelper.action.time;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import github.tdonuk.stringhelper.gui.CustomDialogWrapper;
import github.tdonuk.stringhelper.gui.DialogType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetCurrentTimeAction extends EditorAction {
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    public GetCurrentTimeAction() {
        super(new EditorActionHandler() {
            @Override
            protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
                final Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
                if (openProjects.length == 0) {
                    return;
                }

                try {
                    dateFormatter.format(new Date());

                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

                    clipboard.setContents(new StringSelection(dateFormatter.format(new Date())), null);

                    Notification notification= new Notification("System Clipboard", "String Helpers", "Copied to your clipboard", NotificationType.INFORMATION);

                    Notifications.Bus.notify(notification);

                } catch(Exception e) {
                    e.printStackTrace();
                    Boolean isOk = new CustomDialogWrapper("Encoding Error", "An error has occurred: " + e.getMessage(), DialogType.ERROR).showAndGet();
                }

            }
        });
    }

    protected GetCurrentTimeAction(EditorActionHandler defaultHandler) {
        super(defaultHandler);
    }
}
