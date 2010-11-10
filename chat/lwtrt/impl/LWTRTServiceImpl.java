package lwtrt.impl;

//Hashmap für die Sockets. Aus dem Beispiel entnommen.
import java.util.concurrent.ConcurrentHashMap;
import java.util.Calendar;
import java.util.Timer;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import udp.wrapper.UdpSocketWrapper;
import lwtrt.pdu.LWTRTPdu;
import lwtrt.LWTRTConnection;
import lwtrt.LWTRTService;
import lwtrt.ex.LWTRTException;

public class LWTRTServiceImpl implements LWTRTService {
	
	// Instanzvariablen
	private static Log log = LogFactory.getLog(LWTRTServiceImpl.class);
	protected static ConcurrentHashMap<Integer, UdpSocketWrapper> socketMap = new ConcurrentHashMap<Integer, UdpSocketWrapper>();
	protected static ConcurrentHashMap<Integer, LWTRTConnectionImpl> connectionMap = new ConcurrentHashMap<Integer, LWTRTConnectionImpl>();
	public static LWTRTServiceImpl INSTANCE = new LWTRTServiceImpl();
	private int listenPort;
	private String localAddress = LWTRTHelper.fetchLocalAddress();
	private long sequenceNumber = 0;
	
	@Override
	public void register(int listenPort) throws LWTRTException {
		try {
			UdpSocketWrapper wrapper = new UdpSocketWrapper(listenPort);
			LWTRTServiceImpl.socketMap.put(listenPort, wrapper);
			log.debug("Listenport: " +listenPort+ " wurde registriert.");
			log.debug("UDP-Wrapper erstellt. Hashcode: " +LWTRTServiceImpl.socketMap.get((Integer) listenPort));
			this.listenPort = listenPort;
		} catch (SocketException e) {
			log.debug(listenPort+ " schon in der Socketmap vorhanden. Versuche den nächsten.");
			this.register(listenPort=listenPort+1);
		}
	}

	@Override
	public void unregister() throws LWTRTException {
		try {
			UdpSocketWrapper wrapper = socketMap.get(listenPort);
			socketMap.remove(listenPort);
			wrapper.close();
			log.debug("Unregister port:" +listenPort+ " und aus der socketMap entfernt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public LWTRTConnection connect(String remoteAddress, int remotePort) throws LWTRTException {
		
		UdpSocketWrapper wrapper = LWTRTServiceImpl.socketMap.get(listenPort);		
		LWTRTPdu conReq = new LWTRTPdu(LWTRTPdu.OPID_CONNECT_REQ, remotePort, remoteAddress, this.sequenceNumber, null);
		LWTRTPdu conRecv = new LWTRTPdu();
		
		// Schleife für 2 Wiederholungen
		for (int i=1; i<=2; i++) {
			try {
				wrapper.send(conReq);
				log.debug("Connection-Request gesendet");
				log.debug("LocalAddress: " +localAddress+ " Listenport: " +listenPort);
				log.debug("RemoteAddress: " +remoteAddress+ " RemotePort: " +remotePort);
			} catch (IOException e) {
				e.printStackTrace();
			}
			long tenSecondsFromNow = Calendar.getInstance().getTimeInMillis() + 10000;
			try {
				while (Calendar.getInstance().getTimeInMillis() < tenSecondsFromNow) {
					wrapper.receive(conRecv);
					log.debug("Warte auf Response vom Server");
					if (conRecv != null) break; //While
				}		
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (conRecv != null) {
				log.debug("Connection-Receive empfangen");
				break; //For
			}
		} 
		
		LWTRTConnectionImpl connection =  new LWTRTConnectionImpl(localAddress, listenPort,
				conRecv.getRemoteAddress(), conRecv.getRemotePort(), wrapper);
		connectionMap.put(listenPort, connection);
		LWTRTServiceRecvThread thread = new LWTRTServiceRecvThread(connection);
		thread.start();
		return connection;
	}

	@Override
	public LWTRTConnection accept() throws LWTRTException {
		LWTRTPdu recvPdu = new LWTRTPdu();
		UdpSocketWrapper wrapper = LWTRTServiceImpl.socketMap.get((Integer) listenPort);
		while (true) {
			try {
				wrapper.receive(recvPdu);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			if (recvPdu.getOpId() == LWTRTPdu.OPID_CONNECT_REQ) {
				log.debug("Connection Request PDU empfangen. Hash: " +recvPdu);
				break;
			}
		}
		LWTRTPdu respPdu = new LWTRTPdu();
		respPdu.setOpId(LWTRTPdu.OPID_CONNECT_RSP);
		respPdu.setRemoteAddress(recvPdu.getRemoteAddress());
		respPdu.setRemotePort(recvPdu.getRemotePort());
		respPdu.setSequenceNumber(recvPdu.getSequenceNumber());
		log.debug("LocalAddress: " +localAddress+ " Listenport: " +listenPort);
		log.debug("RemoteAddress: " +recvPdu.getRemoteAddress()+ " RemotePort: " +recvPdu.getRemotePort());
		try {
			wrapper.send(respPdu);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Connection erstellen und in einer Map speichern
		LWTRTConnectionImpl connection = new LWTRTConnectionImpl(localAddress, listenPort, 
				recvPdu.getRemoteAddress(), recvPdu.getRemotePort(), wrapper);
		connection.setServer(true);
		log.debug("Connection erstellt zu: " +recvPdu.getRemoteAddress()+ ", Remoteport: " +recvPdu.getRemotePort());
		LWTRTServiceImpl.connectionMap.put(recvPdu.getRemotePort(), connection);
		// Service Receive Thread starten
		LWTRTServiceRecvThread thread = new LWTRTServiceRecvThread(connection);
		thread.start();
		
		return connection;
	}
	
	public class LWTRTServiceRecvThread extends Thread {
		
		LWTRTConnectionImpl connection;

		public LWTRTServiceRecvThread(LWTRTConnectionImpl connection) {
			this.connection = connection;
		}

		@Override
		public void run() {
			log.debug("--LWTRTService Receive Thread gestartet: " + connection.getLocalPort() +"--");
				
				while (true) {
					LWTRTPdu recvPdu = new LWTRTPdu();
					try {
						this.connection.getWrapper().receive(recvPdu);
					} catch (IOException e) {
						e.printStackTrace();
						this.stop();
					}
					this.connection.recvCache.add(recvPdu);
					log.debug("PDU in Receive-Cache gespeichert. Hash:  " +recvPdu);
				}
		}
	}
}
