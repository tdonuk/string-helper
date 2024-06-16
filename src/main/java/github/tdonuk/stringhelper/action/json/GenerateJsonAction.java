package github.tdonuk.stringhelper.action.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import github.tdonuk.stringhelper.gui.CustomDialogWrapper;
import github.tdonuk.stringhelper.gui.DialogType;
import github.tdonuk.stringhelper.gui.PopupDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class GenerateJsonAction extends EditorAction {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    protected GenerateJsonAction(EditorActionHandler defaultHandler) {
        super(defaultHandler);
    }

    protected GenerateJsonAction() {
        super(new EditorActionHandler() {
            @Override
            protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
                Project project = ProjectManager.getInstance().getOpenProjects()[0];

                PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
                
                if(psiFile == null) {
                    new CustomDialogWrapper("Problem!", "You should open a proper class file to extract json body from its fields", DialogType.WARNING).showAndGet();
                    return;
                }

                PsiClass psiClass = PsiTreeUtil.getParentOfType(psiFile.findElementAt(editor.getCaretModel().getOffset()), PsiClass.class);

                if(psiClass == null) return;
                
                PsiField[] declaredFields = psiClass.getFields();

                try {
                    Map<String, Object> jsonFields = getFieldsOfObject(declaredFields);
                    
                    String json = gson.toJson(jsonFields);
                    
                    PopupDialog.get(json, "Json Result").showInCenterOf(editor.getContentComponent());
                } catch(Exception e) {
                    Boolean isOk = new CustomDialogWrapper("Serializing Error", "An error has occurred: " + e.getMessage(), DialogType.ERROR).showAndGet();
                    // handle isOk
                }
            }
        });
    }

    private static Object getInitialValueFromType(PsiType type) {
        String typeName = type.getCanonicalText();
        
        // handle array
        if(type instanceof PsiArrayType) {
            PsiArrayType arrayType = (PsiArrayType) type;
            PsiType arrayField = arrayType.getComponentType();
            Object arrayValue = getInitialValueFromType(arrayField);
            return new Object[]{arrayValue};
        }

        if (typeName.startsWith("java.lang.String")) {
            return "some text";
        }
        else if (typeName.startsWith("java.lang.Integer") || typeName.startsWith("int") ||
                typeName.startsWith("java.lang.Short") || typeName.startsWith("short") ||
                typeName.startsWith("java.lang.Byte") || typeName.startsWith("byte")) {
            return 123;
        }
        else if (typeName.startsWith("java.lang.Long") || typeName.startsWith("long") ||
                typeName.startsWith("java.math.BigInteger")) {
            return 2135132414125124L;
        }
        else if (typeName.startsWith("java.lang.Float") || typeName.startsWith("float")) {
            return 0.79f;
        }
        else if (typeName.startsWith("java.lang.Double") || typeName.startsWith("double") ||
                typeName.startsWith("java.math.BigDecimal")) {
            return 12343.75d;
        }
        else if (typeName.startsWith("boolean") || typeName.startsWith("java.lang.Boolean")) {
            return false;
        }
        else if (typeName.startsWith("java.util.Date")) {
            return new java.util.Date();
        }
        else if (typeName.startsWith("java.time.LocalDateTime")) {
            return LocalDateTime.now().toString();
        }
        else if (typeName.startsWith("java.time.LocalDate")) {
            return LocalDate.now().toString();
        }
        else if (typeName.startsWith("java.time.LocalTime")) {
            return LocalTime.now().toString();
        }
        else if (getSuperTypes(type).stream().anyMatch(t -> t.getCanonicalText().startsWith("java.util.Collection"))) {
            PsiClassType classType = (PsiClassType) type;
            Object value;
            
            if(classType.hasParameters()) value = getInitialValueFromType(classType.getParameters()[0]);
            else value = "unknown array type";
            
            return Collections.singletonList(value);
        }
        else if (getSuperTypes(type).stream().anyMatch(t -> t.getCanonicalText().startsWith("java.util.Map"))) {
            PsiClassType classType = (PsiClassType) type;
            
            if(!classType.hasParameters()) {
                return new HashMap<>();
            }
            
            PsiType[] arguments = ((PsiClassType) type).getParameters();
            Object keyExample = getInitialValueFromType(arguments[0]);
            Object valueExample = getInitialValueFromType(arguments[1]);
            
            Map<Object, Object> map = new HashMap<>();
            map.put(keyExample, valueExample);
            return map;
        }
        else {
            try {
                PsiClassType classType = (PsiClassType) type;
                PsiClass fieldClass = classType.resolve();
                
                if(fieldClass == null) return null;
                
                return getFieldsOfObject(fieldClass.getFields());
            } catch(Exception e) {
            }
            
            return "unknown field type";
        }
    }

    private static Map<String, Object> getFieldsOfObject(PsiField[] declaredFields) {
        Map<String, Object> fields = new HashMap<>();

        for (PsiField field : declaredFields) {
            if(Arrays.stream(field.getAnnotations()).anyMatch(a -> a.getText().equals("@JsonIgnore"))) continue;

            String fieldName = field.getName();
            PsiType fieldType = field.getType();
            
            fields.put(fieldName, getInitialValueFromType(fieldType));
        }

        return fields;
    }
    
    private static Set<PsiType> getSuperTypes(PsiType type) {
        if(type.getSuperTypes().length == 0) return new HashSet<>();
        
        PsiType[] superTypesArr = type.getSuperTypes();
        
        Set<PsiType> superTypes = new HashSet<>(Arrays.asList(superTypesArr));
        
        for(PsiType superType : superTypesArr) {
            superTypes.addAll(getSuperTypes(superType));
        }
        
        return superTypes;
    }
}
