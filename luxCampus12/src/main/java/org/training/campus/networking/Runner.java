package org.training.campus.networking;

import java.net.InetAddress;

public class Runner {
	private static final String CLIENT_MESSAGE = "Hello!";
	private static final int SERVER_COUNT = 1;
	private static final int CLIENT_COUNT = 5;
	private static final int FIRST_SERVER_PORT = 4444;
	private static final long WORKING_TIME = 20_000;
	private static final InetAddress SERVER_ADDRESS = InetAddress.getLoopbackAddress();

	public static void main(String[] args) {
		System.out.printf("Simulation started with %d servers and %d clients.%n", SERVER_COUNT, CLIENT_COUNT);

		Executable[] servers = startServers();
		Executable[] clients = startClients();

		try {
			System.out.println("Working...");
			Thread.sleep(WORKING_TIME);

			System.out.println("Terminating clients...");
			terminateWait(clients);
			System.out.println("Terminating servers...");
			terminateWait(servers);

			System.out.println("Simulation stopped.");

		} catch (InterruptedException e) {
			System.out.println("Simulation failed.");
			e.printStackTrace();
		}

	}

	private static Executable[] startClients() {
		Executable[] clients = new Executable[CLIENT_COUNT];
		for (int k = 0; k < CLIENT_COUNT; k++) {
			Client client = new Client(k, CLIENT_MESSAGE, SERVER_ADDRESS, FIRST_SERVER_PORT + k % SERVER_COUNT);
			clients[k] = new Executable(new Thread(client), client);
			clients[k].thread().start();
		}
		return clients;
	}

	private static Executable[] startServers() {
		Executable[] servers = new Executable[SERVER_COUNT];
		for (int k = 0; k < SERVER_COUNT; k++) {
			Server server = new Server(k, FIRST_SERVER_PORT + k);
			servers[k] = new Executable(new Thread(server), server);
			servers[k].thread().start();
		}
		return servers;
	}

	private static void terminate(Executable[] execs) {
		for (Executable ex : execs) {
			ex.terminable().terminate();
		}
		for (Executable ex : execs) {
			ex.thread().interrupt();
		}
	}

	private static void waitFor(Executable[] execs) {
		for (Executable ex : execs) {
			try {
				ex.thread().join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void terminateWait(Executable[] execs) {
		terminate(execs);
		waitFor(execs);
	}

}
