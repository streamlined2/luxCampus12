package org.training.campus.networking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Client implements Runnable, Terminable {
	private static final int MESSAGE_COUNT = 100;
	private static final long SLEEP_TIME = 100;
	private static final long START_SHIFT_TIME = 500;
	private static final int BUFFER_SIZE = 1024;

	private final int ordinal;
	private final String message;
	private final InetAddress serverAddress;
	private final int port;
	private volatile boolean proceed = true;

	public Client(int ordinal, String message, InetAddress serverAddress, int port) {
		this.ordinal = ordinal;
		this.message = message;
		this.serverAddress = serverAddress;
		this.port = port;
	}

	@Override
	public boolean isRunning() {
		return proceed;
	}

	@Override
	public void terminate() {
		proceed = false;
	}

	@Override
	public void run() {
		System.out.printf("client #%d (%s:%d) started.%n", ordinal, serverAddress.toString(), port);
		try {
			Thread.sleep(START_SHIFT_TIME * ordinal);
			try (Socket socket = new Socket(serverAddress, port);
					PrintWriter writer = new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(socket.getOutputStream(), Runner.CURRENT_CHARSET), BUFFER_SIZE));
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(socket.getInputStream(), Runner.CURRENT_CHARSET), BUFFER_SIZE)) {
				for (int msgCount = 0; msgCount < MESSAGE_COUNT && isRunning() && !Thread.interrupted(); msgCount++) {
					String stimulus = String.format("client #%d (%s): %s (%d)", ordinal,
							DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.now()), message, msgCount);
					System.out.println(stimulus);
					writer.println(stimulus);
					writer.flush();
					while (reader.ready()) {
						System.out.println(reader.readLine());
					}
					Thread.sleep(SLEEP_TIME);
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new CommunicationException(e);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		System.out.printf("client #%d shutdown.%n", ordinal);
	}

}
