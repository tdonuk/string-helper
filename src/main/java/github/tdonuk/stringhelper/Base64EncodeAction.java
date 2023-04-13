package github.tdonuk.stringhelper;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64EncodeAction extends EditorAction {
	
	public Base64EncodeAction() {
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
					WriteCommandAction.runWriteCommandAction(openProjects[0], () -> {
						if (!document.isWritable()) {
							return;
						}
						
						final SelectionModel selectionModel = editor.getSelectionModel();
						document.replaceString(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd(), Base64.getEncoder().encodeToString(selectedText.getBytes(StandardCharsets.US_ASCII)));
					});
				}
			}
		});
	}
	
	protected Base64EncodeAction(EditorActionHandler defaultHandler) {
		super(defaultHandler);
	}
}
