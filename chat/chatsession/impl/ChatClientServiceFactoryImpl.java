package chatsession.impl;
/*
 * Es wird ein ServiceObjekt (LWTRT) erstellt und die Methode register bietet jetzt
 * die Möglichkeit auf LWTRT-Schicht einen Listenport zu registrieren.
 * Zurückgegeben wird dann ein neues ChatClientServiceImpl Objekt mit dem dazugehörigem LWTRTService
 * Objekt. Kommunikation kann also starten. 
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import chatsession.ChatClientService;
import chatsession.ChatClientServiceFactory;
import chatsession.ex.ChatServiceException;
import lwtrt.ex.LWTRTException;
import lwtrt.impl.LWTRTServiceImpl;

public class ChatClientServiceFactoryImpl implements ChatClientServiceFactory {
	
	//Attribute//
	private static Log log = LogFactory.getLog(BaseServiceImpl.class);
	protected LWTRTServiceImpl lwtrtService = new LWTRTServiceImpl();
	
	
	//Empfangenen Port registrieren
	@Override
	public ChatClientService register(int port) throws ChatServiceException {
		try {
    		lwtrtService.register(port);
		} catch (LWTRTException e) {
			log.debug("Fehler beim registrieren des Ports im LWTRTService: " +e);
		}
		return new ChatClientServiceImpl(lwtrtService);
	}
	

}

