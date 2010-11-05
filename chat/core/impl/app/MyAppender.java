package core.impl.app;

import javax.swing.*;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

public class MyAppender extends AppenderSkeleton {

	private JTextArea area;
	
	public MyAppender(JTextArea a) {
		this.area = a;
	}

	protected void append(LoggingEvent e) {
		area.setText(area.getText()+"\n"+e.getMessage());
	}

	public void close() {
		
	}

	public boolean requiresLayout() {
		return false;
	}
}