package pt.tecnico.cnv.storageserver;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.tecnico.cnv.common.GenericHandler;
import pt.tecnico.cnv.common.HttpAnswer;
import pt.tecnico.cnv.common.HttpStrategy;
import pt.tecnico.cnv.common.InvalidArgumentsException;
import pt.tecnico.cnv.common.QueryParser;
import pt.tecnico.cnv.common.STATIC_VALUES;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;


@SuppressWarnings("restriction")
public class storageWebServer extends Thread{

    private static MetricStorageApp _app;
    private static AmazonDynamoDB dynamoDB;

    private static int port = 8000;

    private static int inc = 0;



    public static void main(String[] args) throws Exception {


        try {
            boolean success = init();
        }catch (Exception e ) {
            e.printStackTrace();
            System.out.println("Init Failed!!");
            return;

        }


        try {
            HttpServer server;
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/ping", new PingHandler());
            server.createContext("/data.html", new ReceiveDataHandler());
            server.createContext("/update.html", new UpdateItemHandler());
            server.createContext("/metric/value", new GenericHandler(new MetricValueStrategy()));
            server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
            server.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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


    static class UpdateItemHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {

            String query = t.getRequestURI().getQuery();

            OutputStream os = t.getResponseBody();
            String message = "";
            int requestStatus = 0;

            Map<String, String> result = new HashMap<>();

            try {

                QueryParser parser = new QueryParser(query);
                result = parser.queryToMap(query);

                requestStatus = 200;

                message += "\nUpdating new items with query...";


                message += _app.updateItemAttributes(query);


                String filename = "";

                for (Map.Entry<String, String> entry : result.entrySet()){

                    switch (entry.getKey()) {
                        case "f":
                            filename = entry.getValue();
                            inc++;
                            break;
                        default:
                            break;
                    }

                }


                message += "\nQuerying new item ...";

                message += _app.queryItem(filename);



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



    static class ReceiveDataHandler implements HttpHandler {

        public void handle(HttpExchange t) throws IOException {

            String query = t.getRequestURI().getQuery();

            OutputStream os = t.getResponseBody();
            String message = "";
            int requestStatus = 0;



            try {


                requestStatus = 200;

                message += "\nCreating new items with query...";


                _app.insertNewItem(query);


                message += "\nQuerying new item ...";

                message += _app.queryItemMetric(query);



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


    private static boolean init() {


        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        dynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_WEST_2).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        _app = new MetricStorageApp(dynamoDB);

        return true;
    }


    private static class MetricValueStrategy extends HttpStrategy {
        @Override
        public HttpAnswer process(String query) throws Exception {
            boolean alreadyInstrumented = false;
            long metric = 100;



            //dont mess with what is below
            String message = alreadyInstrumented + STATIC_VALUES.SEPARATOR_STORAGE_METRIC_REQUEST + metric;
            return new HttpAnswer(200,message);
        }
    }
}