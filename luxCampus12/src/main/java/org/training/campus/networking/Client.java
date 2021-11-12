package org.training.campus.networking;

import static java.lang.Math.min;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Client implements Runnable {
	private static final int MSG_COUNT = 5;
	private static final long SLEEP_TIME = 100;
	private static final int BUFFER_SIZE = 1024;
	private static final long START_SHIFT_TIME = 5000;

	private final int ordinal;
	private final String message;
	private final InetAddress serverAddress;
	private final int port;

	public Client(int ordinal, String message, InetAddress serverAddress, int port) {
		this.ordinal = ordinal;
		this.message = message;
		this.serverAddress = serverAddress;
		this.port = port;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[BUFFER_SIZE];
		try {
			Thread.sleep(START_SHIFT_TIME * ordinal);
			System.out.printf("client #%d (%s:%d) started.%n", ordinal, serverAddress.toString(), port);
			try (Socket socket = new Socket(serverAddress, port);
					OutputStream os = socket.getOutputStream();
					InputStream is = socket.getInputStream()) {
				for (int msgCount = MSG_COUNT; msgCount > 0 && !Thread.interrupted(); msgCount--) {
					String stimulus = String.format("client #%d (%s): %s", ordinal,
							DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.now()), message);
					System.out.println(stimulus);
					os.write(stimulus.getBytes());
					os.flush();
					int size = is.read(buffer, 0, min(BUFFER_SIZE, is.available()));
					if (size > 0) {
						String reply = new String(buffer, 0, size);
						System.out.println(reply);
					}
					Thread.sleep(SLEEP_TIME);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.printf("client #%d shutdown.%n", ordinal);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
