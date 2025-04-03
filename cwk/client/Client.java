import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.text.SimpleDateFormat;

class Client {
	private static final String host = "localhost";
	private static final int port = 7777;

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("~# ERROR : Missing Arguments [At least 1 Required] #~");
			return;
		}
		String command = String.join(" ", args);
		Socket socket = null;
		try {
			socket = new Socket(host, port);
			PrintWriter output = new PrintWriter(socket.getOutputStream(), true); // chatgpt again like mentioned in
																					// server.java
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output.println(command); // server reads this command output as the command and processes it
			String response;
			while ((response = input.readLine()) != null) { // prints servers response to the client
				System.out.println(response);
			}
		} catch (IOException e) {
			System.out.println("# ERROR : Couldn't Connect Successfully to the Server #~");
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) { //better safe than sorry
					System.out.println("# ERROR : Socket Closure Failure #~"); //just becasue of socket socket = null might cause errors
				}
			}
		}
	}
}
