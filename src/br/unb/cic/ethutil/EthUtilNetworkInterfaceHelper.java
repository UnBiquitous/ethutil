package br.unb.cic.ethutil;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class EthUtilNetworkInterfaceHelper {
	
	
	public static String[] listLocalAddresses() throws SocketException {
		return listLocalAddresses(false);
	}
	
	public static String[] listLocalAddresses(boolean includeVirtuals) throws SocketException {
		Enumeration<NetworkInterface> interfaces = (Enumeration<NetworkInterface>)NetworkInterface.getNetworkInterfaces();
		List<String> addressList = new ArrayList<String>();
		while(interfaces.hasMoreElements()) {
			analyseInterface(interfaces,addressList,includeVirtuals);
		}
		
		String[] addresses = new String[addressList.size()];
		int i = addressList.size();
		for (String addr : addressList){
			addresses[--i] = addr;
		}
		
		return addresses;
	}

	private static void analyseInterface(
			Enumeration<NetworkInterface> interfaces, List<String> addressList,
			boolean includeVirtuals) throws SocketException {
		NetworkInterface _interface = interfaces.nextElement();
		if (isValidInterface(_interface,includeVirtuals)){
			Enumeration<InetAddress> adresses = _interface.getInetAddresses();
			while(adresses.hasMoreElements()) {
				analyseAddress(adresses,_interface,addressList);
			}
		}
	}

	private static void analyseAddress(Enumeration<InetAddress> adresses,
										NetworkInterface _interface,
										List<String> addressList) {
		InetAddress addr = adresses.nextElement();
		if (isValidAddress(addr)){
			addressList.add(addr.getHostAddress());
		}
	}

	private static boolean isValidAddress(InetAddress ia) {
		return !ia.isLoopbackAddress() && !ia.isAnyLocalAddress() 
				&& !ia.isMulticastAddress()
				&& ia instanceof Inet4Address; // Ignores IPv6
	}

	private static boolean isValidInterface(NetworkInterface ni,
			boolean includeVirtuals)
			throws SocketException {
		boolean valid = !ni.isLoopback() && ni.isUp() && ni.getMTU() > -1;
		if (includeVirtuals){
			return valid;
		}
		return valid && !isConsideredVirtual(ni);
	}

	private static boolean isConsideredVirtual(NetworkInterface ni) {
		return ni.isVirtual() || isVirualName(ni.getName()) 
				|| isVirualName(ni.getDisplayName());
	}

	private static boolean isVirualName(String name) {
		String upperCaseName = name.toUpperCase();
		return upperCaseName.contains("VMNET") ||
				upperCaseName.contains("VMWARE") ||
				upperCaseName.contains("VIRTUALBOX");
	}
}
