package pt.tecnico.cnv.storageserver;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
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
    private static AmazonDynamoDB dynamoDB;

    private static int port = 8000;



    public static void main(String[] args) throws Exception {
        try {
            HttpServer server;
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/ping", new PingHandler());
            server.createContext("/data.html", new ReceiveDataHandler());
            server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
            server.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        init();

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

                message += parser.toString();

                for (Map.Entry<String, String> entry : result.entrySet()){

                    message +=  "KEY: " + entry.getKey() + "\t\tVALUE: " + entry.getValue() + "\n";
                }

                requestStatus = 200;


                message += "\n\n\n\n" + _app.createDefaultTable(message);

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


    private static void init() {


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
    }


}