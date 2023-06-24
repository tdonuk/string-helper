package github.tdonuk.stringhelper.action.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import github.tdonuk.stringhelper.gui.CustomDialogWrapper;
import github.tdonuk.stringhelper.gui.DialogType;
import github.tdonuk.stringhelper.gui.PopupDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PopulateWithJsonAction extends EditorAction {
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	protected PopulateWithJsonAction(EditorActionHandler defaultHandler) {
		super(defaultHandler);
	}
	
	protected PopulateWithJsonAction() {
		super(new EditorActionHandler() {
			
			@Override
			protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
				Project project = ProjectManager.getInstance().getOpenProjects()[0];
				
				PsiJavaFile classFile = (PsiJavaFile) PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
				
				if(classFile == null) {
					CustomDialogWrapper dialog = new CustomDialogWrapper("Problem!", "You should open a proper class file to populate it from json", DialogType.WARNING);
					dialog.showAndGet();
					return;
				}
				
				PsiClass[] classes = classFile.getClasses();
				
				PsiClass classToPopulate = classes[0];
				
				PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
				
				PopupDialog.getInput("Json", (json) -> {
					if(json == null || ((String) json).isEmpty()) return;
					try {
						WriteCommandAction.runWriteCommandAction(project, () -> {
							TypeToken<HashMap<String, Object>> token = new TypeToken<HashMap<String, Object>>(){};
							Map<Object, Object> jsonFields= gson.fromJson(((String) json), token.getType());
							
							for (Map.Entry<Object, Object> entry : jsonFields.entrySet()) {
								String fieldName = String.valueOf(entry.getKey());
								Object fieldValue = entry.getValue();
								
								if(fieldValue instanceof Map) fieldValue = new Object();
								if(fieldValue instanceof Collection<?>) fieldValue = new ArrayList<>();
								
								PsiClassType type = PsiType.getTypeByName(fieldValue.getClass().getCanonicalName(), project, GlobalSearchScope.projectScope(project));
								
								// Create the PsiField with the appropriate type and name
								PsiField field = elementFactory.createField(fieldName,type);
								
								// Set the field modifiers as needed (e.g., private, public, static)
								field.getModifierList().setModifierProperty(PsiModifier.PRIVATE, true);
								
								// Add the field to the class
								classToPopulate.add(field);
							}
						});
					} catch(Exception e) {
						new CustomDialogWrapper("Problem!", "A problem is occurred: " + e.getMessage(), DialogType.WARNING).showAndGet();
						return;
					}
				}).showInCenterOf(editor.getContentComponent());
			}
		});
	}
	
}
