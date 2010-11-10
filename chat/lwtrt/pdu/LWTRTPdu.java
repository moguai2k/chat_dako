package lwtrt.pdu;

import java.io.Serializable;
import java.net.InetAddress;

public class LWTRTPdu implements Serializable {

	private static final long serialVersionUID = -6172619032079227582L;

	public static final int OPID_CONNECT_REQ = 1;
	public static final int OPID_CONNECT_RSP = 2;
	public static final int OPID_DISCONNECT_REQ = 3;
	public static final int OPID_DISCONNECT_RSP = 4;
	public static final int OPID_DATA_REQ = 5;
	public static final int OPID_DATA_RSP = 6;
	public static final int OPID_PING_REQ = 7;
	public static final int OPID_PING_RSP = 8;

	int opId;
	int remotePort;
	String remoteAddress;
	long sequenceNumber;
	Object userData;

	public LWTRTPdu(int opId, int sourcePort, String remoteAddress,
			long sequenceNumber, Object userData) {
		this.setOpId(opId);
		this.setRemoteAddress(remoteAddress);
		this.setRemotePort(sourcePort);
		this.setSequenceNumber(sequenceNumber);
		this.setUserData(userData);
	}
	
	
	public LWTRTPdu(int opId, long sequenceNumber) {
		this(opId, 0, "0.0.0.0", sequenceNumber, null);
	}

	public LWTRTPdu() {
	};

	/**
	 * @return the opId
	 */
	public int getOpId() {
		return opId;
	}

	/**
	 * @param opId
	 *            the opId to set
	 */
	public void setOpId(int opId) {
		this.opId = opId;
	}

	/**
	 * @return the remotePort
	 */
	public int getRemotePort() {
		return remotePort;
	}

	/**
	 * @param remotePort
	 *            the remotePort to set
	 */
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	/**
	 * @return the remoteAddress
	 */
	public String getRemoteAddress() {
		return remoteAddress;
	}

	/**
	 * @param remoteAddress
	 *            the remoteAddress to set
	 */
	public void setRemoteAddress(String remoteAddress) {
		try {
			this.remoteAddress = InetAddress.getByName(remoteAddress)
					.getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the sequenceNumber
	 */
	public long getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * @param sequenceNumber
	 *            the sequenceNumber to set
	 */
	public void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @return the userData
	 */
	public Object getUserData() {
		return userData;
	}

	/**
	 * @param userData
	 *            the userData to set
	 */
	public void setUserData(Object userData) {
		this.userData = userData;
	}

	public void clone(LWTRTPdu lwtrtPdu) {
		this.opId = lwtrtPdu.getOpId();
		this.remoteAddress = lwtrtPdu.getRemoteAddress();
		this.remotePort = lwtrtPdu.getRemotePort();
		this.sequenceNumber = lwtrtPdu.getSequenceNumber();
		this.userData = lwtrtPdu.getUserData();
	}
}
