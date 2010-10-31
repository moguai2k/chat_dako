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
		log.debug("Thread LWTRTConnection gestartet: " + connection.getLocalPort() + this);
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
								respDisc.setLocalAddress(LWTRTHelper.fetchLocalAddress());
								respDisc.setOpId(LWTRTPdu.OPID_DISCONNECT_RSP);
								try {
									connection.getWrapper().send(respDisc);
									log.debug("Send: DC-Response");
								} catch (IOException e) {
									e.printStackTrace();
								}
								try {
									LWTRTServiceImpl.INSTANCE.unregister();
								} catch (LWTRTException e) {
									e.printStackTrace();
								}
								connection.recvCache.remove(pdu);
								this.stop(); break;	
							case LWTRTPdu.OPID_DISCONNECT_RSP:
								try {
									LWTRTServiceImpl.INSTANCE.unregister();
								} catch (LWTRTException e) {
									e.printStackTrace();
								}
								connection.recvCache.remove(pdu);
								this.stop(); break;
							case LWTRTPdu.OPID_DATA_REQ:
								LWTRTPdu respData = new LWTRTPdu();
								respData.setSequenceNumber(pdu.getSequenceNumber());
								respData.setRemoteAddress(connection.getRemoteAddress());
								respData.setRemotePort(connection.getRemotePort());
								respData.setLocalAddress(LWTRTHelper.fetchLocalAddress());
								respData.setLocalPort(connection.getLocalPort());
								respData.setOpId(LWTRTPdu.OPID_DATA_RSP);
								try {
									connection.getWrapper().send(respData);
									log.debug("Send: Data-Response");
								} catch (IOException e) {
									e.printStackTrace();
								}
								connection.trunk.add(pdu);
								connection.recvCache.remove(pdu);
								break;
							case LWTRTPdu.OPID_DATA_RSP:
								if (pdu.getSequenceNumber() == connection.getSequenceNumber())
									connection.setSend(true);
								log.debug("Datasending OK!");
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
