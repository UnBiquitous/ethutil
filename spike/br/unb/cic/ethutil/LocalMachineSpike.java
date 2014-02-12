package br.unb.cic.ethutil;

import java.net.InetAddress;

public class LocalMachineSpike {

	public static void main(String[] args) throws Exception{
		System.out.println(InetAddress.getLocalHost());
		for (String s :EthUtilNetworkInterfaceHelper.listLocalAddresses()){
			System.out.println(s);
		}
	}
	
}
