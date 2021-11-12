package org.training.campus.networking;

import static java.lang.Math.min;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Server implements Runnable {
	private static final int BUFFER_SIZE = 1024;

	private final int ordinal;
	private final int port;
	private volatile boolean proceed = true;

	public Server(int order, int port) {
		this.ordinal = order;
		this.port = port;
	}

	public void terminate() {
		proceed = false;
	}

	@Override
	public void run() {
		System.out.printf("server #%d started.%n", ordinal);
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			do {
				Socket socket = serverSocket.accept();
				new RequestHandler(socket).start();
			} while (proceed && !Thread.interrupted());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.printf("server #%d shutdown.%n", ordinal);
	}

	private class RequestHandler extends Thread {
		private final Socket socket;

		private RequestHandler(Socket socket) {
			this.socket = socket;
			setDaemon(true);
		}

		@Override
		public void run() {
			try (InputStream is = socket.getInputStream(); OutputStream os = socket.getOutputStream()) {
				byte[] buffer = new byte[BUFFER_SIZE];
				while (proceed && !Thread.interrupted()) {
					int size = is.read(buffer, 0, min(BUFFER_SIZE, is.available()));
					if (size > 0) {
						String reply = String.format("server #%d (%s): %s%n", ordinal,
								DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.now()), new String(buffer, 0, size));
						os.write(reply.getBytes());
						os.flush();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
