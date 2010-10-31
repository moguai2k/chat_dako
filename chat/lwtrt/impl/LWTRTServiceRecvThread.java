package lwtrt.impl;

import udp.wrapper.UdpSocketWrapper;
import lwtrt.pdu.LWTRTPdu;
import java.io.IOException;

public class LWTRTServiceRecvThread extends Thread {
	
	UdpSocketWrapper wrapper;
	LWTRTConnectionImpl connection;

	public LWTRTServiceRecvThread(UdpSocketWrapper wrapper, LWTRTConnectionImpl connection) {
		this.wrapper = wrapper;
		this.connection = connection;
	}

	@Override
	public void run() {
		while (true) {
			LWTRTPdu recvPdu = new LWTRTPdu();
			try {
				wrapper.receive(recvPdu);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Socket ist geschlossen!");
				this.stop();
			}
			this.connection.recvCache.add(recvPdu);
		}
	}
}