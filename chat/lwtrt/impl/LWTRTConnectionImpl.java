package lwtrt.impl;

import udp.wrapper.UdpSocketWrapper;
import lwtrt.pdu.LWTRTPdu;
import lwtrt.LWTRTConnection;
import lwtrt.ex.LWTRTException;
import lwtrt.impl.LWTRTHelper;

import java.io.IOException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
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
	
	// TEST
	private boolean server = false;
	
	public boolean isServer() {
		return server;
	}

	public void setServer(boolean server) {
		this.server = server;
	}

	// UDP-Wrapper zum Senden der Protocol Data Units
	private UdpSocketWrapper wrapper;
	// Puffer für empfangene PDU´s
	public Vector<LWTRTPdu> recvCache = new Vector<LWTRTPdu>();
	// Puffer für Pings
	public Vector<LWTRTPdu> pingCache = new Vector<LWTRTPdu>();
	// Eimer PDU`s
	public Vector<LWTRTPdu> trunk = new Vector<LWTRTPdu>();
	//Reveice Trunk
	public Vector<LWTRTPdu> receivetrunk = new Vector<LWTRTPdu>();
	
	// Konstruktor
	public LWTRTConnectionImpl(String localAddress, int localPort, String remoteAddress, int remotePort, UdpSocketWrapper wrapper) {
		this.localAddress = localAddress;
		this.localPort = localPort;
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.wrapper = wrapper;
		LWTRTConnectionRecvThread recvThread = new LWTRTConnectionRecvThread(this);
		recvThread.start();
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
	
	public UdpSocketWrapper getWrapper() {
		return wrapper;
	}

	public void setWrapper(UdpSocketWrapper wrapper) {
		this.wrapper = wrapper;
	}
	
// Ende Getter + Setter

	@Override
	public void disconnect() throws LWTRTException {
		LWTRTPdu pdu = new LWTRTPdu();
		LWTRTPdu recvPdu = new LWTRTPdu();
		pdu.setOpId(LWTRTPdu.OPID_DISCONNECT_REQ);
		pdu.setRemoteAddress(remoteAddress);
		pdu.setRemotePort(remotePort);

		// Schleife für 2 Wiederholungen bis Timeout
		for (int i=1; i<=2; i++) {
			try {
				wrapper.send(pdu);
			} catch (IOException e) {
				e.printStackTrace();
			}
			long tenSecondsFromNow = Calendar.getInstance().getTimeInMillis() + 10000;
			try {
				while (Calendar.getInstance().getTimeInMillis() < tenSecondsFromNow) {
					wrapper.receive(recvPdu);
					if (recvPdu.getOpId() == LWTRTPdu.OPID_DISCONNECT_RSP){
						System.out.println("Disconnect accepted!");
						break; //While
					}
				}		
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (recvPdu.getOpId() == LWTRTPdu.OPID_DISCONNECT_RSP) {
				System.out.println("Disconnect accepted!");
				break; //For
			}
		} 
		
		sequenceNumber = LWTRTHelper.invertSeqNum(sequenceNumber);

	}

	@Override
	public void acceptDisconnection() throws LWTRTException {
		LWTRTPdu pdu = new LWTRTPdu();
		pdu.setOpId(LWTRTPdu.OPID_DISCONNECT_RSP);
		pdu.setRemoteAddress(remoteAddress);
		pdu.setRemotePort(remotePort);
		try {
			wrapper.send(pdu);
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
			wrapper.send(pdu);
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		boolean received = false;
		while(received==false) {
			for (int i=0; i<receivetrunk.size(); i++) {
				LWTRTPdu element = receivetrunk.get(i);
				log.debug("Warte auf Response...");
				if (pdu.getSequenceNumber() == element.getSequenceNumber()) {
					log.debug("Response PDU eingetroffen und entfernt");
					receivetrunk.remove(element);
					received = true;
					break;
				}	
			}
		}
	}
	
	@Override
	public Object receive() throws LWTRTException {
		while (true) {
			if (!trunk.isEmpty()) {
				LWTRTPdu pdu = trunk.firstElement();
				trunk.remove(pdu);
				log.debug("PDU abgearbeitet und aus Trunk entfernt");
				return pdu.getUserData();
			}
			try {
				Thread.sleep(10);
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
		this.sequenceNumber = LWTRTHelper.invertSeqNum(this.sequenceNumber);
		try {
			wrapper.send(pdu);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
