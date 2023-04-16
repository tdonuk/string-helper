package github.tdonuk.stringhelper.gui;

import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.awt.*;

public class CustomDialogWrapper extends DialogWrapper {
	
	private final String message;
	private final DialogType type;
	
	public CustomDialogWrapper(String title, String message, DialogType type) {
		super(true); // use current window as parent
		setTitle(title);
		
		this.message = message;
		this.type = type;
		
		init();
	}
	
	@Override
	protected JComponent createCenterPanel() {
		JPanel dialogPanel = new JPanel(new BorderLayout());
		
		JLabel label = new JLabel(message);
		label.setPreferredSize(new Dimension(100, 100));
		dialogPanel.add(label, BorderLayout.CENTER);
		
		JPanel statusPanel = new JPanel();
		statusPanel.setBackground(type.getColor());
		
		dialogPanel.add(statusPanel, BorderLayout.SOUTH);
		
		return dialogPanel;
	}
}