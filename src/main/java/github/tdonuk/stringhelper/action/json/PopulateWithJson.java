package github.tdonuk.stringhelper.action.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class PopulateWithJson extends EditorAction {
	private static final ObjectMapper mapper = new JsonMapper();
	
	static {
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
		mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		mapper.setDateFormat(dateTimeFormat);
	}
	
	protected PopulateWithJson(EditorActionHandler defaultHandler) {
		super(defaultHandler);
	}
	
	protected PopulateWithJson() {
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
					if(json == null || ((String) json).isBlank()) return;
					try {
						WriteCommandAction.runWriteCommandAction(project, () -> {
							Map<String, Object> jsonFields;
							try {
								jsonFields = mapper.readValue((String) json, HashMap.class);
							} catch(JsonProcessingException e) {
								throw new RuntimeException(e);
							}
							
							for (Map.Entry<String, Object> entry : jsonFields.entrySet()) {
								String fieldName = entry.getKey();
								Object fieldValue = entry.getValue();
								
								// Create the PsiField with the appropriate type and name
								PsiField field = elementFactory.createField(fieldName, PsiType.getTypeByName(fieldValue.getClass().getSimpleName(), project, GlobalSearchScope.projectScope(project)));
								
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
