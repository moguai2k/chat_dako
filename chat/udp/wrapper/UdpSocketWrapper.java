package udp.wrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.log4j.Logger;

import lwtrt.pdu.LWTRTPdu;

public class UdpSocketWrapper {

	private DatagramSocket socket;
	private static Logger log = Logger.getLogger(UdpSocketWrapper.class);

	public UdpSocketWrapper(int port) throws SocketException {
		socket = new DatagramSocket(port);
	}

	public synchronized void receive(LWTRTPdu lwtrtPdu) throws IOException {
		byte[] bytes = new byte[65527];
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
		socket.receive(packet);
		ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
		ObjectInputStream ois = new ObjectInputStream(bais);
		try {
			lwtrtPdu.clone((LWTRTPdu) ois.readObject());
			String remoteAddress = packet.getAddress().toString();
			remoteAddress = remoteAddress.substring(1, remoteAddress.length());
			lwtrtPdu.setRemoteAddress(remoteAddress);
			lwtrtPdu.setRemotePort(packet.getPort());
			log.debug("RECEIVE: from " + packet.getAddress() + ":"
					+ packet.getPort() + "  Opid-seqnr   " + lwtrtPdu.getOpId()
					+ "-" + lwtrtPdu.getSequenceNumber());
		} catch (ClassNotFoundException e) {
			log.error("ClassNotFoundException:", e);
		}
	}

	public void send(LWTRTPdu lwtrtPdu) throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(lwtrtPdu);
		byte[] bytes = out.toByteArray();
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length,
				InetAddress.getByName(lwtrtPdu.getRemoteAddress()),
				lwtrtPdu.getRemotePort());
		log.debug("SEND: " + packet.getAddress() + ":" + packet.getPort()
				+ "  Opid-seqnr   " + lwtrtPdu.getOpId() + "-"
				+ lwtrtPdu.getSequenceNumber());
		socket.send(packet);

	}

	public String getLocalAddress() {
		return socket.getLocalAddress().getHostAddress();
	}

	public int getLocalPort() {
		return socket.getLocalPort();
	}

	public void close() {
		socket.close();
	}

}
