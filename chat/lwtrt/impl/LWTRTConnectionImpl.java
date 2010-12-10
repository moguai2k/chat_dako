package lwtrt.impl;

import lwtrt.pdu.LWTRTPdu;
import lwtrt.LWTRTConnection;
import lwtrt.ex.LWTRTException;

import java.io.IOException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LWTRTConnectionImpl implements LWTRTConnection {
	
	private static Log log = LogFactory.getLog(LWTRTConnectionImpl.class);
	
	private final int RESENDING_TIMES = 2;
	private final int SECONDS_RETRY = 10;
	
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
	// Trunk für data
	private Vector<LWTRTPdu> dataTrunk = new Vector<LWTRTPdu>();
	
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
	
	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public Vector<LWTRTPdu> getPingCache() {
		return pingCache;
	}

	public void setPingCache(Vector<LWTRTPdu> pingCache) {
		this.pingCache = pingCache;
	}

	public Vector<LWTRTPdu> getResponeTrunk() {
		return responeTrunk;
	}

	public void setResponeTrunk(Vector<LWTRTPdu> responeTrunk) {
		this.responeTrunk = responeTrunk;
	}

	public Vector<LWTRTPdu> getDataTrunk() {
		return dataTrunk;
	}

	public void setDataTrunk(Vector<LWTRTPdu> dataTrunk) {
		this.dataTrunk = dataTrunk;
	}
	
// Ende Getter + Setter

	@Override
	public void disconnect() throws LWTRTException {
		LWTRTPdu pdu = new LWTRTPdu();
		pdu.setOpId(LWTRTPdu.OPID_DISCONNECT_REQ);
		pdu.setRemoteAddress(remoteAddress);
		pdu.setRemotePort(remotePort);
		pdu.setSequenceNumber(sequenceNumber);
		try {
			service.getWrapper().send(pdu);
		} catch (IOException e) {
			e.printStackTrace();
		}
		responseArrivedResend(pdu, RESENDING_TIMES, SECONDS_RETRY);
	}

	@Override
	public void acceptDisconnection() throws LWTRTException {
		log.debug("Ein Client hat sich abgemeldet. Garbage-Collection wurde gestartet.");
		long time = System.currentTimeMillis();
		System.gc();
		log.debug("Es dauerte " +(System.currentTimeMillis()-time)+ " ms.");
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
		responseArrivedResend(pdu, RESENDING_TIMES, SECONDS_RETRY);
	}
	
	@Override
	public Object receive() throws LWTRTException {
		while (true) {
			if (!this.dataTrunk.isEmpty()) {
				LWTRTPdu pdu = this.dataTrunk.firstElement();
				this.dataTrunk.remove(pdu);
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
			e.printStackTrace();
		}
		responseArrivedResend(pdu, RESENDING_TIMES, SECONDS_RETRY);
	}
	
	// Hilfsmethode
	private void responseArrivedResend(LWTRTPdu pdu, int sendTimes, int secsToRetry) throws LWTRTException {
		int secsRetry = secsToRetry * 1000;
		int counter = 0;
		long milli = System.currentTimeMillis() + secsRetry;
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
			if(milli < System.currentTimeMillis()) {
				milli = System.currentTimeMillis() + secsRetry;
				counter++;
				log.debug("Paket gesendet - Response kam nach " +secsRetry+ " Sekunden nicht an - " +
						"Paket wird nochmal geschickt. Anzahl der geschickten Pakete: "+ counter);
				try {
					service.getWrapper().send(pdu);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(counter > sendTimes) {
					log.debug("Erzwinge Exit, da kein Response empfangen wurde.");
					System.exit(0);
				}
			}
		}
	}
	
}
