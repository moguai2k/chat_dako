package lwtrt.impl;

//Hashmap für die Sockets. Aus dem Beispiel entnommen.
import java.util.concurrent.ConcurrentHashMap;
import java.util.Calendar;
import java.util.Vector;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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
	
	private UdpSocketWrapper wrapper;
	private Vector<LWTRTPdu> connectionRequests = new Vector<LWTRTPdu>();
	private int listenPort;
	private String localAddress = this.fetchLocalAddress();
	private long sequenceNumber = 0;
	private boolean serverRunning = false;
	
	public Vector<LWTRTPdu> getConnectionRequests() {
		return connectionRequests;
	}

	public void setConnectionRequests(Vector<LWTRTPdu> connectionRequests) {
		this.connectionRequests = connectionRequests;
	}

	public int getListenPort() {
		return listenPort;
	}

	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}

	public UdpSocketWrapper getWrapper() {
		return wrapper;
	}

	public void setWrapper(UdpSocketWrapper wrapper) {
		this.wrapper = wrapper;
	}

	@Override
	public void register(int listenPort) throws LWTRTException {
		try {
			wrapper = new UdpSocketWrapper(listenPort);
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
				conRecv.getRemoteAddress(), conRecv.getRemotePort(), this);
		connectionMap.put(conRecv.getRemotePort(), connection);
		LWTRTServiceRecvThread thread = new LWTRTServiceRecvThread();
		thread.start();
		LWTRTConnectionRecvThread handleThread = new LWTRTConnectionRecvThread(this);
		handleThread.start();
		return connection;
	}

	@Override
	public LWTRTConnection accept() throws LWTRTException {	
		LWTRTPdu recvPdu = new LWTRTPdu();
		UdpSocketWrapper wrapper = LWTRTServiceImpl.socketMap.get((Integer) listenPort);
		LWTRTConnectionImpl connection;
		if (!serverRunning) {
			while (true) {
				try {
					wrapper.receive(recvPdu);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				if (recvPdu.getOpId() == LWTRTPdu.OPID_CONNECT_REQ) {
					log.debug("Connection Request PDU empfangen. Hash: " +recvPdu);
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
					connection = new LWTRTConnectionImpl(localAddress, listenPort, 
							recvPdu.getRemoteAddress(), recvPdu.getRemotePort(), this);
					log.debug("Connection erstellt zu: " +recvPdu.getRemoteAddress()+ ", Remoteport: " +recvPdu.getRemotePort());
					LWTRTServiceImpl.connectionMap.put(recvPdu.getRemotePort(), connection);
					LWTRTServiceRecvThread thread = new LWTRTServiceRecvThread();
					LWTRTConnectionRecvThread handleThread = new LWTRTConnectionRecvThread(this);
					thread.start();
					handleThread.start();
					serverRunning = true;
					return connection;
				}
			}
		} else {
			while (true) {
				if (!connectionRequests.isEmpty()) {
					recvPdu = connectionRequests.firstElement();
					connectionRequests.remove(recvPdu);
					log.debug("Connection Request PDU empfangen. Hash: " +recvPdu);
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
					connection = new LWTRTConnectionImpl(localAddress, listenPort, 
							recvPdu.getRemoteAddress(), recvPdu.getRemotePort(), this);
					log.debug("Connection erstellt zu: " +recvPdu.getRemoteAddress()+ ", Remoteport: " +recvPdu.getRemotePort());
					LWTRTServiceImpl.connectionMap.put(recvPdu.getRemotePort(), connection);
					return connection;
				}
			}		
		}
	}
	
	// Gibt die lokale Addresse zurück.
	private String fetchLocalAddress() {
		String localAddress = null;
		try {
			localAddress = (String)InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return localAddress;
	}
	
	public class LWTRTServiceRecvThread extends Thread {
		
		UdpSocketWrapper wrapper = socketMap.get((listenPort));

		@Override
		public void run() {
			log.debug("--LWTRTConnection Receive Thread gestartet: " +listenPort+ "--");
			synchronized (wrapper) {
				while (true) {
					LWTRTPdu recvPdu = new LWTRTPdu();
					try {
						this.wrapper.receive(recvPdu);
					} catch (IOException e) {
						e.printStackTrace();
						this.stop();
					}
					LWTRTHelper.getRecvCache().add(recvPdu);
					log.debug("PDU in Receive-Cache gespeichert. Hash:  " +recvPdu);
					try{
						Thread.sleep(20);
					} catch (InterruptedException e){
						log.debug(e);
					}
				}		
			}
		}
	}
}
