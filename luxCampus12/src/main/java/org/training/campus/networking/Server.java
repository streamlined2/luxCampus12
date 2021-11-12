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
	private static final long SLEEP_TIME = 0;

	private final int ordinal;
	private final int port;

	public Server(int order, int port) {
		this.ordinal = order;
		this.port = port;
	}

	@Override
	public void run() {
		System.out.printf("server #%d started.%n", ordinal);
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			do {
				try (Socket socket = serverSocket.accept();
						InputStream is = socket.getInputStream();
						OutputStream os = socket.getOutputStream()) {
					byte[] buffer = new byte[BUFFER_SIZE];
					while (!Thread.interrupted()) {
						int size = is.read(buffer, 0, min(BUFFER_SIZE, is.available()));
						if (size > 0) {
							String reply = String.format("server #%d (%s): %s%n", ordinal,
									DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.now()),
									new String(buffer, 0, size));
							os.write(reply.getBytes());
							os.flush();
						}
						Thread.sleep(SLEEP_TIME);
					}
				}
			} while (!Thread.interrupted());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		System.out.printf("server #%d shutdown.%n", ordinal);
	}

}
