package lwtrt.impl;

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
	
}
