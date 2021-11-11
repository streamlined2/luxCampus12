package org.training.campus.networking;

import static java.lang.Math.min;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Server implements Runnable {
	private static final int BUFFER_SIZE = 1024;
	private static final long SLEEP_TIME = 1000;

	private final int order;
	private final int port;

	public Server(int order, int port) {
		this.order = order;
		this.port = port;
	}

	@Override
	public void run() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			try (Socket socket = serverSocket.accept()) {
				byte[] buffer = new byte[BUFFER_SIZE];
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				while (!Thread.interrupted()) {
					int size = is.read(buffer, 0, min(BUFFER_SIZE, is.available()));
					if (size != -1) {
						String reply = String.format("server %d echoed at %s: %s", order,
								DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()),
								new String(buffer, 0, size));
						os.write(reply.getBytes());
					}
					Thread.sleep(SLEEP_TIME);
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
