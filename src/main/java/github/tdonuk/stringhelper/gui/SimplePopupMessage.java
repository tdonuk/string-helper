package github.tdonuk.stringhelper.gui;

import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;

import javax.swing.*;
import java.awt.*;

public class SimplePopupMessage {
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
}
