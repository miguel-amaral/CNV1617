package pt.tecnico.cnv.storageserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.tecnico.cnv.common.InvalidArgumentsException;
import pt.tecnico.cnv.common.QueryParser;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;


@SuppressWarnings("restriction")
public class storageWebServer extends Thread{

    private static MetricStorageApp _app;

    private static int port = 8000;



    public static void main(String[] args) throws Exception {
        try {
            HttpServer server;
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/ping", new PingHandler());
            server.createContext("/r.html", new ReceiveDataHandler());
            server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
            server.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        _app = getAppInstance();
        _app.init();

        System.out.println("Greetings summoners! MSS is now running..");
    }

    static class PingHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();

            System.out.println(query);
            OutputStream os = t.getResponseBody();
            String message = "ok";
            int requestStatus = 200;
            t.sendResponseHeaders(requestStatus, message.length());
            os.write(message.getBytes());
            os.close();
        }
    }

    static class ReceiveDataHandler implements HttpHandler {

        public void handle(HttpExchange t) throws IOException {

            String query = t.getRequestURI().getQuery();

            OutputStream os = t.getResponseBody();
            String message = null;
            int requestStatus = 0;

            Map<String, String> result = new HashMap<>();

            try {

                QueryParser parser = new QueryParser(query);
                result = parser.queryToMap(query);

                message =  "THIS IS YOUR QUERY! LET'S PREPROCESS IT!!\n";

                message += parser.toHMTLString();

                for (Map.Entry<String, String> entry : result.entrySet()){

                    message +=  "KEY: " + entry.getKey() + "\t\tVALUE: " + entry.getValue() + "\n";
                }

                requestStatus = 200;


            } catch (InvalidArgumentsException e) {
                e.printStackTrace();
                message = "Error: InvalidArgumentsException";
                requestStatus = 400;
                System.err.println(message);
            } catch (Throwable e) {
                e.printStackTrace();
                message = "Error: " + e.getMessage();
                System.err.println(message);
            }
            t.sendResponseHeaders(requestStatus, message.length());
            os.write(message.getBytes());

            os.close();


        }
    }


    public static MetricStorageApp getAppInstance(){
        if(_app == null) _app = new MetricStorageApp();
        return _app;
    }


}