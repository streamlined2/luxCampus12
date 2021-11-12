package org.training.campus.networking;

import java.net.InetAddress;

public class Runner {
	private static final String CLIENT_MESSAGE = "Hello!";
	private static final int SERVER_COUNT = 1;
	private static final int CLIENT_COUNT = 3;
	private static final int FIRST_SERVER_PORT = 4444;
	private static final long WORKING_TIME = 10_000;
	private static final InetAddress SERVER_ADDRESS = InetAddress.getLoopbackAddress();

	public static void main(String[] args) throws InterruptedException {
		System.out.printf("Simulation started with %d servers and %d clients.%n", SERVER_COUNT, CLIENT_COUNT);

		Thread[] servers = startServers();
		Thread[] clients = startClients(SERVER_COUNT);

		Thread.sleep(WORKING_TIME);

		interruptAll(servers);
		interruptAll(clients);

		waitForAll(servers);
		waitForAll(clients);

		System.out.println("Simulation stopped.");
	}

	private static Thread[] startClients(int serverCount) {
		Thread[] clients = new Thread[CLIENT_COUNT];
		for (int k = 0; k < CLIENT_COUNT; k++) {
			Client client = new Client(k, CLIENT_MESSAGE, SERVER_ADDRESS, FIRST_SERVER_PORT + k % serverCount);
			clients[k] = new Thread(client);
			clients[k].start();
		}
		return clients;
	}

	private static Thread[] startServers() {
		Thread[] servers = new Thread[SERVER_COUNT];
		for (int k = 0; k < SERVER_COUNT; k++) {
			Server server = new Server(k, FIRST_SERVER_PORT + k);
			servers[k] = new Thread(server);
			servers[k].start();
		}
		return servers;
	}

	private static void interruptAll(Thread[] threads) {
		for (int k = 0; k < threads.length; k++) {
			threads[k].interrupt();
		}
	}

	private static void waitForAll(Thread[] threads) throws InterruptedException {
		for (int k = 0; k < threads.length; k++) {
			threads[k].join();
		}
	}

}
