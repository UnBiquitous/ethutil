package br.unb.cic.ethutil;

import java.util.HashSet;
import java.util.Vector;

public class RadarSpike {

	public static void main(String[] args) throws InterruptedException {
		EthUtil util = new EthUtil(new EthUtilClientListener() {
			
			HashSet<String> oldSet = new HashSet<String>();
			
			public void deviceDiscoveryFinished(Vector<String> neighbourDiscoveredHosts) {
				HashSet<String> newSet = new HashSet<String>(neighbourDiscoveredHosts);
				
				HashSet<String> newGuys = (HashSet<String>) newSet.clone();
				newGuys.removeAll(oldSet);
				
				HashSet<String> byeGuys = (HashSet<String>) oldSet.clone();
				byeGuys.removeAll(newSet);
				
				System.out.println(
						String.format(	"\nNew:%s\nBye:%s\nA:%s\n", 
										newGuys, byeGuys, 
										neighbourDiscoveredHosts));
				oldSet = newSet;
			}
			
			public void deviceDiscovered(String host) {
//				System.out.println(String.format("D:%s", host));
			}
		});
		
		for (int i = 0; i < 10; i++){
			util.discoverDevices(EthUtil.DISCOVER_DEVICES_USING_PING);
			Thread.sleep(1000);
		}
	}

}
