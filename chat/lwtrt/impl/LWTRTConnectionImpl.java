package lwtrt.impl;

import lwtrt.pdu.LWTRTPdu;
import lwtrt.LWTRTConnection;
import lwtrt.ex.LWTRTException;
import lwtrt.impl.LWTRTHelper;

import java.io.IOException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LWTRTConnectionImpl implements LWTRTConnection {
	
	private static Log log = LogFactory.getLog(LWTRTConnectionImpl.class);
	
	
	// Instanzvariablen
	private String localAddress;
	private String remoteAddress;
	private int localPort;
	private int remotePort;

	private long sequenceNumber;
	
	private LWTRTServiceImpl service;

	// Puffer für Pings
	private Vector<LWTRTPdu> pingCache = new Vector<LWTRTPdu>();
	//Response Trunk
	private Vector<LWTRTPdu> responeTrunk = new Vector<LWTRTPdu>();
	
	// Konstruktor
	public LWTRTConnectionImpl(String localAddress, int localPort, String remoteAddress, int remotePort, 
			LWTRTServiceImpl service) {
		this.localAddress = localAddress;
		this.localPort = localPort;
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.service = service;
		this.sequenceNumber = 1;	
	}
	
// Getter + Setter	
	
	public String getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(String localAddress) {
		this.localAddress = localAddress;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public long getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	
	public Vector<LWTRTPdu> getResponeTrunk() {
		return responeTrunk;
	}

	public void setResponeTrunk(Vector<LWTRTPdu> responeTrunk) {
		this.responeTrunk = responeTrunk;
	}
	
	public Vector<LWTRTPdu> getPingCache() {
		return pingCache;
	}

	public void setPingCache(Vector<LWTRTPdu> pingCache) {
		this.pingCache = pingCache;
	}
	
// Ende Getter + Setter
	

	@Override
	public void disconnect() throws LWTRTException {
		LWTRTPdu pdu = new LWTRTPdu();
		pdu.setOpId(LWTRTPdu.OPID_DISCONNECT_REQ);
		pdu.setRemoteAddress(remoteAddress);
		pdu.setRemotePort(remotePort);
		try {
			service.getWrapper().send(pdu);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		boolean received = false;
		while(received==false) {
			for (int i=0; i<responeTrunk.size(); i++) {
				LWTRTPdu element = responeTrunk.get(i);
				log.debug("Warte auf Response...");
				if (pdu.getSequenceNumber() == element.getSequenceNumber()) {
					log.debug("Response PDU eingetroffen und entfernt");
					responeTrunk.remove(element);
					received = true;
					break;
				}	
			}
		}
	}

	@Override
	public void acceptDisconnection() throws LWTRTException {
		LWTRTPdu pdu = new LWTRTPdu();
		pdu.setOpId(LWTRTPdu.OPID_DISCONNECT_RSP);
		pdu.setRemoteAddress(remoteAddress);
		pdu.setRemotePort(remotePort);
		try {
			service.getWrapper().send(pdu);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void send(Object chatPdu) throws LWTRTException {		
		LWTRTPdu pdu = new LWTRTPdu();
		pdu.setOpId(LWTRTPdu.OPID_DATA_REQ);
		pdu.setRemoteAddress(remoteAddress);
		pdu.setRemotePort(remotePort);
		pdu.setUserData(chatPdu);
		pdu.setSequenceNumber(sequenceNumber);
		this.sequenceNumber++;
		try {
			//log.debug("Sende chatPDU -- Versuch: " +i);
			service.getWrapper().send(pdu);
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		int counter = 0;
		long milli = System.currentTimeMillis() + 2000;
		boolean received = false;
		while(received==false) {
			for (int i=0; i<responeTrunk.size(); i++) {
				LWTRTPdu element = responeTrunk.get(i);
				log.debug("Warte auf Response...");
				if (pdu.getSequenceNumber() == element.getSequenceNumber()) {
					log.debug("Response PDU eingetroffen und entfernt");
					responeTrunk.remove(element);
					received = true;
					break;
				}	
			}
			// Testen ob das Response Paket innerhalb von 2000 Millisekunden ankam.
			// Falls nicht, wird das Paket nochmal geschickt, und der timer nochmal um 2000 Millisekunden hochgesetzt
			// Die Counter Variable dient dazu, dass das Paket maximal 5 mal gesendet wird.
			if(milli < System.currentTimeMillis()) {
				milli = System.currentTimeMillis() + 10000;
				counter++;
				log.debug("Paket gesendet - Response kam nach 10 Sekunden nicht an - " +
						"Paket wird nochmal geschickt. Anzahl der geschickten Pakete: "+ counter);
				//Paket wird nochmal gesendet.
				try {
					service.getWrapper().send(pdu);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(counter > 2) {
					System.exit(0);
				}
			}
		}
	}
	
	@Override
	public Object receive() throws LWTRTException {
		while (true) {
			if (!LWTRTHelper.getTrunk().isEmpty()) {
				LWTRTPdu pdu = LWTRTHelper.getTrunk().firstElement();
				LWTRTHelper.getTrunk().remove(pdu);
				log.debug("PDU abgearbeitet und aus Trunk entfernt");
				return pdu.getUserData();
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
	}

	@Override
	public void ping() throws LWTRTException {	
		LWTRTPdu pdu = new LWTRTPdu();
		pdu.setOpId(LWTRTPdu.OPID_PING_REQ);
		pdu.setRemoteAddress(remoteAddress);
		pdu.setRemotePort(remotePort);
		pdu.setSequenceNumber(this.sequenceNumber);
		try {
			service.getWrapper().send(pdu);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
