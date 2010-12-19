package lwtrt.impl;

import lwtrt.pdu.LWTRTPdu;
import lwtrt.LWTRTConnection;
import lwtrt.ex.LWTRTException;

import java.io.IOException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LWTRTConnectionImpl implements LWTRTConnection {
	
	// Log
	private static Log log = LogFactory.getLog(LWTRTConnectionImpl.class);
	
	// 2x neu senden, jeweils 10 Sekunden warten.
	private final int RESENDING_TIMES = 2;
	private final int SECONDS_RETRY = 10;
	
	// Lokale Adresse und Port + Remoteadresse und Port.
	// Die lokale wird in jeder Connection mitgespeicher, obwohl Sie eigentlich garnicht benötigt wird.
	// Ich habe da irgendwas in der Spezifikation von gelesen. Für debug und logging Zwecke vielleicht ganz sinnvoll.
	private String localAddress;
	private String remoteAddress;
	private int localPort;
	private int remotePort;

	// Genaue Identität eines Pakets.
	private long sequenceNumber;
	
	// Das zugehörige Service-Objekt.
	private LWTRTServiceImpl service;

	// Puffer für Pings
	private Vector<LWTRTPdu> pingCache = new Vector<LWTRTPdu>();
	// Response Trunk
	private Vector<LWTRTPdu> responeTrunk = new Vector<LWTRTPdu>();
	// Trunk für data
	private Vector<LWTRTPdu> dataTrunk = new Vector<LWTRTPdu>();
	
	// Alles wichtige wird initialisiert.
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

	// disconnect-Request wird versendet.
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
		responseArrivedResend(pdu);
	}

	// Baustelle... naja, wir wussten jetzt nicht genau, was mit der Methode anzufangen ist.
	// Im Endeffekt wird sie jetzt nach einem erfolgreichen disconnect aufgerufen und macht eine
	// GarbageCollection. (nur im Server, da der Client ja beendet wurde). Der Client soll seinen service 
	// abmelden, jedoch ist er wahrscheinlich vorher schon geschlossen. (wie in der Email erwähnt... disconnect durch
	// alle Schichten.)
	@Override
	public void acceptDisconnection() throws LWTRTException {
		if (service.isServer()) {
			log.debug("Ein Client hat sich abgemeldet. Garbage-Collection wurde gestartet.");
			long time = System.currentTimeMillis();
			System.gc();
			log.debug("Es dauerte " +(System.currentTimeMillis()-time)+ " ms.");
		}
		else service.unregister();	
	}

	// Hiermit können jetzt Data-Objekte verschickt werden.
	// Sequencenumber wird bei jedem Aufruf inkrementiert um Eindeutigkeit generell zu gewährleisten.
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
		responseArrivedResend(pdu);
	}
	
	// Der dataTrunk enthält die PDUS mit den eigentlichen Daten.
	// Im Pdu wird der eigentliche Datenkern als Object realisiert.
	// Diese Methode gibt auf dieser Basis die empfangenen Datenpakete 
	// an seinen Aufrufer zurück. Wenn nichts da ist, gibts null.
	@Override
	public Object receive() throws LWTRTException {
		if (!this.dataTrunk.isEmpty()) {
			LWTRTPdu pdu = this.dataTrunk.firstElement();
			this.dataTrunk.remove(pdu);
			log.debug("PDU abgearbeitet und aus Trunk entfernt");
			return pdu.getUserData();
		}
		else return null;
	}
	
	// Ping realisiert, wird allerdings nicht verwendet. Hätten wir noch mehr Zeit gehabt, wären z.B. Pings
	// alle x-Sekunden interessant gewesen (Ob der Partner noch aktiv ist).
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
		responseArrivedResend(pdu);
	}
	
	// Hilfsmethode für den Empfang eines Responses. Es wird auf ein Paket mit der selben sequencenumber gewartet.
	// Wenn es nach zuerst 1 Sekunde dann 10 Sekunden nicht da ist, wird nochmal gesendet. Nach RESENDING_TIMES mal und
	// keinem passenden Response, wird das Programm beendet.
	private void responseArrivedResend(LWTRTPdu pdu) throws LWTRTException {
		int millisRetry = SECONDS_RETRY * 1000;
		int counter = 1;
		long milli = System.currentTimeMillis() + 1000;
		int seconds = 1;
		boolean received = false;
		while(received==false) {
			for (int i=1; i<responeTrunk.size(); i++) {
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
				log.debug("Paket gesendet - Response kam nach " +seconds+ " Sekunde/n nicht an - " +
						"Paket wird nochmal geschickt. Anzahl der geschickten Pakete: "+ counter);
				milli = System.currentTimeMillis() + millisRetry;
				seconds = SECONDS_RETRY;
				try {
					service.getWrapper().send(pdu);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(counter >= RESENDING_TIMES) {
					log.debug("Erzwinge Exit, da kein Response empfangen wurde.");
					service.unregister();
					System.exit(0);
				}
				counter++;
			}
		}
	}
	
}
