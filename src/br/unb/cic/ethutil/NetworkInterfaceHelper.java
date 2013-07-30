package br.unb.cic.ethutil;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class NetworkInterfaceHelper {
	public static String[] listLocalAddresses() throws SocketException {
		Enumeration<NetworkInterface> interfaces = (Enumeration<NetworkInterface>)NetworkInterface.getNetworkInterfaces();
		Map<Integer, String> addressMap = new HashMap<Integer, String>(); 
		while(interfaces.hasMoreElements()) {
			analyseInterface(interfaces,addressMap);
		}
		
		String[] addresses = new String[addressMap.size()];
		int i = 0;
		for (Integer index : addressMap.keySet()){
			addresses[i++] = addressMap.get(index);
		}
		
		return addresses;
	}

	private static void analyseInterface(
			Enumeration<NetworkInterface> interfaces, Map<Integer, String> addressMap) throws SocketException {
		NetworkInterface _interface = interfaces.nextElement();
		if (isValidInterface(_interface)){
			Enumeration<InetAddress> adresses = _interface.getInetAddresses();
			while(adresses.hasMoreElements()) {
				analyseAddress(adresses,_interface,addressMap);
			}
		}
	}

	private static void analyseAddress(Enumeration<InetAddress> adresses,
										NetworkInterface _interface,
										Map<Integer, String> addressMap) {
		InetAddress addr = adresses.nextElement();
		if (isValidAddress(addr)){
			addressMap.put(_interface.getIndex(), addr.getHostAddress());
		}
	}

	private static boolean isValidAddress(InetAddress ia) {
		return !ia.isLoopbackAddress() && !ia.isAnyLocalAddress() 
				&& !ia.isMulticastAddress()
				&& ia instanceof Inet4Address; // Ignores IPv6
	}

	private static boolean isValidInterface(NetworkInterface ni)
			throws SocketException {
		return !ni.isLoopback() && !ni.isVirtual() && ni.isUp();
	}
}
