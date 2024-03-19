import java.nio.charset.StandardCharsets;

public class BodyFactory {
    public static byte[] GetPredefinedBody(int statusCode){
        String body = "";
        switch (statusCode){
            case 400:
                 body = "<html><head><title>400 Bad Request</title></head>"
                        + "<body><h1>400 Bad Request</h1>"
                        + "<p>Bad Request</p></body></html>";
                 break;
            case 404:
                body = "<html><head><title>404 Not Found</title></head>"
                        + "<body><h1>404 Not Found</h1>"
                        + "<p>The resource you are looking for might have been removed, had its name changed, or is temporarily unavailable.</p></body></html>";
                break;
            case 500:
                body = "<html><body><h1>500 Server Error</h1><p>Internal Server Error</p></body></html>";
                break;
            case 501:
                body = "<html><head><title>501 Not Implemented</title></head>"
                        + "<body><h1>501 Not Implemented</h1>"
                        + "<p>Not Implemented</p></body></html>";
                break;

        }

        return body.getBytes(StandardCharsets.UTF_8);
    }
}
