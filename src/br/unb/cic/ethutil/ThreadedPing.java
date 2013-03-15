package br.unb.cic.ethutil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class implements a thread for pinging a specified host.
 * I have two ways to Ping: Using Default Java Ping method, or using Native OS inkove command
 * @author Passarinho
 *
 */
class ThreadedPing extends Thread{
	
	/* *****************************
	 *   	ATRIBUTES
	 * *****************************/
	
	// Command for ping sending 3 packets in MS Windows.
	private static final String pingCmdMS = "ping -n 3 ";
	// Command for ping sending 3 packets in UNIX/LINUX/MacOSX.
	private static final String pingCmdUnix = "ping -c 3 ";
	// Flag tho identify if the system OS is Windows or Not.
	private boolean isMS = false;
	
	// The Host who will be pinged.
	String host;
	// Caller Class, used to notify the discvered host
	EthUtil ethUtil;
	
	
	/* *****************************
	 *   	CONSTRUCTOR
	 * *****************************/
	
	/**
	 * costructor passing the host to be pinged and the ethutil class to be notify
	 */
	public ThreadedPing(String host, EthUtil ethUtil){
		this.host = host;
		this.ethUtil = ethUtil;
		//Gets the OS Name to check know with ping command(Win or Unix/OSX) use.
		String os = System.getProperty("os.name");
		if (os.indexOf("Windows") != -1) {
			this.isMS = true;
		}
	}
	
	/* *****************************
	 *   	THREAD METHOD
	 * *****************************/
	
	/**
	 * Run method of the thread
	 */
	public void run(){
		
		try {
			//System.out.println("Searching: "+host+" - "+this.getId()+" | "+this.getName()+" |"+ this.getState());
			// Ping using Native OS Command
			pingUsingNativeOS();
			// Native OS Command had better results than Java Ping during tests, So We will use the first.
			//pingUsingJava();
		} catch (Exception e) {
		}
	}


	/* *****************************
	 *   	OS NATIVE PING METHOD
	 * *****************************/
	private void pingUsingNativeOS() throws IOException {
		Process p;
		// Invokes Native OS Command ping according to OS.
		if (isMS){
			p = Runtime.getRuntime().exec(pingCmdMS+host);
		}else{
			p = Runtime.getRuntime().exec(pingCmdUnix+host);
		}
		// Watches the output.
		this.readResult(p.getInputStream());
		// kill the process.
		p.destroy();
	}
	
	/**
	 * Method to read the result of native ping  
	 * @param in
	 */
	private void readResult (InputStream in){
		String line = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		try {
			//read each line
			while ((line = br.readLine()) != null){
				line = line+"\n";
				// If TTL was printed, the host was reached! :P
				if (line.contains("TTL=") || line.contains("ttl=")){
					//System.out.println("THREADED PING - Found Host:" + host);
					// Notifies ther EthUtil class
					ethUtil.deviceDiscoveredByPingThread(host);
				}
			}	
		}
		catch (IOException e) {}
		finally {
			try {
				br.close();
			} catch (IOException e) {}
		}
	}
	
	/* *****************************
	 *   	JAVA PING METHOD
	 * *****************************/
	
	/**
	 * Method to ping using Java method 
	 */
	private void pingUsingJava() throws IOException, UnknownHostException {
		if (InetAddress.getByName(host).isReachable(5000)){
			//System.out.println("THREADED PING - Found Host:" + host);
			ethUtil.deviceDiscoveredByPingThread(host);
		}
	}
	
} 

