package core;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class MyAppender extends AppenderSkeleton {
	
	//Variablen
	private JList jList;
	private DefaultListModel defaultListModel;
	
	
	//Ctor welcher Liste und Model von Server entf�ngt
	public MyAppender(JList a, DefaultListModel defaultListModel) {
		this.jList = a;
		this.defaultListModel = defaultListModel;
	}
    
	
	//�berschriebene append-Methode - hinzuf�gen der Loggdaten direkt in die Liste
	public void append(LoggingEvent event) {
        defaultListModel.addElement(new String(this.layout.format(event)));
        jList.ensureIndexIsVisible(defaultListModel.size() - 1);
   }

	
	//�berschriebene close-Methode
    public void close() {
        if(this.closed)
            return;
        this.closed = true;
    }
    
    
    //�berschriebene Layout-Methode welche immer ben�tigt wird
    public boolean requiresLayout() {
        return true;
    }    
}