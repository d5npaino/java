import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class Server {
    private static ExecutorService executor;
    private static Map<String, AtomicInteger> votes = new ConcurrentHashMap<>();
    private static final AtomicInteger activeClients = new AtomicInteger(0);
    private static final int clients = 30; // max number of clients
    private static final int port = 7777;
    private static final String file = "log.txt";

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("~# ERROR : Missing Arguments [At least 2 Required] #~");
            return;
        }
        for (String option : args) {
            votes.put(option, new AtomicInteger(0)); // catching the vote argument inputted
        }
        executor = Executors.newFixedThreadPool(clients);
        try (ServerSocket sServer = new ServerSocket(port)) {
            while (true) { // continuously running server
                Socket sClient = sServer.accept();
                ClientThread thread = new ClientThread(sClient);
                thread.start(); // new thread
            }
        } catch (IOException e) {
            System.out.println("# ERROR : Couldn't Connect Successfully to the Server #~");
            return;
        } finally {
            executor.shutdown();
        }
    }

    private static class ClientThread extends Thread {
        private Socket socket;

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            if (activeClients.get() >= clients) {
                System.out.println("# ERROR : Maximum Clients Connected, Please Try Again Later #~");
                try {
                    socket.close();
                } catch (IOException e) { // better safe than sorry
                    System.out.println("# ERROR : Socket Closure Failure #~");
                    return;
                }
                return;
            }
            activeClients.incrementAndGet();
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true); // chatgpt helped with this, send
                                                                                      // printed response to client
                String request = input.readLine();
                String response = processRequest(request, socket.getInetAddress().getHostAddress());
                output.println(response);
            } catch (IOException e) {
                System.out.println("# ERROR : Request Processing Error #~");
            } finally {
                activeClients.decrementAndGet();
                try {
                    socket.close();
                } catch (IOException e) { // better safe than sorry
                    System.out.println("# ERROR : Socket Closure Failure #~");
                    return;
                }
            }
        }

        private static synchronized String processRequest(String request, String clientIp) {
            if (request == null || request.isEmpty()) {
                return "~# ERROR : Invalid Input #~";
            }
            String[] parts = request.split(" ");
            String command = parts[0];
            switch (command) {
                case "list":
                    logRecorded(clientIp, "list");
                    return votes.entrySet().stream()
                            .map(entry -> "'" + entry.getKey() + "' has " + entry.getValue().get() + " vote(s)")
                            .collect(Collectors.joining("\n")); // used chatgpt to help me with this
                case "vote":
                    if (parts.length != 2)
                        return "~# ERROR : Invalid Vote Format #~";
                    String option = parts[1];
                    return voteRecorded(clientIp, option);
                default:
                    return "~# ERROR : Invalid Input #~";
            }
        }

        private static synchronized String voteRecorded(String clientIp, String option) {
            if (!votes.containsKey(option)) {
                return "~# ERROR : Invalid Vote Option Specified #~";
            }
            votes.get(option).incrementAndGet();
            logRecorded(clientIp, "vote");
            return "Voted: " + option;
        }

        private static synchronized void logRecorded(String clientIp, String requestType) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd|HH:mm:ss").format(new Date()); // chatgpt again
            try {
                BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(file, true));
                bufferWriter.write(timestamp + "|" + clientIp + "|" + requestType);
                bufferWriter.newLine();
                bufferWriter.close();
            } catch (IOException e) {
                System.out.println("# ERROR : Logging Error #~");
                return;
            }
        }
    }
}
