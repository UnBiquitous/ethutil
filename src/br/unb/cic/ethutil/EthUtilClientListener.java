package br.unb.cic.ethutil;

import java.util.Vector;

/**
 * A interface implemented by anyone who wants to be noticied of discovered devices by the EthUtil
 * 
 * @author Passarinho
 *
 */
public interface EthUtilClientListener {
	/**
	 * Notifies a discovered device
	 * @param string
	 */
	public void deviceDiscovered(String host);
	
	/**
	 *  Method invoked when a discovery is completed, it returns a list of all found devices
	 * @param neighbourDiscoveredHosts
	 */
	public void deviceDiscoveryFinished(Vector<String> neighbourDiscoveredHosts);
}
