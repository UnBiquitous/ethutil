package br.unb.cic.ethutil;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class EthUtilNetworkInterfaceHelper {
	
	public static void main(String[] args) throws Exception {
		for(String s :listLocalAddresses()){
			System.out.print(s+" ,");
		}
		System.out.println();
	}
	
	public static String[] listLocalAddresses() throws SocketException {
		Enumeration<NetworkInterface> interfaces = (Enumeration<NetworkInterface>)NetworkInterface.getNetworkInterfaces();
		List<String> addressList = new ArrayList<String>();
		while(interfaces.hasMoreElements()) {
			analyseInterface(interfaces,addressList);
		}
		
		String[] addresses = new String[addressList.size()];
		int i = addressList.size();
		for (String addr : addressList){
			addresses[--i] = addr;
		}
		
		return addresses;
	}

	private static void analyseInterface(
			Enumeration<NetworkInterface> interfaces, List<String> addressList) throws SocketException {
		NetworkInterface _interface = interfaces.nextElement();
		if (isValidInterface(_interface)){
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

	private static boolean isValidInterface(NetworkInterface ni)
			throws SocketException {
		return !ni.isLoopback() && !ni.isVirtual() && ni.isUp();
	}
}
