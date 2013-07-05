package br.unb.cic.ethutil;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

	private Logger logger = Logger.getLogger(Server.class.getName());
	private int port;
	private Set<Client> clientPool = new HashSet<Client>();
	private ServerSocket serverSocket;
	

	public Server(int port) throws UnknownHostException, IOException {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			logger.log(Level.INFO, "Could not listen on port: " + port);
			throw e;
		}
	}

	public Client wait4Client() throws IOException {
		logger.log(Level.INFO, "Server Initialized");
		Socket clientSocket = null;
		try {
			clientSocket = serverSocket.accept();
			Client client = new Client(clientSocket);
			clientPool.add(client);
			return client;
		} catch (IOException e) {
			logger.log(Level.INFO, "Accept failed: " + port);
			throw e;
		}
	}

	public Set<Client> getClientPool() {
		return clientPool;
	}

	public void close() throws IOException {
		serverSocket.close();
	}

}
