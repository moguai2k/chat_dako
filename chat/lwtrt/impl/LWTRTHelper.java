package lwtrt.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import lwtrt.pdu.*;

public class LWTRTHelper {

	private static Vector<LWTRTPdu> recvCache = new Vector<LWTRTPdu>();

	public static Vector<LWTRTPdu> getRecvCache() {
		return recvCache;
	}

	public static void setRecvCache(Vector<LWTRTPdu> recvCache) {
		LWTRTHelper.recvCache = recvCache;
	}

	// Hilfsmethode, die die lokale Addresse ausliest und zurückgibt.
	public static String fetchLocalAddress() {
		String localAddress = null;
		try {
			localAddress = (String)InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return localAddress;
	}
	
}
