package core;

import javax.swing.JTextArea;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class MyAppender extends AppenderSkeleton {

	private JTextArea area;
	
	
	public MyAppender(JTextArea a) {
		this.area = a;
	}
    

	public void append(LoggingEvent event) {
        //System.out.print(this.layout.format(event));
		area.setText(area.getText()+this.layout.format(event));
   }

    public void close() {
        if(this.closed)
            return;
        this.closed = true;
    }
    
    public boolean requiresLayout() {
        return true;
    }    
}