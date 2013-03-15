package br.unb.cic.ethutil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Client {

	Socket socket;

	private OutputStream outputStream;

	private InputStream inputStream;

	private BufferedWriter writer;

	private BufferedReader reader;

	Client(Socket socket) throws IOException {
		this.socket = socket;
		outputStream = socket.getOutputStream();
		inputStream = socket.getInputStream();
		writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		reader = new BufferedReader(new InputStreamReader(inputStream));
	}

	public void close() throws IOException {
		writer.close();
		reader.close();
		outputStream.close();
		inputStream.close();
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public BufferedWriter getWriter() {
		return writer;
	}

	public BufferedReader getReader() {
		return reader;
	}

}