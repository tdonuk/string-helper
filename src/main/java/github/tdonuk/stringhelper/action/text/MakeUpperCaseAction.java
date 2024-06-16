package github.tdonuk.stringhelper.action.text;

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

import java.util.Locale;

public class MakeUpperCaseAction extends EditorAction {
	public MakeUpperCaseAction() {
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
					
					try {
						String result = selectedText.toUpperCase(Locale.getDefault());
						WriteCommandAction.runWriteCommandAction(openProjects[0], () -> {
							if (!document.isWritable()) {
								return;
							}
							
							final SelectionModel selectionModel = editor.getSelectionModel();
							
							document.replaceString(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd(), result);
						});
					} catch(Exception e) {
						Boolean isOk = new CustomDialogWrapper("Encoding Error", "An error has occurred: " + e.getMessage(), DialogType.ERROR).showAndGet();
					}
					
				}
			}
		});
	}
	
	protected MakeUpperCaseAction(EditorActionHandler defaultHandler) {
		super(defaultHandler);
	}
}
