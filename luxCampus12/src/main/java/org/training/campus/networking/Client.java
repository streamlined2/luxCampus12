package org.training.campus.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Client implements Runnable {
	private static final int MSG_COUNT = 1000;
	private static final long SLEEP_TIME = 1000;

	private final int ordinal;
	private final String message;
	private InetAddress serverAddress;
	private final int port;

	public Client(int ordinal, String message, InetAddress serverAddress, int port) {
		this.ordinal = ordinal;
		this.message = message;
		this.serverAddress = serverAddress;
		this.port = port;
	}

	@Override
	public void run() {
		try (Socket socket = new Socket(serverAddress, port)) {
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			for (int msgCount = MSG_COUNT; msgCount > 0; msgCount--) {
				String stimulus = String.format("client %d, time %s: %s", ordinal,
						DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()), message);
				os.write(stimulus.getBytes());
				os.flush();
				Thread.sleep(SLEEP_TIME);
				String reply = new String(is.readAllBytes());
				System.out.println(reply);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
