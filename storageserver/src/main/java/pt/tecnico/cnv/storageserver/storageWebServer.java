package pt.tecnico.cnv.storageserver;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.tecnico.cnv.common.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


@SuppressWarnings("restriction")
public class storageWebServer extends Thread{

    private static MetricStorageApp _app;
    private static AmazonDynamoDB dynamoDB;

    private static int port = 8000;

    private static int inc = 0;



    public static void main(String[] args) throws Exception {

        boolean deleteOnInit = Boolean.parseBoolean(args[0]);
        try {
            boolean success = init(deleteOnInit);
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
            server.createContext("/testmetric.html", new TestMetricHandler());
            server.createContext("/test/item", new TestItemHandler());
            server.createContext("/metric/value", new GenericHandler(new MetricValueStrategy()));
            server.createContext("/deleteTable", new DeleteTableHandler());
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

            System.out.println("pong");
            OutputStream os = t.getResponseBody();
            String message = "ok";
            int requestStatus = 200;
            t.sendResponseHeaders(requestStatus, message.length());
            os.write(message.getBytes());
            os.close();
        }
    }

    static class DeleteTableHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {

            OutputStream os = t.getResponseBody();
            String message = "Forwarding request for deletion..\n";

            int requestStatus = 200;

            _app.deleteDefaultTable();


            t.sendResponseHeaders(requestStatus, message.length());
            os.write(message.getBytes());
            os.close();
        }
    }


    static class TestMetricHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {

            String query = t.getRequestURI().getQuery();

            System.out.println("TEST QUERY: "  + query);
            OutputStream os = t.getResponseBody();
            String message = "";
            int requestStatus = 0;


            try {



                message += "\nQuerying item with query: " + query + "\n\n\n";


                message += "METRIC VALUE: " +_app.queryItemMetric(query);



            /*} catch (InvalidArgumentsException e) {
                e.printStackTrace();
                message = "Error: InvalidArgumentsException";
                requestStatus = 400;
                System.err.println(message);
            }*/} catch (Throwable e) {
                e.printStackTrace();
                message = "Error: " + e.getMessage();
                System.err.println(message);
            }
            t.sendResponseHeaders(requestStatus, message.length());
            os.write(message.getBytes());

            os.close();
        }

    }

    static class TestItemHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {

            String query = t.getRequestURI().getQuery();


            OutputStream os = t.getResponseBody();
            String message = "";
            int requestStatus = 0;


            try {



               message = _app.queryItemFullTest();


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
            System.out.println("########################################################\n\n" +
                    "PUT QUERY: "  + query);
            OutputStream os = t.getResponseBody();
            String message =  "PUT QUERY: "  + query + "\n\n\n";
            int requestStatus = 0;

            try {
                requestStatus = 200;
                _app.insertNewItem(query);
            } catch (Throwable e) {
                e.printStackTrace();
                message = "Error: " + e.getMessage();
                System.err.println(message);
            }

            System.out.println("########################################################\n\n");
            t.sendResponseHeaders(requestStatus, message.length());
            os.write(message.getBytes());

            os.close();


        }
    }


    private static boolean init(boolean deleteOnInit) {


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
        dynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_WEST_2).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        _app = new MetricStorageApp(dynamoDB, deleteOnInit);

        return true;
    }


    private static class MetricValueStrategy extends HttpStrategy {
        @Override
        public HttpAnswer process(String query) throws Exception {

            System.out.println("QUERY: "  + query);

            String message = _app.queryItemMetric(query);

            //dont mess with what is below

            return new HttpAnswer(200,message);
        }
    }
}