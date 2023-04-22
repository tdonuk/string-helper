package github.tdonuk.stringhelper.action.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.profile.codeInspection.ui.table.ScopesOrderTable;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiTreeUtil;
import github.tdonuk.stringhelper.gui.CustomDialogWrapper;
import github.tdonuk.stringhelper.gui.DialogType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ExtractJsonBodyAction extends EditorAction {
    private static final ObjectMapper mapper = new JsonMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
    }

    protected ExtractJsonBodyAction(EditorActionHandler defaultHandler) {
        super(defaultHandler);
    }

    protected ExtractJsonBodyAction() {
        super(new EditorActionHandler() {
            @Override
            protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
                Project project = ProjectManager.getInstance().getOpenProjects()[0];

                PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

                PsiClass psiClass = PsiTreeUtil.getParentOfType(psiFile.findElementAt(editor.getCaretModel().getOffset()), PsiClass.class);

                PsiField[] declaredFields = psiClass.getFields();

                Map<String, Object> jsonFields = getFieldsOfObject(declaredFields);

                try {
                    StringWriter writer = new StringWriter();
                    mapper.writerWithDefaultPrettyPrinter().writeValue(writer, jsonFields);

                    String result = writer.toString();

                    JPanel panel = new JPanel(new BorderLayout());

                    JTextArea textArea = new JTextArea();
                    textArea.append(result);

                    JButton closeButton = new JButton("Close");
                    closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                    panel.add(textArea, BorderLayout.CENTER);
                    panel.add(closeButton, BorderLayout.SOUTH);

                    Balloon popup = JBPopupFactory.getInstance().createDialogBalloonBuilder(panel, "Json Result").createBalloon();

                    closeButton.addActionListener((e) -> {
                        popup.dispose();
                    });

                    popup.showInCenterOf(editor.getContentComponent());

                } catch (IOException e) {
                    Boolean isOk = new CustomDialogWrapper("Json Error", "An error has occurred: " + e.getMessage(), DialogType.ERROR).showAndGet();
                    e.printStackTrace();
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
        else if (typeName.startsWith("java.time.LocalDate")) {
            return java.time.LocalDate.now();
        }
        else if (typeName.startsWith("java.time.LocalTime")) {
            return java.time.LocalTime.now();
        }
        else if (typeName.startsWith("java.time.LocalDateTime")) {
            return java.time.LocalDateTime.now();
        }
        else if (typeName.startsWith("java.util.List") || typeName.startsWith("java.util.ArrayList")) {
            Object value = getInitialValueFromType(((PsiClassType) type).getParameters()[0]);
            return List.of(value);
        }
        else if (typeName.startsWith("java.util.Map") || typeName.startsWith("java.util.HashMap")) {
            PsiType[] arguments = ((PsiClassType) type).getParameters();
            Object keyExample = getInitialValueFromType(arguments[0]);
            Object valueExample = getInitialValueFromType(arguments[1]);
            return Map.of(keyExample, valueExample);
        }
        else if (typeName.startsWith("java.util.Set") || typeName.startsWith("java.util.HashSet")) {
            Object typeValue = getInitialValueFromType(((PsiClassType) type).getParameters()[0]);
            return Set.of(typeValue);
        }
        else {
            try {
                PsiClassType classType = (PsiClassType) type;
                PsiClass fieldClass = classType.resolve();
                return getFieldsOfObject(fieldClass.getFields());
            } catch(Exception e) {

            }
            return "unknown field type";
        }
    }

    private static Map<String, Object> getFieldsOfObject(PsiField[] declaredFields) {
        Map<String, Object> fields = new HashMap<>();

        for (PsiField field : declaredFields) {
            if(Arrays.stream(field.getAnnotations()).filter(a -> a.getText().equals("@JsonIgnore")).findAny().isPresent()) continue;

            String fieldName = field.getName();
            PsiType fieldType = field.getType();

            for(PsiAnnotation an : field.getAnnotations()) {
                if(an.getText().equals("@JsonIgnore")) continue;
            }

            fields.put(fieldName,getInitialValueFromType(fieldType));
        }

        return fields;
    }
}
