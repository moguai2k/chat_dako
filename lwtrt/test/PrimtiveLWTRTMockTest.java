package test;

import junit.framework.Assert;

import org.junit.Test;

import lwtrt.LWTRTConnection;
import lwtrt.ex.LWTRTException;
import test.mocks.LWTRTServiceMock;

public class PrimtiveLWTRTMockTest {

	@Test
	public void testLWTRTMock() {
		AcceptConnectionAndEchoMessagesThread server = new AcceptConnectionAndEchoMessagesThread(
				50000);
		server.start();
		// Warte 300ms bis Server initialisiert ist
		try {
			Thread.sleep(300L);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		// Teste Echo-Server
		try {
			LWTRTServiceMock serviceMock = new LWTRTServiceMock();
			serviceMock.register(50002);
			LWTRTConnection clientConnection = serviceMock.connect("127.0.0.1",
					50000);

			clientConnection.send("Hallo");
			Object receivedObject = clientConnection.receive();
			Assert.assertEquals(receivedObject, "Hallo");
		} catch (LWTRTException e) {
			Assert.fail();
		}

	}

	public static class AcceptConnectionAndEchoMessagesThread extends Thread {
		int port;

		public AcceptConnectionAndEchoMessagesThread(int port) {
			this.port = port;
		}

		@Override
		public void run() {
			try {
				LWTRTServiceMock serviceMock = new LWTRTServiceMock();
				serviceMock.register(port);
				LWTRTConnection connection = serviceMock.accept();
				// verbindung wurde akzeptiert. starte echo
				while (true) {
					Object o = connection.receive();
					connection.send(o);
				}
			} catch (LWTRTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
