package il.ac.tau.cs.hanukcoin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.stream.Collectors;


/**
 * Created by talfranji on 17/02/2020.
 */
class ServerSimpleThreads {
	protected static int accepPort = 8080;

	public static void main(String[] argv) {
		if (argv.length > 0) {
			// allow changing accept port
			accepPort = Integer.parseInt(argv[0]);
		}
		ServerSimpleThreads server = new ServerSimpleThreads();
		try {
			server.runServer();
		} catch (InterruptedException e) {
			// Exit - Ctrl -C pressed
		}
	}

	public void runServer() throws InterruptedException {
		ServerSocketChannel acceptSocket = null;
		try {
			acceptSocket = ServerSocketChannel.open();
			acceptSocket.socket().bind(new InetSocketAddress(accepPort));
		} catch (IOException e) {
			System.out.printf("ERROR accepting at port %d%n", accepPort);
			return;
		}

		while (true) {
			SocketChannel connectionSocket = null;
			try {
				connectionSocket = acceptSocket.accept(); // this blocks
				if (connectionSocket != null) {
					new ClientConnection(connectionSocket.socket(), true).runInThread();
				}
			} catch (IOException e) {
				System.out.printf("ERROR accept:\n  %s%n", e);
				continue;
			}
		}
	}

	class ClientConnection {
		Socket connectionSocket;
		private DataInputStream dataInput;
		private DataOutputStream dataOutput;
		private boolean isIncoming = false;

		public ClientConnection(Socket connectionSocket, boolean incoming) {
			isIncoming = incoming;
			this.connectionSocket = connectionSocket;
			try {
				dataInput = new DataInputStream(connectionSocket.getInputStream());
				dataOutput = new DataOutputStream(connectionSocket.getOutputStream());
			} catch (IOException e) {
				// connectionThread would fail and kill thread
			}
		}

		public void runInThread() {
			new Thread(new Runnable() {
				@Override
				public void run() {
					// Note - in here we are in:
					// class ServerSimpleThread (parent)
					// class ClientConnection (dynamic inner class)
					// class annonymous_runnable (dynamic inner child of ClientConnection)
					// Here I call my parent instance of ClientConnection
					ClientConnection.this.connectionThread();
				}
			}).start();
		}

		private void connectionThread() {
			//This function runs in a separate thread to handle the connection
			try {
				boolean firstLine = true;
				while (true) {
					String line = dataInput.readLine();  // This is blocking
					if (firstLine) {
						firstLine = false;
						if (!line.startsWith("GET / ")) {
							return;
						}
					}
					System.out.printf("Http header: %s%n", line);
					if (line.length() == 0) {
						break;
					}
				}
				sendHtml();
			} catch (IOException e) {
			} finally {
				try {
					connectionSocket.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

		protected void sendHtml() throws IOException {
			String html = String.format("<html><body>I am alive<br/>" + "I am thread-based server connection:%s" + "</body></html>\r\n", this);
			int contentLen = html.length();
			HashMap<String, String> header = new HashMap<>();
			header.put("Content-Length", Integer.valueOf(contentLen).toString());
			header.put("Content-Type", "text/html");
			header.put("Connection", "Closed");
			String responseLine = "HTTP/1.1 200 OK\r\n";
			String headerText = header.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("\r\n"));
			String headerFull = responseLine + headerText + "\r\n\r\n";
			System.out.println(headerFull);
			String response = headerFull + html;
			dataOutput.write(response.getBytes(StandardCharsets.UTF_8));
			dataOutput.flush();
		}
	}
}