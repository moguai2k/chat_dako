package lwtrt.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LWTRTHelper {
	
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
