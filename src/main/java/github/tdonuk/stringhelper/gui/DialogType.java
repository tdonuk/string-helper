package github.tdonuk.stringhelper.gui;

import com.intellij.ui.JBColor;

import java.awt.*;

public enum DialogType {
	ERROR(new JBColor(new Color(236, 122, 122), new Color(236, 122, 122))),
	WARNING(new JBColor(new Color(245, 226, 164), new Color(245, 226, 164))),
	INFO(new JBColor(new Color(167, 186, 243), new Color(167, 186, 243)));
	
	private final Color color;
	
	public Color getColor() {
		return color;
	}
	
	DialogType(Color color) {
		this.color = color;
	}
}
