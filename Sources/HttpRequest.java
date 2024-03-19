import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private eRequestType RequestType;
    private Boolean IsChunked;
    private int ContentLength;
    private String RequestedPage;

    public int getContentLength() {return ContentLength;}

    public Boolean isChunked() {return IsChunked;}
    public String getRequestedPage() {return RequestedPage;}
    public eRequestType getRequestType() {return RequestType;}

    public HttpRequest(){
        this.IsChunked = false;
    }

    public HttpRequest(String request)throws IOException{
        parseRequest(request);
    }

    private void parseRequest(String request) throws IOException{
        request = request.replaceAll("\\.\\./", "\\./");
        String method = request.split("[ \n]+")[0].replaceAll("\\s+$", "");
        this.RequestType = eRequestType.valueOf(method);
        this.extractHeaderValues(request);

        String requestedPage = request.split("\r\n")[0].split(" ").length > 1 ? request.split("\r\n")[0].split(" ")[1].split("\\?")[0].replaceAll("\\s+$", "") : "";
        if(requestedPage.isEmpty() || requestedPage.equals("/"))
        {
            requestedPage = HttpServer.getDefaultPage().toString();
        }

        System.out.println("Requested Page: " + requestedPage);
        this.RequestedPage = HttpServer.getRootDirectory() + requestedPage;
        System.out.println("Requested Page: " + this.RequestedPage);
        if (!Files.exists(Paths.get(this.RequestedPage))){
            throw new FileNotFoundException("Requested Page doesn't exist");
        }

        if(this.RequestType == eRequestType.POST || this.RequestType == eRequestType.GET)
        {
            Map<String, String> map = new HashMap<String, String>();
            parseURLParams(request, map);
            if (this.RequestType == eRequestType.POST && this.ContentLength > 0){
                parseBodyParams(request, map);
            }

            if (!map.isEmpty())
            {
                HttpServer.InsertData(map);
            }
        }
    }

    private void parseBodyParams(String request, Map<String, String> map) {
        String[] requestLines = request.split("\\r\\n");
        String paramString = requestLines[requestLines.length - 1];
        String[] paramPairs = paramString.split("&"); // Split into key-value pairs
        insertData(paramPairs, map);
    }

    private void parseURLParams(String request, Map<String, String> map){
        if (request.split(" ").length > 1 && request.split(" ")[1].split("\\?").length > 1) {
            String paramString = request.split(" ")[1].split("\\?")[1].split("\\r\\n")[0]; // Get the part after '?'
            String[] paramPairs = paramString.split("&"); // Split into key-value pairs
            insertData(paramPairs, map);
        }
    }

    private void insertData(String[] paramPairs, Map<String, String> map)
    {
        for (String pair : paramPairs) {
            String[] keyValue = pair.split("=", 2); // Limit split to 2 in case value contains '='
            if (keyValue.length == 2) { // Ensure there's a key and a value
                // Decode the parameters to handle encoded characters like spaces (%20)
                String key = keyValue[0];
                String value = keyValue[1];
                map.put(key, value); // Add to map
            }
        }
    }

    private void extractHeaderValues(String request) throws IOException {
        this.ContentLength = -1;
        this.IsChunked = false;
        String[] lines = request.split("\n");
        for (String line : lines) {
            line = line.toLowerCase();
            if (line.startsWith("content-length:")) {
                String value = line.substring("content-length:".length()).trim();
                try {
                    this.ContentLength = Integer.parseInt(value);
                    // Ensure contentLength is not negative
                    if (this.ContentLength < 0) {
                        throw new IOException("negative content length not allowed");
                    }
                    break; // Exit the loop once a valid Content-Length is found
                } catch (NumberFormatException e) {
                    throw new IOException("content length expected to be a positive number");
                }
            }
            else if (line.startsWith("chunked:")){
                String value = line.substring("chunked:".length()).trim();
                if (!value.equals("yes") && !value.equals("no")){
                    throw new IOException("invalid chunked value");
                }
                else{
                    this.IsChunked = value.equals("yes");
                }
            }
        }
    }


}
