package lwtrt.impl;

//Hashmap für die Sockets. Aus dem Beispiel entnommen.
import java.util.concurrent.ConcurrentHashMap;
import java.util.Calendar;
import java.io.IOException;

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
	static ConcurrentHashMap<Integer, UdpSocketWrapper> socketMap = new ConcurrentHashMap<Integer, UdpSocketWrapper>();
	static ConcurrentHashMap<Integer, LWTRTConnectionImpl> connectionMap = new ConcurrentHashMap<Integer, LWTRTConnectionImpl>();
	public static LWTRTServiceImpl INSTANCE = new LWTRTServiceImpl();
	private UdpSocketWrapper recvWrapper;
	
	private int localPort;
	private String localAddress;
	private long sequenceNumber = 0;
	
	public LWTRTServiceImpl() {
		localPort = 50000;
	}
	
	
	/* Register-Dienst: lokaler Dienst
	 * - Kommunikationspartner wird zum Entgegennehmen von Verbindungsaufbauwünschen vorbereitet
	 * - Verbindungswarteschlange wird eingereicht
	 */
	
	@Override
	public void register(int localPort) throws LWTRTException {
		if (socketMap.contains(localPort)) {
			throw new LWTRTException("Port ist schon vergeben.");
		}
		else {
			UdpSocketWrapper wrapper;
			try {
				wrapper = new UdpSocketWrapper(localPort);
				socketMap.put(localPort, wrapper);
				this.localPort = localPort;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/* Unregister-Dienst: lokaler Dienst
	 * - Verbindungswarteschlange wird abgebaut und es wird kein Verbindungswunsch mehr akzeptiert
	 * - Alle Verbindungen werden lokal aufgelöst
	 * - Partner werden nicht informiert. (vereinfacht)
	 */
	
	@Override
	public void unregister() throws LWTRTException {
		UdpSocketWrapper wrapper;
		try {
			wrapper = socketMap.get(localPort);
			socketMap.remove(localPort);
			wrapper.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public LWTRTConnection connect(String remoteAddress, int remotePort)
			throws LWTRTException {
		UdpSocketWrapper wrapper = socketMap.get(localPort);
		LWTRTPdu conReq = new LWTRTPdu(LWTRTPdu.OPID_CONNECT_REQ, localPort, localAddress, 
				remotePort, remoteAddress, sequenceNumber);
		LWTRTPdu conRecv = new LWTRTPdu();
		
		// Schleife für 2 Wiederholungen
		for (int i=1; i<=2; i++) {
			try {
				wrapper.send(conReq);
			} catch (IOException e) {
				e.printStackTrace();
			}
			long tenSecondsFromNow = Calendar.getInstance().getTimeInMillis() + 10000;
			try {
				while (Calendar.getInstance().getTimeInMillis() < tenSecondsFromNow) {
					wrapper.receive(conRecv);
					if (conRecv != null) break; //While
				}		
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (conRecv != null) break; //For
		} 
		
		LWTRTConnectionImpl connection =  new LWTRTConnectionImpl(localAddress, localPort,
				conRecv.getLocalAddress(), conRecv.getLocalPort());
		connectionMap.put(localPort, connection);
		LWTRTServiceRecvThread thread = new LWTRTServiceRecvThread(wrapper, connection);
		thread.start();
		return connection;
	}

	@Override
	public LWTRTConnection accept() throws LWTRTException {
		LWTRTPdu recvPdu = new LWTRTPdu();
		UdpSocketWrapper wrapper = socketMap.get(localPort);
		localAddress = LWTRTHelper.fetchLocalAddress();
		while (true) {
			try {
				wrapper.receive(recvPdu);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			if (recvPdu != null) {
				System.out.println(recvPdu.getOpId());
				break;
			}
		}	
		LWTRTPdu respPdu = new LWTRTPdu();
		respPdu.setOpId(LWTRTPdu.OPID_CONNECT_RSP);
		respPdu.setLocalAddress(localAddress);
		respPdu.setLocalPort(localPort);
		respPdu.setRemoteAddress(recvPdu.getLocalAddress());
		respPdu.setRemotePort(recvPdu.getLocalPort());
		respPdu.setSequenceNumber(recvPdu.getSequenceNumber());
		try {
			recvWrapper = new UdpSocketWrapper(localPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			recvWrapper.send(respPdu);
		} catch (IOException e) {
			e.printStackTrace();
		}
		socketMap.put(localPort, recvWrapper);
		
		// Connection
		LWTRTConnectionImpl connection = new LWTRTConnectionImpl(localAddress, localPort, 
				recvPdu.getLocalAddress(), recvPdu.getLocalPort());
		
		LWTRTServiceRecvThread thread = new LWTRTServiceRecvThread(recvWrapper, connection);
		thread.start();
		connectionMap.put(localPort, connection);
		this.localPort++;
		return connection;
	}
}
