public class Main {
    public static void main(String args[]){
        String configFilePath = "../config.ini";
        HttpServer server = new HttpServer(configFilePath);
        server.start();
    }
}
