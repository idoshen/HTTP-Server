import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HttpResponse {
    private int StatusCode;
    private ContentTypeHelper.ContentType ContentType;
    private byte[] Body;
    private OutputStream output;
    private Boolean IsHeadOnly;
    private Boolean IsChunked;

    private HttpResponse(int statusCode, ContentTypeHelper.ContentType contentType, byte[] body, HttpRequest request, OutputStream output){
        this.output = output;
        this.StatusCode = statusCode;
        this.ContentType = contentType;
        this.Body = body;

        this.IsHeadOnly = request.getRequestType() == eRequestType.HEAD;
        this.IsChunked = request.isChunked();

        SenderUtils.SendResponse(this);
    }

    public Boolean isChunked() {return IsChunked;}
    public OutputStream getOutput() {return output;}
    public byte[] getBody() {
        return Body;
    }
    public int getStatusCode() {
        return StatusCode;
    }
    public String getContentType() {
        return ContentType.GetDescription();
    }
    public boolean isHeaderOnly() {return IsHeadOnly;}

    private static void notFound(OutputStream output, HttpRequest request) {
        new HttpResponse(404, ContentTypeHelper.ContentType.html, BodyFactory.GetPredefinedBody(404), request,output);
    }

    public static void serverError(OutputStream output) {
        //todo make a predefined server error http request
        new HttpResponse(500, ContentTypeHelper.ContentType.html, BodyFactory.GetPredefinedBody(500), new HttpRequest(), output);
    }

    public static void ProcessRequest(OutputStream output, String userRequest) {
        try {
            HttpRequest request = new HttpRequest(userRequest);

            switch (request.getRequestType()){
                case HEAD:
                case POST:
                case GET:
                    readAndSendFile(output, request);
                    break;
                case TRACE:
                    processTraceRequest(output, userRequest);
                    break;
                case PUT:
                case PATCH:
                case DELETE:
                case CONNECT:
                case OPTIONS:
                    notImplemented(output, request);
                    break;
                default:
                    badRequest(output);
                    break;
            }
        }
        catch(FileNotFoundException ex){
            notFound(output, new HttpRequest());
        }
        catch (IOException ex)
        {
            badRequest(output);
        }
        catch (IllegalArgumentException ex) {
            badRequest(output);
        }
        catch (Exception ex)
        {
            serverError(output);
        }
    }

    private static void processTraceRequest(OutputStream output, String request) {
        new HttpResponse(200, ContentTypeHelper.ContentType.other, request.getBytes(), new HttpRequest(), output);
    }

    private static void badRequest(OutputStream output) {
       new HttpResponse(400, ContentTypeHelper.ContentType.html, BodyFactory.GetPredefinedBody(400), new HttpRequest(), output);
    }

    private static void notImplemented(OutputStream output, HttpRequest request) {
        new HttpResponse(501, ContentTypeHelper.ContentType.html, BodyFactory.GetPredefinedBody(501), request ,output);
    }

    private static void readAndSendFile(OutputStream output, HttpRequest request) {
        ContentTypeHelper.ContentType type = ContentTypeHelper.GetContentType(request.getRequestedPage());
        try{
            byte[] fileBytes = Files.readAllBytes(Paths.get(request.getRequestedPage()));
            if(request.getRequestedPage().equals(HttpServer.getRootDirectory() + "/params_info.html"))
            {
               fileBytes = insertGeneratedTableIntoHTML(fileBytes);
            }

            new HttpResponse(200, type, fileBytes, request, output);
        }
        catch(IOException exception) {
            notFound(output, request);
        }
    }

    public static byte[] insertGeneratedTableIntoHTML(byte[] htmlBytes) {
        // Convert the byte array to a String
        String htmlContent = new String(htmlBytes, StandardCharsets.UTF_8);

        // Generate the table HTML
        String tableHTML = HttpServer.generateTableHTML(); // Assuming this method exists and generates the HTML table

        // Find the position to insert the table (just before </body>)
        int insertPosition = htmlContent.lastIndexOf("</body>");
        if (insertPosition == -1) {
            insertPosition = htmlContent.length() - 1;
        }

        // Insert the table HTML into the existing content
        String modifiedHTML = new StringBuilder(htmlContent)
                .insert(insertPosition, tableHTML)
                .toString();

        // Convert the modified String back to a byte array
        return modifiedHTML.getBytes(StandardCharsets.UTF_8);
    }
}
