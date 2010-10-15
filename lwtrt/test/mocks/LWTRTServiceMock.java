package test.mocks;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import lwtrt.LWTRTConnection;
import lwtrt.LWTRTService;
import lwtrt.ex.LWTRTException;

public class LWTRTServiceMock implements LWTRTService {
	private static Log log = LogFactory.getLog(LWTRTServiceMock.class);

	static ConcurrentHashMap<Integer, ServerSocket> socketmap = new ConcurrentHashMap<Integer, ServerSocket>();
	int port;

	@Override
	public void unregister() throws LWTRTException {
		try {
			ServerSocket serverSocket = socketmap.get(port);
			socketmap.remove(port);
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public LWTRTConnection connect(String remoteAddress, int remotePort)
			throws LWTRTException {
		try {
			log.debug("Connection from " + port + " to RemoteAdress: "
					+ remoteAddress + ":" + remotePort);
			return new MockLWTRTConnection(
					new Socket(remoteAddress, remotePort));
		} catch (IOException e) {
			throw new LWTRTException(e);
		}

	}

	public LWTRTConnection accept() throws LWTRTException {
		try {
			log.debug("Accept on " + port);
			ServerSocket serverSocket = socketmap.get(port);

			if (serverSocket == null) {
				serverSocket = new ServerSocket(port);
				socketmap.put(port, serverSocket);
			}
			Socket socket2 = serverSocket.accept();
			return new MockLWTRTConnection(socket2);

		} catch (IOException e) {
			throw new LWTRTException(e);
		}
	}

	public void register(int localPort) throws LWTRTException {
		port = localPort;

	}

}
