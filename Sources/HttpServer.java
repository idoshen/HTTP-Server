import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private static final Object lock = new Object();
    private static ArrayList<Map<String, String>> Data;
    private int port;

    public static File getRootDirectory() {
        return rootDirectory;
    }

    public static File getDefaultPage() { return defaultPage; }

    private static File rootDirectory;
    private static File defaultPage;
    private ExecutorService threadPool;

    public HttpServer(String configFilePath) {
        Properties config = new Properties();
        try {
            config.load(new FileInputStream(configFilePath));
            this.port = Integer.parseInt(config.getProperty("port"));
            this.rootDirectory = new File(config.getProperty("root").replace("~", System.getProperty("user.home")));
            this.defaultPage = new File(config.getProperty("defaultPage"));
            int maxThreads = Integer.parseInt(config.getProperty("maxThreads"));
            this.threadPool = Executors.newFixedThreadPool(maxThreads);
            this.Data = new ArrayList<Map<String, String>>();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("failure during server configuration");
            System.exit(1);
        }
    }

    public static void InsertData(Map<String, String> i_Data)
    {
        synchronized(lock) {
            Data.add(i_Data);
            System.out.println("Added New Session Request Params:");
            System.out.println(Data.toString());
        }
    }

    public static String generateTableHTML() {
        synchronized(lock) {
            StringBuilder htmlBuilder = new StringBuilder();

            // Start the table and add the header row
            htmlBuilder.append("<table border='1'>") // Adding border for visibility, adjust styling as needed
                    .append("<tr><th>Index</th><th>Key</th><th>Value</th></tr>");

            // Increment index
            int index = 0;

            // Iterate through the list of maps
            for (Map<String, String> paramMap : Data) {
                index++;
                // Iterate through each key-value pair in the current map
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    htmlBuilder.append("<tr>")
                            .append("<td>").append(index).append("</td>") // Increment index for each key-value pair
                            .append("<td>").append(entry.getKey()).append("</td>")
                            .append("<td>").append(entry.getValue()).append("</td>")
                            .append("</tr>");
                }
            }

            // Close the table
            htmlBuilder.append("</table>");

            return htmlBuilder.toString();
        }
    }


    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("HTTP Server started on port " + this.port);

            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket, this.rootDirectory, this.defaultPage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private File rootDirectory;
        private File defaultPage;

        public ClientHandler(Socket clientSocket, File rootDirectory, File defaultPage) {
            this.clientSocket = clientSocket;
            this.rootDirectory = rootDirectory;
            this.defaultPage = defaultPage;
        }

        @Override
        public void run() {
            try {
                // Open input and output streams for the socket
                InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream();
                //PrintWriter writer = new PrintWriter(output, true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                // Read the request line by line
                String line;
                StringBuilder requestBuilder = new StringBuilder();
                int contentLength = 0;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    if (line.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
                    }
                    requestBuilder.append(line + "\r\n");
                }

                // print the request headers
                System.out.println("User request: \n" + requestBuilder);

                // Read body if Content-Length is present
                if (contentLength > 0) {
                    char[] body = new char[contentLength];
                    reader.read(body, 0, contentLength);
                    System.out.println("user request body: \n" + new String(body));
                    requestBuilder.append(new String(body));
                }
                // get the request type
                String serverResponse = "";

                try {
                    HttpResponse.ProcessRequest(output, requestBuilder.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    HttpResponse.serverError(output);
                }
                finally {
                    System.out.println(serverResponse);
                }

                // Always close the client socket and streams after handling the request
                reader.close();
                output.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
