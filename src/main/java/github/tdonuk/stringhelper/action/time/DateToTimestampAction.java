package github.tdonuk.stringhelper.action.time;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import github.tdonuk.stringhelper.gui.CustomDialogWrapper;
import github.tdonuk.stringhelper.gui.DialogType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class DateToTimestampAction extends EditorAction {
    private static SimpleDateFormat[] dateFormatters = new SimpleDateFormat[]{new SimpleDateFormat("dd/MM/yyyy hh:mm:ss"), new SimpleDateFormat("dd.MM.yyyy hh:mm:ss"), new SimpleDateFormat("dd-MM-yyyy hh:mm:ss")};
    public DateToTimestampAction() {
        super(new EditorActionHandler() {
            @Override
            protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
                final Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
                if (openProjects.length == 0) {
                    return;
                }

                final String selectedText = editor.getSelectionModel().getSelectedText();

                final Document document = editor.getDocument();

                if (selectedText != null && !selectedText.isEmpty()) {
                    Date date = null;
                    for(SimpleDateFormat formatter : dateFormatters) {
                        try {
                            date = formatter.parse(selectedText);
                            break;
                        } catch (ParseException e) {
                            continue;
                        }
                    }

                    try {
                        if(date == null) throw new Exception("Unknown date format: " + selectedText + " (example: 18/04/2023 17:42:35)");

                        Instant result = date.toInstant();
                        WriteCommandAction.runWriteCommandAction(openProjects[0], () -> {
                            if (!document.isWritable()) {
                                return;
                            }

                            final SelectionModel selectionModel = editor.getSelectionModel();

                            document.replaceString(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd(), String.valueOf(result.getEpochSecond()));
                        });
                    } catch(Exception e) {
                        e.printStackTrace();
                        Boolean isOk = new CustomDialogWrapper("Encoding Error", "An error has occurred: " + e.getMessage(), DialogType.ERROR).showAndGet();
                    }

                }
            }
        });
    }

    protected DateToTimestampAction(EditorActionHandler defaultHandler) {
        super(defaultHandler);
    }
}
