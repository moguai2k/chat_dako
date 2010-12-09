package lwtrt.impl;

import java.io.IOException;
import lwtrt.ex.LWTRTException;
import lwtrt.pdu.LWTRTPdu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LWTRTConnectionRecvThread extends Thread {
	
	private static Log log = LogFactory.getLog(LWTRTConnectionRecvThread.class);
	
	private LWTRTServiceImpl service;

	public LWTRTConnectionRecvThread(LWTRTServiceImpl service) {
			this.service = service;
	}
	
	public void run() {
		log.debug("LWTRTConnection Handle Thread gestartet: " + service.getListenPort());
		while (true) {
			synchronized (LWTRTHelper.getRecvCache()) {
				if (!LWTRTHelper.getRecvCache().isEmpty()) {
						LWTRTPdu pdu = LWTRTHelper.getRecvCache().firstElement();
						LWTRTConnectionImpl connection = LWTRTServiceImpl.connectionMap.get(pdu.getRemotePort());
						switch (pdu.getOpId()) {
							case LWTRTPdu.OPID_CONNECT_REQ: 
								LWTRTHelper.getRecvCache().remove(pdu);
								service.getConnectionRequests().add(pdu);
								break;
							case LWTRTPdu.OPID_CONNECT_RSP: LWTRTHelper.getRecvCache().remove(pdu); break;
							case LWTRTPdu.OPID_DISCONNECT_REQ:
								LWTRTPdu respDisc = new LWTRTPdu();
								respDisc.setSequenceNumber(pdu.getSequenceNumber());
								respDisc.setRemoteAddress(connection.getRemoteAddress());
								respDisc.setRemotePort(connection.getRemotePort());
								respDisc.setOpId(LWTRTPdu.OPID_DISCONNECT_RSP);
								try {
									service.getWrapper().send(respDisc);
									log.debug("Send: Disconnect-Response");
								} catch (IOException e) {
									e.printStackTrace();
								}
								LWTRTHelper.getRecvCache().remove(pdu);
								//this.stop(); 
								break;	
							case LWTRTPdu.OPID_DISCONNECT_RSP:
								connection.getResponeTrunk().add(pdu);
								log.debug("PDU in Response-Cache gespeichert. Hash:  " +pdu);
								log.debug("Data-Response PDU aus revCache entfernt.");
								LWTRTHelper.getRecvCache().remove(pdu);
								break;
							case LWTRTPdu.OPID_DATA_REQ:
								LWTRTPdu respData = new LWTRTPdu();
								respData.setSequenceNumber(pdu.getSequenceNumber());
								respData.setRemoteAddress(connection.getRemoteAddress());
								respData.setRemotePort(connection.getRemotePort());
								respData.setOpId(LWTRTPdu.OPID_DATA_RSP);
								try {
									service.getWrapper().send(respData);
									log.debug("Send: Data-Response");
								} catch (IOException e) {
									e.printStackTrace();
								}
								connection.getDataTrunk().add(pdu);
								log.debug("trunk.add("+pdu+")");
								LWTRTHelper.getRecvCache().remove(pdu);
								log.debug("recvCache.remove("+pdu+")");
								break;
							case LWTRTPdu.OPID_DATA_RSP:
								connection.getResponeTrunk().add(pdu);
								log.debug("PDU in Response-Cache gespeichert. Hash:  " +pdu);
								log.debug("Data-Response PDU aus recvCache entfernt.");
								LWTRTHelper.getRecvCache().remove(pdu);
								break;
							case LWTRTPdu.OPID_PING_REQ:
								connection.getPingCache().add(pdu);
								log.debug("PDU in Ping-Cache gespeichert. Hash: " +pdu);
							case LWTRTPdu.OPID_PING_RSP:
								connection.getPingCache().add(pdu);
								LWTRTHelper.getRecvCache().remove(pdu);
								break;
					} // end-switch
				} // end-if 
			} // synchronized
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} // end-while
	} // end-run
} // end-class
