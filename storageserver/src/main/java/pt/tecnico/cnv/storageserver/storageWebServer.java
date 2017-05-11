package pt.tecnico.cnv.storageserver;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import pt.tecnico.cnv.common.InvalidArgumentsException;
import pt.tecnico.cnv.common.QueryParser;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;


@SuppressWarnings("restriction")
public class MetricStorageApp extends Thread{
    private static MetricStorageApp _instance;
    private List<Data> _data;




    private static int port = 8000;
    private static AmazonDynamoDB dynamoDB;

    static String defaultTableName = "";



    private MetricStorageApp(){
        _data = new ArrayList<Data>();
    }

    public static void main(String[] args) throws Exception {
        try {
            HttpServer server;
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/sendData.html", new ReceiveDataHandler());
            server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
            server.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        init();
        System.out.println("Greetings summoners! MSS is now running..");
    }

    static class ReceiveDataHandler implements HttpHandler {

        public void handle(HttpExchange t) throws IOException {

            String query = t.getRequestURI().getQuery();

            OutputStream os = t.getResponseBody();
            String message = null;
            int requestStatus = 0;

            Map<String, String> result = new HashMap<String, String>();

            try {

                QueryParser parser = new QueryParser(query);
                result = parser.queryToMap(query);

                for (Map.Entry<String, String> entry : result.entrySet()){

                    System.out.println("KEY: "entry.getKey() + "\tVALUE: " + entry.getValue() + "\n");
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


    public static MetricStorageApp getInstance(){
        if(_instance == null) _instance = new MetricStorageApp();
        return _instance;
    }

    public static void initTables(){

    }

    public static void createTable(String tableName){

        try {
        // Create a table with a primary hash key named 'name', which holds a string
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                .withKeySchema(new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName("name").withAttributeType(ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

        // Create table if it does not exist yet
        TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
        // wait for the table to move into ACTIVE state

        TableUtils.waitUntilActive(dynamoDB, tableName);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


    private static void init() throws Exception {

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
    }

    public List<Data> data(){ return _data; }

    public void data(Data data){ _data.add(data); }


}