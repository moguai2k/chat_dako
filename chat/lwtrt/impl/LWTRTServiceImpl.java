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
	
	// Log
	private static Log log = LogFactory.getLog(LWTRTServiceImpl.class);
	
	// Sockets (Wrapper)
	protected volatile static ConcurrentHashMap<Integer, UdpSocketWrapper> socketMap = new ConcurrentHashMap<Integer, UdpSocketWrapper>();
	// Alle Connections in einer Map.
	protected volatile static ConcurrentHashMap<Integer, LWTRTConnectionImpl> connectionMap = new ConcurrentHashMap<Integer, LWTRTConnectionImpl>();
	
	// Jedes empfangene Paket kommt erstmal hier rein.
	protected volatile static Vector<LWTRTPdu> recvCache = new Vector<LWTRTPdu>();
	
	// Connectionrequests werden hier gespeichert. (Serverseitig)
	protected volatile Vector<LWTRTPdu> connectionRequests = new Vector<LWTRTPdu>();
	
	// Der Wrapper zum senden/empfangen. Jedes Serviceobjekt hat genau einen!
	private UdpSocketWrapper wrapper;
	
	// Der registrierte listenport.
	private int listenPort;
	// Über die Hilfsmethode fetchLocalAddress wird hier die Lokale Adresse als String gespeichert. Final!
	private final String localAddress = this.fetchLocalAddress();
	// Sequencenumber im Serviceobjekt.
	private long sequenceNumber = 0;
	
	// Unterscheidungen zwischen Client und Server.
	private boolean serverRunning = false;
	private boolean isServer = false;
	
	// Getter + Setter
	public boolean isServer() {
		return isServer;
	}

	public void setServer(boolean isServer) {
		this.isServer = isServer;
	}

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

	// Registriert einen Port. Erstellt Wrapper und speichert ihn in der socketMap.
	// Wenn ein Port schon registriert wurde, wird automatisch inkrementiert und nochmal
	// versucht bis ein freier Port gefunden wurde. (quasi Server hat bei lokalem Testen fest die 50000,
	// Client versucht auch die 50000, belegt, also nimmt er als listenport die 50001. Jeder weitere Client simultan,
	// der User hat damit nichts zu tun. Nur der Port des Servers muss bekannt sein.)
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

	// Port aus der Map raus und wrapper schließen.
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
	
	// Client versucht einen Connect zu einer Zieladresse. Pdu wird weggeschickt und es wird auf ein ResponsePDU
	// gewartet. Über die Calendarklasse wird 10 Sekunden auf Response gewartet und eventuell nochmal geschickt.
	// Kommt Response zurück wird Verbindung aufgebaut.
	@Override
	public LWTRTConnection connect(String remoteAddress, int remotePort) throws LWTRTException {
		
		UdpSocketWrapper wrapper = LWTRTServiceImpl.socketMap.get(listenPort);		
		LWTRTPdu conReq = new LWTRTPdu(LWTRTPdu.OPID_CONNECT_REQ, remotePort, remoteAddress, this.sequenceNumber, null);
		LWTRTPdu conRecv = new LWTRTPdu();
		
		// Schleife für 2 Wiederholungen. Nicht ganz sicher ob so optimal. Haben ein wenig ausprobiert,
		// Lösung in ConnectionImpl etwas anders. (Alternative wäre ein Timer gewesen).
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
		// Neue Connection erstellen. Ab in die connectionmap. Passende Threads starten und connection zurückgeben.
		// Das ganze ist jetzt Clientseitig ausgelegt!
		LWTRTConnectionImpl connection =  new LWTRTConnectionImpl(localAddress, listenPort,
				conRecv.getRemoteAddress(), conRecv.getRemotePort(), this);
		connectionMap.put(conRecv.getRemotePort(), connection);
		LWTRTServiceRecvThread recvThread = new LWTRTServiceRecvThread();
		recvThread.start();
		LWTRTConnectionRecvThread handleThread = new LWTRTConnectionRecvThread(this);
		handleThread.start();
		return connection;
	}

	// Serverseitig... accept muss also ständig auf connectionrequests warten.
	@Override
	public LWTRTConnection accept() throws LWTRTException {	
		LWTRTPdu recvPdu = new LWTRTPdu();
		UdpSocketWrapper wrapper = LWTRTServiceImpl.socketMap.get((Integer) listenPort);
		LWTRTConnectionImpl connection;
		// 1. Fall: Server hat noch keine Connection aufgebaut. Simultan zu connect nur in while!
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
					isServer  = true;
					return connection;
				}
			}
		} else {
			// etwas redundant, aber dafür deutlich.
			// 2. Fall: Der Server läuft schon, muss aber trotzdem auf connections warten.
			// Es wird ständig in einem Vector geschaut, ob ein connectionrequest ankam und dann
			// simultan zum 1. Fall darauf reagiert. Threads werden jetzt allerdings nicht mehr gesartet,
			// da sie nach der ersten Connection schon laufen. Anhand der connectionMap mit den unterschiedlichen
			// Remoteports, kann jetzt auf alle Clients reagiert werden. Jede Verbindung wird durch ein ConnectionImpl Objekt
			// realisiert.
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
	
	// Innere Klasse für den generellen Receivethread. Synchronisiert mit dem passenden UDPwrapper(1 Paket nach dem anderen :)).
	// Sobald eine Connection entstanden ist, läuft dieser Thread. Er macht nichts anderes, als die receive-Methode in
	// der Wrapperklasse aufzurufen. Wenn ein Paket empfangen wurde, wird es im Vector des Serviceobjekts gespeichert.
	// Kurzer Sleep nach jedem Durchlauf (Performanz).
	public class LWTRTServiceRecvThread extends Thread {
		
		UdpSocketWrapper wrapper = socketMap.get((listenPort));

		@Override
		public void run() {
			log.debug("--LWTRTConnection Receive Thread auf: " +listenPort+ " gestartet.--");
			synchronized (wrapper) {
				while (true) {
					LWTRTPdu recvPdu = new LWTRTPdu();
					try {
						this.wrapper.receive(recvPdu);
					} catch (IOException e) {
						e.printStackTrace();
						this.stop();
					}
					recvCache.add(recvPdu);
					log.debug("PDU in Receive-Cache gespeichert. Hash:  " +recvPdu);
					try{
						Thread.sleep(25);
					} catch (InterruptedException e){
						log.debug(e);
					}
				}		
			}
		}
	}
}
