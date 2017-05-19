package pt.tecnico.cnv.storageserver;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import pt.tecnico.cnv.common.InvalidArgumentsException;
import pt.tecnico.cnv.common.MetricCalculation;
import pt.tecnico.cnv.common.QueryParser;
import pt.tecnico.cnv.common.STATIC_VALUES;

import java.util.*;

/**
 * Created by rafa32 on 11-05-2017.
 */
public class MetricStorageApp {

    private static AmazonDynamoDB _dynamoDB;

    private static String defaultTableName = "metrics_table";

    private static Map<String, String> _cache;

    public MetricStorageApp(AmazonDynamoDB dynamoDB, boolean deleteOnInit){

        this._dynamoDB = dynamoDB;
        this._cache = new HashMap<>();

        if(deleteOnInit){
            deleteDefaultTable();
        }
        createDefaultTable();
    }

    private static void createDefaultTable(){

        try {


            List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
            attributeDefinitions.add(new AttributeDefinition().withAttributeName("query").withAttributeType(ScalarAttributeType.S));

            List<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
            keySchema.add(new KeySchemaElement().withAttributeName("query").withKeyType(KeyType.HASH)); // Partition key

            CreateTableRequest newRequest = new CreateTableRequest().withTableName(defaultTableName).withKeySchema(keySchema)
                    .withAttributeDefinitions(attributeDefinitions).withProvisionedThroughput(
                            new ProvisionedThroughput().withReadCapacityUnits(25L).withWriteCapacityUnits(25L));



            // Create table if it does not exist yet
            TableUtils.createTableIfNotExists(_dynamoDB, newRequest);
            // wait for the table to move into ACTIVE state
            TableUtils.waitUntilActive(_dynamoDB, defaultTableName);


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


    public static void insertNewItem(String query){


        try{


            putItemInCache(query);

            Map<String, AttributeValue> item = newItem(query);
            PutItemRequest putItemRequest = new PutItemRequest(defaultTableName, item);
            PutItemResult putItemResult = _dynamoDB.putItem(putItemRequest);


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
        }


    }

    private static void putItemInCache(String query) {

        InstQueryParser parser = null;
        try {
            parser = new InstQueryParser(query);
        

            Map<String, String> resultMap = new HashMap<>();
            resultMap = parser.queryToMap(query);
    
    
            int index = query.indexOf("jobID");
            String query_for_key = query.substring(0,index-1);
    
            long metric = computeMetric(resultMap);
    
    
            resultMap.put(query_for_key, Long.toString(metric));

        } catch (InvalidArgumentsException e) {
            e.printStackTrace();
        }

    }



    public static String queryItemMetric(String query){

        String message = "";
        try{

            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            Condition condition = new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue(query));
            scanFilter.put("query", condition);
            ScanRequest scanRequest = new ScanRequest(defaultTableName).withScanFilter(scanFilter);
            ScanResult scanResult = _dynamoDB.scan(scanRequest);


            int metric = Integer.parseInt(scanResult.getItems().get(0).get("metric").getN());

            message = true + STATIC_VALUES.SEPARATOR_STORAGE_METRIC_REQUEST + Integer.toString(metric);



        } catch (IndexOutOfBoundsException e) {

            message = false + STATIC_VALUES.SEPARATOR_STORAGE_METRIC_REQUEST + guessMetric(query);


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
        }


        return message;
    }

    private static Map<String, AttributeValue> newItem(String query) {

        InstQueryParser parser = null;
        try {
            parser = new InstQueryParser(query);
        } catch (InvalidArgumentsException e) {
            e.printStackTrace();
        }

        Map<String, String> resultMap = new HashMap<>();
        resultMap = parser.queryToMap(query);



        int index = query.indexOf("jobID");
        String query_for_key = query.substring(0,index-1);

        System.out.println("\n\nPARAMS: \n" + parser.toString() + "\n");

        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();

        item.put("query", new AttributeValue(query_for_key));


        double sc = 0, sr = 0, wc = 0, wr = 0, coff = 0, roff = 0;


        long metric = computeMetric(resultMap);

        System.out.println("INSERTING THIS QUERY:" + query_for_key + "\nWITH METRIC: " + Long.toString(metric) + "\n");

        for (Map.Entry<String, String> entry : resultMap.entrySet()) {

            switch (entry.getKey()) {
                case "f":
                    item.put("file", new AttributeValue().withS(entry.getValue()));
                    break;
                case "sc":
                    item.put("sc", new AttributeValue().withN(entry.getValue()));
                    sc = Double.parseDouble(entry.getValue());
                    break;
                case "sr":
                    item.put("sr", new AttributeValue().withN(entry.getValue()));
                    sr = Double.parseDouble(entry.getValue());
                    break;
                case "wc":
                    item.put("wc", new AttributeValue().withN(entry.getValue()));
                    wc = Double.parseDouble(entry.getValue());
                    break;
                case "wr":
                    item.put("wr", new AttributeValue().withN(entry.getValue()));
                    wr = Double.parseDouble(entry.getValue());
                    break;
                case "coff":
                    item.put("coff", new AttributeValue().withN(entry.getValue()));
                    coff = Double.parseDouble(entry.getValue());
                    break;
                case "roff":
                    item.put("roff", new AttributeValue().withN(entry.getValue()));
                    roff = Double.parseDouble(entry.getValue());
                    break;
                case "instructions":
                    item.put("instructions", new AttributeValue().withN(entry.getValue()));
                    break;
                case "bb_blocks":
                    item.put("bb_blocks", new AttributeValue().withN(entry.getValue()));
                    break;
                case "branch_fail":
                    item.put("branch_fail", new AttributeValue().withN(entry.getValue()));
                    break;
                default:
                    break;
            }

        }

        double row_percent_start = (roff / sr)*100;
        if(row_percent_start>100.0) row_percent_start = 100.0;

        double row_percent_end = ((roff + wr) / sr)*100;
        if(row_percent_end>100.0) row_percent_start = 100.0;

        double column_percent_start = (coff / sc)*100;
        if(column_percent_start>100.0) row_percent_start = 100.0;

        double column_percent_end = ((coff + wc) / sr)*100;
        if(column_percent_end>100.0) row_percent_start = 100.0;

        item.put("row_percent_start", new AttributeValue().withN(Integer.toString((int)row_percent_start)));
        item.put("row_percent_end", new AttributeValue().withN(Integer.toString((int)row_percent_end)));
        item.put("column_percent_start", new AttributeValue().withN(Integer.toString((int)column_percent_start)));
        item.put("column_percent_end", new AttributeValue().withN(Integer.toString((int)column_percent_end)));

        System.out.println("row_percent_start:" + Double.toString(row_percent_start));
        System.out.println("row_percent_end:" + Double.toString(row_percent_end));
        System.out.println("column_percent_start:" + Double.toString(column_percent_start));
        System.out.println("column_percent_end:" + Double.toString(column_percent_end));



        item.put("metric", new AttributeValue().withN(Long.toString(metric)));

        return item;
    }

    private static long computeMetric(Map<String, String> result) {


        long blocks = 0, meths = 0, b_fail = 0;


        for (Map.Entry<String, String> entry : result.entrySet()){

            switch (entry.getKey()) {

                case "bb_blocks":
                    blocks = Long.parseLong(entry.getValue());
                    break;
                case "methods":
                    meths = Long.parseLong(entry.getValue());
                    break;
                case "branch_fail":
                    b_fail = Long.parseLong(entry.getValue());
                    break;
                default:
                    break;
            }

        }
        return MetricCalculation.calculate(blocks,meths,b_fail);

    }

    private static long guessMetric(String query) {


        long metric;

        //CHECK HOW QUERY IS INSERTED
        if((_cache.containsKey(query))) {
            metric = Long.parseLong(_cache.get(query));
            return metric;
        }


        QueryParser parser = null;
        try {
            parser = new QueryParser(query);
        } catch (InvalidArgumentsException e) {
            e.printStackTrace();
        }

        Map<String, String> result = new HashMap<>();
        result = parser.queryToMap(query);

        metric = queryItemWithCoords(query);

        if(metric == 0)
            metric = 1;

        return metric;

    }


    public static void deleteDefaultTable() {

        System.out.println("Request for deletion on: " + defaultTableName + "is being handled...\n");

        DeleteTableRequest deleteRequest = new DeleteTableRequest().withTableName(defaultTableName);
        // Deletes if table exists
        TableUtils.deleteTableIfExists(_dynamoDB, deleteRequest);
        System.out.println("Deletion complete!!\n");

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

    public static String insertNewItemTest(String query){

        String message = "";
        String error = "";
        try{

            InstQueryParser parser = new InstQueryParser(query);

            Map<String, String> result = new HashMap<>();
            result = parser.queryToMap(query);

            int index = query.indexOf("instructions");
            String query_for_key = query.substring(0,index-1);



            Map<String, AttributeValue> item = newItem(query);
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
        } catch (InvalidArgumentsException e){

            e.printStackTrace();
        }


        return message + error;
    }

    public static String queryItemTest(String query){

        String message = "";
        String error = "";
        Map<String, String> result = new HashMap<>();

        try{

            InstQueryParser parser = new InstQueryParser(query);
            result = parser.queryToMap(query);

            String filename = "";

            for (Map.Entry<String, String> entry : result.entrySet()){

                switch (entry.getKey()) {
                    case "f":
                        filename = entry.getValue();
                        break;
                    default:
                        break;
                }

            }

            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            Condition condition = new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue(filename));
            scanFilter.put("file", condition);
            ScanRequest scanRequest = new ScanRequest(defaultTableName).withScanFilter(scanFilter);
            ScanResult scanResult = _dynamoDB.scan(scanRequest);
            message += "\n\nResult of equality: " + scanResult;
            message += "\n\nFilename: " + scanResult.getItems().get(0).get("file").getS();

            int insts = Integer.parseInt(scanResult.getItems().get(0).get("instructions").getN());


            message += "\n\n# of Instructions: " + Integer.toString(insts);

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
        } catch (InvalidArgumentsException e){

            e.printStackTrace();
        }


        return message + error;
    }

    public static String queryItemFullTest(){

        String message = "";


        try{




            ScanRequest scanRequest = new ScanRequest(defaultTableName);
            scanRequest.setConditionalOperator(ConditionalOperator.AND);

            Map<String, Condition> scanFilter = new HashMap<String, Condition>();
            scanFilter.put("row_percent_start", new Condition()
                    .withAttributeValueList(new AttributeValue().withN("5")).withComparisonOperator(ComparisonOperator.GT.toString()));
            scanFilter.put("row_percent_end", new Condition()
                    .withAttributeValueList(new AttributeValue().withN("95")).withComparisonOperator(ComparisonOperator.LT.toString()));
            scanFilter.put("column_percent_start", new Condition()
                    .withAttributeValueList(new AttributeValue().withN("5")).withComparisonOperator(ComparisonOperator.GT.toString()));
            scanFilter.put("column_percent_end", new Condition()
                    .withAttributeValueList(new AttributeValue().withN("95")).withComparisonOperator(ComparisonOperator.LT.toString()));


            scanRequest.setScanFilter(scanFilter);
            ScanResult scanResult = _dynamoDB.scan(scanRequest);

            System.out.println("Printing items..");


            if(scanResult.getCount() == 0){

                System.out.println("\nNo items matched..");
            }
            for(Map<String, AttributeValue> item : scanResult.getItems()) {

               System.out.println("image query: " + item.get("query").getS());
               System.out.println("row_percent_start: " + item.get("row_percent_start").getN());
               System.out.println("row_percent_end: " + item.get("row_percent_end").getN());
               System.out.println("column_percent_start: " + item.get("column_percent_start").getN());
               System.out.println("column_percent_end: " + item.get("column_percent_end").getN());

            }




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
        }


        return message;
    }

    public static Long queryItemWithCoords(String query){

        String message = "";

        long metric_sum = 0;

        try{

            QueryParser parser = null;
            try {
                parser = new QueryParser(query);
            } catch (InvalidArgumentsException e) {
                e.printStackTrace();
            }

            Map<String, String> result = new HashMap<>();
            result = parser.queryToMap(query);


            ScanRequest scanRequest = new ScanRequest(defaultTableName);
            scanRequest.setConditionalOperator(ConditionalOperator.AND);

            Map<String, Condition> scanFilter = new HashMap<String, Condition>();


            System.out.println("################################");



            for (Map.Entry<String, String> entry : result.entrySet()) {

                switch (entry.getKey()) {
                    case "f":
                        System.out.println("file to scan: " + entry.getValue());
                        scanFilter.put("f", new Condition()
                                .withAttributeValueList(new AttributeValue().withS(entry.getValue()))
                                .withComparisonOperator(ComparisonOperator.EQ.toString()));
                        break;
                    /*case "wc":
                        int wc = Integer.parseInt(entry.getValue()); //+ (int)(Integer.parseInt(entry.getValue())*0.10);
                        System.out.println("wc: " + Integer.toString(wc));

                        scanFilter.put("wc", new Condition()
                                .withAttributeValueList(new AttributeValue().withN(Integer.toString(wc)))
                                .withComparisonOperator(ComparisonOperator.LT.toString()));
                        break;
                    case "wr":
                        int wr = Integer.parseInt(entry.getValue()); //+ (int)(Integer.parseInt(entry.getValue())*0.10);

                        System.out.println("wr: " + Integer.toString(wr));
                        scanFilter.put("wr", new Condition()
                                .withAttributeValueList(new AttributeValue().withN(Integer.toString(wr)))
                                .withComparisonOperator(ComparisonOperator.LT.toString()));
                        break;
                    case "coff":
                        int coff = Integer.parseInt(entry.getValue()); //- (int)(Integer.parseInt(entry.getValue())*0.05);

                        System.out.println("coff: " + Integer.toString(coff));
                        scanFilter.put("coff", new Condition()
                                .withAttributeValueList(new AttributeValue().withN(Integer.toString(coff)))
                                .withComparisonOperator(ComparisonOperator.GT.toString()));
                        break;
                    case "roff":
                        int roff = Integer.parseInt(entry.getValue()); //- (int)(Integer.parseInt(entry.getValue())*0.05);

                        System.out.println("roff: " + Integer.toString(roff));
                        scanFilter.put("roff", new Condition()
                                .withAttributeValueList(new AttributeValue().withN(Integer.toString(roff)))
                                .withComparisonOperator(ComparisonOperator.GT.toString()));
                        break;*/
                    default:
                        break;
                }

            }




            scanRequest.setScanFilter(scanFilter);
            ScanResult scanResult = _dynamoDB.scan(scanRequest);

            System.out.println("Printing items..");



            if(scanResult.getCount() == 0){

                System.out.println("\nNo items matched..");
            }

            for(Map<String, AttributeValue> item : scanResult.getItems()) {

                metric_sum += Long.parseLong(item.get("metric").getN());
                System.out.println("\nimage query: " + item.get("query").getS());

            }




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
        }


        return metric_sum;
    }
}
