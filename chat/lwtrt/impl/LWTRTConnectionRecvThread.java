package lwtrt.impl;

import java.io.IOException;
import lwtrt.ex.LWTRTException;
import lwtrt.pdu.LWTRTPdu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LWTRTConnectionRecvThread extends Thread {
	
	private static Log log = LogFactory.getLog(LWTRTConnectionRecvThread.class);
	private LWTRTConnectionImpl connection;

	public LWTRTConnectionRecvThread(LWTRTConnectionImpl connection) {
			this.connection = connection;
	}
	
	public void run() {
		log.debug("LWTRTConnection Receive Thread gestartet: " + connection.getLocalPort());
		while (true) {
			synchronized (connection) {
				if (!connection.recvCache.isEmpty()) {
						LWTRTPdu pdu = connection.recvCache.firstElement();
						switch (pdu.getOpId()) {
							case LWTRTPdu.OPID_CONNECT_REQ: connection.recvCache.remove(pdu); break;
							case LWTRTPdu.OPID_CONNECT_RSP: connection.recvCache.remove(pdu); break;
							case LWTRTPdu.OPID_DISCONNECT_REQ:
								LWTRTPdu respDisc = new LWTRTPdu();
								respDisc.setSequenceNumber(pdu.getSequenceNumber());
								respDisc.setRemoteAddress(connection.getRemoteAddress());
								respDisc.setRemotePort(connection.getRemotePort());
								respDisc.setOpId(LWTRTPdu.OPID_DISCONNECT_RSP);
								try {
									connection.getWrapper().send(respDisc);
									log.debug("Send: Disconnect-Response");
								} catch (IOException e) {
									e.printStackTrace();
								}
								connection.recvCache.remove(pdu);
								this.stop(); break;	
							case LWTRTPdu.OPID_DISCONNECT_RSP:
								connection.responeTrunk.add(pdu);
								log.debug("PDU in Response-Cache gespeichert. Hash:  " +pdu);
								log.debug("Data-Response PDU aus revCache entfernt.");
								connection.recvCache.remove(pdu);
								this.stop();break;
							case LWTRTPdu.OPID_DATA_REQ:
								LWTRTPdu respData = new LWTRTPdu();
								respData.setSequenceNumber(pdu.getSequenceNumber());
								log.debug("get sequence "+respData.getSequenceNumber());
								respData.setRemoteAddress(connection.getRemoteAddress());
								respData.setRemotePort(connection.getRemotePort());
								respData.setOpId(LWTRTPdu.OPID_DATA_RSP);
								try {
									connection.getWrapper().send(respData);
									log.debug("Send: Data-Response");
								} catch (IOException e) {
									e.printStackTrace();
								}
								connection.trunk.add(pdu);
								log.debug("trunk.add("+pdu+")");
								connection.recvCache.remove(pdu);
								log.debug("recvCache.remove("+pdu+")");
								break;
							case LWTRTPdu.OPID_DATA_RSP:
								connection.responeTrunk.add(pdu);
								log.debug("PDU in Response-Cache gespeichert. Hash:  " +pdu);
								log.debug("Data-Response PDU aus revCache entfernt.");
								connection.recvCache.remove(pdu);
								break;
							case LWTRTPdu.OPID_PING_REQ:
								break;
							case LWTRTPdu.OPID_PING_RSP:
								connection.pingCache.add(pdu);
								connection.recvCache.remove(pdu);
								break;
					} // end-switch
				} // end-if 
			} // synchronized
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} // end-while
	} // end-run
} // end-class
