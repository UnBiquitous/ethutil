package br.unb.cic.ethutil;

/**
 *  THIS CLASS WAS GET AT - http://www.jguru.com/faq/view.jsp?EID=735679 - IT'S A RESPONSE
 *  FOR A SOCKET CONSTRUCTOR PROBLEM FOUND IN SOME JVM VERSIONS 
 *  (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5092063)
 */

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;

/**
 * This class offers a timeout feature on socket connections.
 * A maximum length of time allowed for a connection can be
 * specified, along with a host and port.
 */
class TimedSocket
{
	/**
	 * Attempts to connect to a service at the specified address
	 * and port, for a specified maximum amount of time.
	 *
	 *	@param	addr	Address of host
	 *	@param	port	Port of service
	 * @param	delay	Delay in milliseconds
	 */
	public static Socket getSocket(String host, int port, int delay)
	throws Exception
	{
		Socket socket = null;
		try
		{
			// Create a new socket thread, and start it running
			SocketThread st = new SocketThread(host, port );
			st.start();
			//wait for the SocketThread to finish or exceed its alloted time
			st.join(delay);
			if (st.isConnected())
			{
				// Yes ...  assign to socket variable, and break out of loop
				socket = st.getSocket();
				return socket;
			}
			else if (st.isError())
			{
				// No connection could be established
				throw st.getException();
			}
			else // must have timed out
			{
				st.setDoneWaiting(new InterruptedIOException("Could not connect for " + delay + " milliseconds"));
				//make the thread trying to open the socket stop
				st.interrupt();
				throw st.getException();
			}	//end if socket open timed out
		}	//end try
		catch (Exception outerEx)
		{
			throw outerEx;
		}
	}

	// Inner class for establishing a socket thread
	// within another thread, to prevent blocking.
	static class SocketThread extends Thread
	{
		// Socket connection to remote host
		volatile private Socket m_connection = null;
		volatile private boolean doneWaiting = false;
		// Hostname to connect to
		private String m_host       = null;
		// Port number to connect to
		private int    m_port       = 0;
		// Exception in the event a connection error occurs
		private IOException m_exception = null;
		
		// Connect to the specified host and port number
		public SocketThread( String host, int port)
		{
			// Assign to member variables
			m_host = host;
			m_port = port;
		}
		
		public void setDoneWaiting(InterruptedIOException ioe)
		{
			doneWaiting = true;
			m_exception = ioe;
		}
		
		public void run()
		{
			// Socket used for establishing a connection
			m_connection = null;
			
			try
			{
				// Connect to a remote host - BLOCKING I/O
				m_connection = new Socket(m_host, m_port);
			}
			catch (IOException ioe)
			{
				// Assign to our exception member variable
				m_exception = ioe;
			}	//end if exception
			
			if (doneWaiting || isError())
			{
				//if the caller got tired of waiting then
				//immediately close the socket since the caller is no longer
				//waiting for the socket.
				//If there was an error but the socket was still somehow established also
				//close it
				if (m_connection != null)
				{
					try
					{
						m_connection.close();
						m_connection = null;
					}
					catch (IOException e)
					{
					}
				}
			}	//end if done waiting or caught an exception
		}	//end run
		
		// Are we connected?
		public boolean isConnected()
		{
			if (m_connection == null)
				return false;
			else
				return true;
		}
		
		// Did an error occur?
		public boolean isError()
		{
			if (m_exception == null)
				return false;
			else
				return true;
		}
		
		// Get socket
		public Socket getSocket()
		{
			return m_connection;
		}
		
		// Get exception
		public IOException getException()
		{
			return m_exception;
		}
	}	//end SocketThread inner class
	

}	//end TimedSocket class
