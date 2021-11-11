package org.training.campus.networking;

import java.net.InetAddress;

public class Runner {
	public static final int SERVER_COUNT = 1;
	public static final int CLIENT_COUNT = 1;
	public static final int SERVER_PORT = 3000;
	public static final long WORKING_TIME = 10_000;
	public static final InetAddress LOOPBACK_INTERFACE = InetAddress.getLoopbackAddress();

	public static void main(String[] args) throws InterruptedException {
		Thread[] servers = new Thread[SERVER_COUNT];
		createStartServers(servers);

		Thread[] clients = new Thread[CLIENT_COUNT];
		createStartClients(clients);

		Thread.sleep(WORKING_TIME);

		interruptAll(servers);
		interruptAll(clients);

		joinAll(servers);
		joinAll(clients);

	}

	private static void createStartClients(Thread[] clients) {
		for (int k = 0; k < CLIENT_COUNT; k++) {
			Client client = new Client(k, "hello", LOOPBACK_INTERFACE, SERVER_PORT);
			clients[k] = new Thread(client);
			clients[k].start();
		}
	}

	private static void createStartServers(Thread[] servers) {
		for (int k = 0; k < SERVER_COUNT; k++) {
			Server server = new Server(k, SERVER_PORT);
			servers[k] = new Thread(server);
			servers[k].start();
		}
	}

	private static void interruptAll(Thread[] threads) {
		for (int k = 0; k < threads.length; k++) {
			threads[k].interrupt();
		}
	}

	private static void joinAll(Thread[] threads) throws InterruptedException {
		for (int k = 0; k < threads.length; k++) {
			threads[k].join();
		}
	}

}
