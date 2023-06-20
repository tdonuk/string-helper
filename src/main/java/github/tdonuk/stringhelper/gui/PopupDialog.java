package github.tdonuk.stringhelper.gui;

import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class PopupDialog {
    public static Balloon get(String message, String title) {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea textArea = new JTextArea();
        textArea.append(message);

        JButton closeButton = new JButton("Close");
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        panel.add(textArea, BorderLayout.CENTER);
        panel.add(closeButton, BorderLayout.SOUTH);

        Balloon popup = JBPopupFactory.getInstance().createDialogBalloonBuilder(panel, title).createBalloon();

        closeButton.addActionListener((e) -> {
            popup.dispose();
        });

        return popup;
    }
    
    public static Balloon getInput(String title, Consumer<Object> onSubmit) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(true);
        
        JPanel buttonPanel = new JPanel(new BorderLayout(5,5));
        
        JButton okButton = new JButton("Process");
        okButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if(onSubmit != null) {
            okButton.addActionListener(e -> onSubmit.accept(textArea.getText()));
        }
        
        buttonPanel.add(okButton);
        
        panel.add(textArea, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        Balloon popup = JBPopupFactory.getInstance().createDialogBalloonBuilder(panel, title).createBalloon();
        
        textArea.setColumns(30);
        textArea.setRows(10);
        
        okButton.addActionListener((e) -> {
            popup.dispose();
        });
        
        return popup;
    }
}
