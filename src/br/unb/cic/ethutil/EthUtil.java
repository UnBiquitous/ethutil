package br.unb.cic.ethutil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class provides a util class for searching neighbour devices in a IPv4 Network
 * 
 * @author Passarinho
 *
 */
public class EthUtil {
	
	/* *****************************
	 *   	ATRIBUTES
	 * *****************************/
	private int UBIQUITOS_ETH_TCP_PORT;
	private int UBIQUITOS_ETH_TCP_CONTROL_PORT;
	
	public static final int DISCOVER_DEVICES_USING_ARP  = 0;
	public static final int DISCOVER_DEVICES_USING_PING = 1;
	// Maximum number of threads that will be created to ping
	private static final int TOTAL_PING_THREADS = 32;
	// Defines the listener
	private EthUtilClientListener listener;
	
	//A Set(no replicated) of found IPs(Hosts)
	private Set<String> neighbourIpVector = new HashSet<String>();
	
	// The local Machine
	InetAddress localMachine;
	
	
	/* *****************************
	 *   	CONSTRUCTOR
	 * *****************************/
    
	public EthUtil(EthUtilClientListener listener){
		// Defines the listener
		this.listener = listener;
		
		//Retrieves the local machine Eth NIC
		try {
			localMachine = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/* *****************************
	 *   	PUBLIC METHODS
	 * *****************************/
	
	/**
	 * A Facade Method for start a neighbour discovery. I can be done in two ways:
	 *   Using ARP TABLE - DISCOVER_DEVICES_USING_ARP
	 *   Using PING - DISCOVER_DEVICES_USING_PING
	 *   
	 * This constant is passed using parameter for this method
	 *
	 */
	public void discoverDevices(int discoverMode) {
		if (discoverMode == DISCOVER_DEVICES_USING_ARP){
			this.discoverDevicesUsingArp();
		}else{
			this.discoverDevicesUsingPing();
		}
	}

	/**
	 * Return the Host Name oh the local machine
	 * e.g: "estevao-passarinhos-computer.local"
	 * 
	 * @return String
	 */
	public String getLocalMachineHostName(){
		return localMachine.getHostName();
	}
	
	
	/**
	 * Returns the list of Local Machine Host Addresses, 
	 * without loopback: The Machine IP
	 * e.g: "192.168.1.101"
	 * @return
	 */
	public List<String> getLocalMachineHostAddresses(){
		
		List<String> ips = new ArrayList<String>();
		
		Enumeration<NetworkInterface> e1;
		try {
			e1 = (Enumeration<NetworkInterface>) NetworkInterface.getNetworkInterfaces();
			
			while(e1.hasMoreElements()) {
				NetworkInterface ni = e1.nextElement();
				
				if (!ni.isLoopback() && !ni.isVirtual() && ni.isUp()){
					Enumeration<InetAddress> e2 = ni.getInetAddresses();
					String addr = null;
					while(e2.hasMoreElements()) {
						InetAddress ia = e2.nextElement();
						if (!ia.isLoopbackAddress() && !ia.isAnyLocalAddress() && !ia.isMulticastAddress()){
							if (!ia.toString().contains(":")){
								// FIXME : TCP Plugin : This denies a ipv6 server to be create which is a very restrictive strategy.
								ips.add(ia.getHostAddress());
							}
						}
					}
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ips;
		
		//return localMachine.getHostAddress();
	}
		
	/* *****************************
	 *   	PUBLIC STATIC METHODS
	 * *****************************/
	
	public static Socket getSocket(String host, int port, int timeout) throws Exception{
		return TimedSocket.getSocket(host, port, timeout);
	}

	/* *****************************
	 *   	PRIVATE METHODS - PING
	 * *****************************/
    
	/**
	 * Discovers neihghbour computers using a PING command.
	 * We will search for a IP in a certain range. A IP is a number composed by 4 numbers just like X.X.X.X, where X is a number between 0 and 255.
	 * We will use the first 2 numbers from the same local NIC IP. the other 2 will be searched
	 * 
	 */
	private void discoverDevicesUsingPing() {
		//flags for busy wait
		boolean isThreadAllocated;
		
		// Get the first 2 numbers form the local machine IP spliting the ip in the "." char 
		
		for (String ip : getLocalMachineHostAddresses()) {
		
			String[] splitedIP = ip.split("\\.");
			String ipPrefix = splitedIP[0]+"."+ splitedIP[1] + "."+splitedIP[2] + ".";
			StringBuffer searchedIP = null;
			
			
			// Creates Base Threads for Pinging.
			List<ThreadedPing> threadList = new ArrayList<ThreadedPing>();
			// Starts the first TOTAL_PING_THREADS threads.
			for (int i = 0; i < TOTAL_PING_THREADS; i++) {
				ThreadedPing t = createAndStartThreadedPing(ipPrefix+i);
				threadList.add(t);
			}
			
			// create a combination of all possible ip numbers for the same IP class.
			for (int i = TOTAL_PING_THREADS; i <= 255; i++) {
				isThreadAllocated = false;
				// creates a Ip "xxx.xxx.xxx.i" where xxx.xxx.xxx is the same local machine IP prefix.
				searchedIP = new StringBuffer(ipPrefix);
				searchedIP.append(i);
				
				// Busy Wait to allocate a new thread
				while (!isThreadAllocated){
					//Searches a Free Thread to look for the Host.
					for (int j = 0; j < threadList.size(); j++) {
						// if there iterated thread has finished..
						if (!threadList.get(j).isAlive()){
							// creates a new thread to run in that slot.
							threadList.set(j, createAndStartThreadedPing(ipPrefix+i));
							isThreadAllocated = true;
							break;
						}
					}
				}
			}
			// Wait untill all threads has finished
			waitFinishingAllthreads(threadList);
			// Notifies the listener the finish of discovery. A list of all hosts is returned
			listener.deviceDiscoveryFinished(new Vector<String>(neighbourIpVector));
			neighbourIpVector.clear();
		}
	}

	/**
	 * Busy Wait - Wait untill all threads has finished
	 * @param threadList
	 */
	private void waitFinishingAllthreads(List<ThreadedPing> threadList) {
//		boolean isAllThreadFinished = false;
//		// Busy Wait
//		while (!isAllThreadFinished){
//			isAllThreadFinished = true;
			for (int j = 0; j < threadList.size(); j++) {
				if (threadList.get(j).isAlive()){ // is thread alive?
//					isAllThreadFinished = false;
					try {
						threadList.get(j).join();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
//		}
	}
	

	/**
	 * Pings a IP. Creates a ThreadedPing to Ping the Host
	 * @param string
	 */
	private ThreadedPing createAndStartThreadedPing(String host) {
		ThreadedPing t= new ThreadedPing(host, this);
		t.start();
		return t;
	}
	
	/* *****************************
	 *   	PROTECTED PING METHODS - Called by ThreadedPing
	 * *****************************/
	
	protected void deviceDiscoveredByPingThread(String host){
		// check if the host isnt duplicated
		if (!neighbourIpVector.contains(host)){
			// keep a record of the found host in the local set
			this.neighbourIpVector.add(host);
			//notifies the listener
			listener.deviceDiscovered(host);
		}
	}

	/* *****************************
	 *   	PRIVATE METHODS - ARP
	 * *****************************/
    
	/**
	 * Discover devices in the network accessing the ARP table of the OS
	 */
	private void discoverDevicesUsingArp() {
		try {
			Process p;
			// Invokes a OS Native command "arp -a" to get all registers in the arp table 
			p = Runtime.getRuntime().exec("arp -a");
			// read the command output
			Set<String> neighbourIpVector = this.readResult(p.getInputStream());
			// notifies the listener the end fo the search
			listener.deviceDiscoveryFinished(new Vector<String>(neighbourIpVector));
			p.destroy();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads the "arp -a" command result a searches for a IP on them. 
	 * for each IP found, the listener is notified.
	 * At the end, a vector of all found devices is returned.
	 * @param in
	 */
	private Set<String> readResult(InputStream in){
		
		
		// A Regex do find a "IP" number "xxx.xxx.xxx.xxx"
		String regex = "\\d?\\d?\\d[.]\\d?\\d?\\d[.]\\d?\\d?\\d[.]\\d?\\d?\\d";  
		// Creates a pattern for the Regex above
		Pattern pattern = Pattern.compile( regex );  
		
		String line = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		try {
			// Read every line returned from the command
			while ((line = br.readLine()) != null)
			{
				line = line + "\n";
				// Crates a matcher to search the pattern on the read line
				Matcher matcher = pattern.matcher(line);
				// searches for the pattern on the line
				while (matcher.find()){
					// Gets the IP (found pattern)
					String foundIP = matcher.group();
					if (!line.contains("incomplete") && !foundIP.equals(this.getLocalMachineHostAddresses())){
						// Stores the IP on the local repository
						neighbourIpVector.add(foundIP);
						// Notifies the listener
						listener.deviceDiscovered(foundIP);
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				br.close();
			}
			catch (IOException e) {
			}
		}
		return neighbourIpVector;
	}
}
