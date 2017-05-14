package pt.tecnico.cnv.storageserver;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import pt.tecnico.cnv.common.QueryParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rafa32 on 11-05-2017.
 */
public class MetricStorageApp {

    private static AmazonDynamoDB _dynamoDB;

    private static String defaultTableName = "metrics_table";

    public MetricStorageApp(AmazonDynamoDB dynamoDB){

        this._dynamoDB = dynamoDB;
        createDefaultTable();
    }

    public static void createDefaultTable(){

        String sms = "";

        try {

            //Deletes table if exists;
            deleteDefaultTable();


            List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
            attributeDefinitions.add(new AttributeDefinition().withAttributeName("query").withAttributeType(ScalarAttributeType.S));

            List<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
            keySchema.add(new KeySchemaElement().withAttributeName("query").withKeyType(KeyType.HASH)); // Partition key

            CreateTableRequest newRequest = new CreateTableRequest().withTableName(defaultTableName).withKeySchema(keySchema)
                    .withAttributeDefinitions(attributeDefinitions).withProvisionedThroughput(
                            new ProvisionedThroughput().withReadCapacityUnits(15L).withWriteCapacityUnits(15L));

            sms += "Issuing CreateTable request for " + defaultTableName;



            // Create table if it does not exist yet
            TableUtils.createTableIfNotExists(_dynamoDB, newRequest);
            // wait for the table to move into ACTIVE state
            TableUtils.waitUntilActive(_dynamoDB, defaultTableName);



            sms += "\nWaiting for " + defaultTableName + " to be created...this may take a while...\n";

            sms += getTableInformation();



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

    }





    public static String insertNewItem(String query){

        String message = "";
        String error = "";
        try{

            InstQueryParser parser = new InstQueryParser(query);

            Map<String, String> result = new HashMap<>();
            result = parser.queryToMap(query);

            int index = query.indexOf("instructions");
            String query_for_key = query.substring(0,index);

            String filename = "";
            String instructions = "";

            for (Map.Entry<String, String> entry : result.entrySet()){

                switch (entry.getKey()) {
                    case "f":
                        filename = entry.getValue();
                        break;
                    case "instructions":
                        instructions = entry.getValue();
                        break;
                    default:
                        break;
                }

            }


            Map<String, AttributeValue> item = newItem(query, filename, instructions);
            PutItemRequest putItemRequest = new PutItemRequest(defaultTableName, item);
            PutItemResult putItemResult = _dynamoDB.putItem(putItemRequest);


            message += "\nItem inserted..";


        } catch (AmazonServiceException ase) {
            error += "\n\nCaught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.";
            error += "\nError Message:    " + ase.getMessage();
            error += "\nHTTP Status Code: " + ase.getStatusCode();
            error += "\nAWS Error Code:   " + ase.getErrorCode();
            error += "\nError Type:       " + ase.getErrorType();
            error += "\nRequest ID:       " + ase.getRequestId();
        } catch (AmazonClientException ace) {
            error += "\nCaught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.";
            error += "\nError Message: " + ace.getMessage();
        } catch (Exception e){

            e.printStackTrace();
        }


        return message + error;
    }

    public static String queryItem(String filename){

        String message = "";
        String error = "";
        try{


            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            Condition condition = new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue(filename));
            scanFilter.put("file", condition);
            ScanRequest scanRequest = new ScanRequest(defaultTableName).withScanFilter(scanFilter);
            ScanResult scanResult = _dynamoDB.scan(scanRequest);
            message += "\n\nResult of equality: " + scanResult;

            scanFilter = new HashMap<String, Condition>();
            condition = new Condition()
                    .withComparisonOperator(ComparisonOperator.GT.toString())
                    .withAttributeValueList(new AttributeValue().withN("1"));
            scanFilter.put("instructions", condition);
            scanRequest = new ScanRequest(defaultTableName).withScanFilter(scanFilter);
            scanResult = _dynamoDB.scan(scanRequest);
            message += "\n\nResult of greater than: " + scanResult;





        } catch (AmazonServiceException ase) {
            error += "\n\nCaught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.";
            error += "\nError Message:    " + ase.getMessage();
            error += "\nHTTP Status Code: " + ase.getStatusCode();
            error += "\nAWS Error Code:   " + ase.getErrorCode();
            error += "\nError Type:       " + ase.getErrorType();
            error += "\nRequest ID:       " + ase.getRequestId();
        } catch (AmazonClientException ace) {
            error += "\nCaught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.";
            error += "\nError Message: " + ace.getMessage();
        } catch (Exception e){

            e.printStackTrace();
        }


        return message + error;
    }


    public static String updateItemAttributes(String query) {


        String message = "";
        String error = "";

        try {

            HashMap<String, AttributeValue> key = new HashMap<String, AttributeValue>();
            key.put("query", new AttributeValue().withS(query));


            Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
            expressionAttributeValues.put(":val", new AttributeValue().withN(Integer.toString(10)));

            ReturnValue returnValues = ReturnValue.ALL_NEW;

            UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                    .withTableName(defaultTableName)
                    .withKey(key)
                    .withUpdateExpression(" set instructions=:val")
                    .withExpressionAttributeValues(expressionAttributeValues)
                    .withReturnValues(returnValues);

            UpdateItemResult result = _dynamoDB.updateItem(updateItemRequest);



        }   catch (AmazonServiceException ase) {

            error += ase.getMessage();
        }

        return message + error;
    }



    private static Map<String, AttributeValue> newItem(String query, String filename, String instructions) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();

        item.put("query", new AttributeValue(query));
        item.put("file", new AttributeValue(filename));
        item.put("instructions", new AttributeValue().withN(instructions));


        return item;
    }


    private static void deleteDefaultTable() {

        DeleteTableRequest deleteRequest = new DeleteTableRequest().withTableName(defaultTableName);
        // Deletes if table exists
        TableUtils.deleteTableIfExists(_dynamoDB, deleteRequest);

    }


    private static void deleteTable(String table_name) {

        DeleteTableRequest deleteRequest = new DeleteTableRequest().withTableName(table_name);
        // Deletes if table exists
        TableUtils.deleteTableIfExists(_dynamoDB, deleteRequest);

    }


    private static String getTableInformation() {

        System.out.println("Describing " + defaultTableName);

        DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(defaultTableName);
        TableDescription tableDescription = _dynamoDB.describeTable(describeTableRequest).getTable();

        String message = String.format(
                "Name: %s\n" + "Status: %s \n" + "Provisioned Throughput (read capacity units/sec): %d \n"
                        + "Provisioned Throughput (write capacity units/sec): %d \n",
                tableDescription.getTableName(), tableDescription.getTableStatus(),
                tableDescription.getProvisionedThroughput().getReadCapacityUnits(),
                tableDescription.getProvisionedThroughput().getWriteCapacityUnits());

        return message;
    }

}
