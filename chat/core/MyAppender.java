package core;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class MyAppender extends AppenderSkeleton {

	private JList jList;
	private DefaultListModel defaultListModel;
	
	
	
	public MyAppender(JList a, DefaultListModel defaultListModel) {
		this.jList = a;
		this.defaultListModel = defaultListModel;
	}
    

	public void append(LoggingEvent event) {
        defaultListModel.addElement(new String(this.layout.format(event)));
        jList.ensureIndexIsVisible(defaultListModel.size() - 1);
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