package br.unb.cic.ethutil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class LocalMachineSpike {

	public static void main(String[] args) throws Exception{
		System.out.println(InetAddress.getLocalHost());
		for (String s :EthUtilNetworkInterfaceHelper.listLocalAddresses()){
			System.out.println(s);
		}
		Enumeration<NetworkInterface> interfaces = (Enumeration<NetworkInterface>)NetworkInterface.getNetworkInterfaces();
		while(interfaces.hasMoreElements()) {
			NetworkInterface net = interfaces.nextElement();
			StringBuilder sMAC = new StringBuilder();
			byte[] mac = net.getHardwareAddress();
			if (mac != null){ 
		        for (int i = 0; i < mac.length; i++) {
		            sMAC.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));        
		        }
			}
			String msg = String.format("%s , %s : %s \t %s", 
					net.getName(), net.getDisplayName(), 
					sMAC, net.getMTU());
			System.out.println(msg);
		}
	}
	
}
