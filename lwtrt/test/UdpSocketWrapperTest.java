package test;

import java.io.IOException;
import java.net.SocketException;

import org.junit.Assert;
import org.junit.Test;

import lwtrt.pdu.LWTRTPdu;
import udp.wrapper.UdpSocketWrapper;

public class UdpSocketWrapperTest {

	@Test
	public void simpleSendDataTest() {
		UdpSocketWrapper socketwrapperSender;
		try {
			socketwrapperSender = new UdpSocketWrapper(50010);

			UdpSocketWrapper socketwrapperReceiver = new UdpSocketWrapper(50020);

			// Datenpaket zum Senden erstellen
			LWTRTPdu pdu = new LWTRTPdu();
			pdu.setRemoteAddress("127.0.0.1");
			pdu.setRemotePort(50020);
			pdu.setUserData("Hallo");
			// Daten versenden
			socketwrapperSender.send(pdu);

			// Datenpaket zum empfangen erstellen
			LWTRTPdu receivedPdu = new LWTRTPdu();
			// Daten empfangen
			socketwrapperReceiver.receive(receivedPdu);

			// Empfangene Daten auswerten
			Assert.assertEquals("Hallo", receivedPdu.getUserData());
			Assert.assertEquals("127.0.0.1", receivedPdu.getRemoteAddress());

			// Vorsicht, hier wird immer der Remote port, also der Port des
			// Partner eingetragen
			Assert.assertEquals(50010, receivedPdu.getRemotePort());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
