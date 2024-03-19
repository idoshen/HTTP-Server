import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class SenderUtils {
    public static void SendHeader(HttpResponse response) throws IOException{
        StringBuilder header = new StringBuilder();
        header.append("HTTP/1.1 " + response.getStatusCode() + " " + getStatusText(response.getStatusCode()));
        header.append(System.lineSeparator());
        header.append("Content-Type: " + response.getContentType());
        header.append(System.lineSeparator());

        int headerLength = response.getBody().length;
        header.append("Content-Length: " + headerLength);
        header.append(System.lineSeparator());
        //send the headers
        System.out.println("Server response: \r\n" + header);

        response.getOutput().write(header.toString().getBytes());
        response.getOutput().write("\r\n".getBytes(StandardCharsets.UTF_8));
        response.getOutput().flush();
    }

    private static String getStatusText(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 400:
                return "Bad Request";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            case 501:
                return "Not Implemented";
            default:
                return "Unknown";
        }
    }

    public static void sendChunkedResponse(HttpResponse response) throws IOException {
        OutputStream out = response.getOutput();

        int chunkSize = 1024; // Define a chunk size

        int start = 0;
        while (start < response.getBody().length) {
            // Determine the size of the current chunk
            int end = Math.min(response.getBody().length, start + chunkSize);
            int currentChunkSize = end - start;

            // Send the size of the current chunk in hexadecimal
            String sizeHeader = Integer.toHexString(currentChunkSize) + "\r\n";
            out.write(sizeHeader.getBytes(StandardCharsets.UTF_8));
            // Send the current chunk of the response body
            out.write(response.getBody(), start, currentChunkSize);
            out.write("\r\n".getBytes(StandardCharsets.UTF_8)); // End of the chunk

            // Move to the next chunk
            start = end;
        }

        // Send a zero-length chunk to indicate the end of the response
        out.write("0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public static void SendResponse(HttpResponse response) {
        try {
            SenderUtils.SendHeader(response);

            if (!response.isHeaderOnly()) {

                if (response.isChunked()) {
                    sendChunkedResponse(response);
                } else {
                    response.getOutput().write(response.getBody());
                    response.getOutput().write("\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                    response.getOutput().flush();
                }
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            //todo handle io error
            System.out.println("error sending image");
        }
    }
}
