package org.training.campus.networking;

import static java.lang.Math.min;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Server implements Runnable, Terminable {
	private static final int ACCEPT_TIMEOUT = 1000;
	private static final int BUFFER_SIZE = 1024;

	private final int ordinal;
	private final int port;
	private volatile boolean proceed = true;
	private int handlerCount = 0;
	private ServerSocket serverSocket;

	public Server(int ordinal, int port) {
		this.ordinal = ordinal;
		this.port = port;
	}

	@Override
	public boolean isRunning() {
		return proceed;
	}

	@Override
	public void terminate() {
		proceed = false;
		closeSocket();
	}

	private void closeSocket() {
		if (serverSocket != null) {
			try {
				serverSocket.close();
				serverSocket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		System.out.printf("server #%d started.%n", ordinal);
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(ACCEPT_TIMEOUT);
			while (isRunning() && !Thread.interrupted()) {
				handleRequest();
			}
		} catch (IOException e) {
			if (isRunning()) {
				e.printStackTrace();
				throw new CommunicationException(e);
			}
		} finally {
			closeSocket();
		}
		System.out.printf("server #%d shutdown.%n", ordinal);
	}

	private void handleRequest() throws IOException {
		try {
			Socket socket = serverSocket.accept();
			new RequestHandler(socket, handlerCount++).start();
		} catch (SocketTimeoutException e) {
			// break loop if execution should be stopped after timeout exceeds
		}
	}

	private class RequestHandler extends Thread {
		private final Socket socket;
		private final int no;

		private RequestHandler(Socket socket, int no) {
			this.socket = socket;
			this.no = no;
		}

		@Override
		public void run() {
			System.out.printf("handler #%d of server #%d started.%n", no, ordinal);
			try (InputStream is = socket.getInputStream(); OutputStream os = socket.getOutputStream()) {
				byte[] buffer = new byte[BUFFER_SIZE];
				while (isRunning() && !Thread.interrupted()) {
					int size = is.read(buffer, 0, min(BUFFER_SIZE, is.available()));
					if (size > 0) {
						String reply = String.format("server #%d, handler #%d (%s): %s%n", ordinal, no,
								DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.now()), new String(buffer, 0, size));
						os.write(reply.getBytes());
						os.flush();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new CommunicationException(e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.printf("handler #%d of server #%d shutdown.%n", no, ordinal);
		}
	}

}
