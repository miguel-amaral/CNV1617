package pt.tecnico.cnv.storageserver;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import pt.tecnico.cnv.common.QueryParser;

import javax.xml.crypto.Data;
import java.util.*;

/**
 * Created by rafa32 on 11-05-2017.
 */
public class MetricStorageApp {

    private static AmazonDynamoDB _dynamoDB;

    private static String defaultTableName = "concatenation_of_query";

    public MetricStorageApp(AmazonDynamoDB dynamoDB){

        this._dynamoDB = dynamoDB;
        //createDefaultTable();
    }

    public static String createDefaultTable(String message){


        try {



            List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
            attributeDefinitions.add(new AttributeDefinition().withAttributeName("concat").withAttributeType(ScalarAttributeType.S));

            List<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
            keySchema.add(new KeySchemaElement().withAttributeName("concat").withKeyType(KeyType.HASH)); // Partition key

            CreateTableRequest newRequest = new CreateTableRequest().withTableName(defaultTableName).withKeySchema(keySchema)
                    .withAttributeDefinitions(attributeDefinitions).withProvisionedThroughput(
                            new ProvisionedThroughput().withReadCapacityUnits(5L).withWriteCapacityUnits(6L));

            System.out.println("Issuing CreateTable request for " + defaultTableName);



            // Create table if it does not exist yet
            TableUtils.createTableIfNotExists(_dynamoDB, newRequest);
            // wait for the table to move into ACTIVE state
            TableUtils.waitUntilActive(_dynamoDB, defaultTableName);

            message += "Waiting for " + defaultTableName + " to be created...this may take a while...\n";

            message += getTableInformation(message);

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return message;

    }

    private static String getTableInformation(String message) {

        System.out.println("Describing " + defaultTableName);

        DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(defaultTableName);
        TableDescription tableDescription = _dynamoDB.describeTable(describeTableRequest).getTable();

        message += String.format(
                "Name: %s\n" + "Status: %s \n" + "Provisioned Throughput (read capacity units/sec): %d \n"
                        + "Provisioned Throughput (write capacity units/sec): %d \n",
                tableDescription.getTableName(), tableDescription.getTableStatus(),
                tableDescription.getProvisionedThroughput().getReadCapacityUnits(),
                tableDescription.getProvisionedThroughput().getWriteCapacityUnits());

        return message;
    }

    private static void deleteDefaultTable() {

        System.out.println("Deleting table: " + defaultTableName);

        DeleteTableRequest deleteRequest = new DeleteTableRequest().withTableName(defaultTableName);

        // Deletes if table exists
        TableUtils.deleteTableIfExists(_dynamoDB, deleteRequest);


    }

    private static void insertNewItem(String query){

        try{
            Map<String, String> result = new HashMap<>();

            QueryParser parser = new QueryParser(query);
            result = parser.queryToMap(query);

            Map<String, AttributeValue> item = newItem(query, result);
            PutItemRequest putItemRequest = new PutItemRequest(defaultTableName, item);
            PutItemResult putItemResult = _dynamoDB.putItem(putItemRequest);

            System.out.println("Result: " + putItemResult);

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        } catch (Exception e){

            e.printStackTrace();
        }

    }



    private static Map<String, AttributeValue> newItem(String query, Map<String, String> result) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();

        item.put("concat", new AttributeValue(query));

        for (Map.Entry<String, String> entry : result.entrySet()){

            switch (entry.getKey()) {
                case "f":
                    item.put("fileName", new AttributeValue(entry.getValue()));
                    break;
                case "sc":
                    item.put("scols", new AttributeValue(entry.getValue()));
                    break;
                case "sr":
                    item.put("srows", new AttributeValue(entry.getValue()));
                    break;
                case "wc":
                    item.put("wcols", new AttributeValue(entry.getValue()));
                    break;
                case "wr":
                    item.put("wrows", new AttributeValue(entry.getValue()));
                    break;
                case "coff":
                    item.put("coff", new AttributeValue(entry.getValue()));
                    break;
                case "roff":
                    item.put("roff", new AttributeValue(entry.getValue()));
                    break;
                default:

            }
        }

        return item;
    }




}
