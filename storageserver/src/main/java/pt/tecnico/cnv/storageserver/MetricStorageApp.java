package pt.tecnico.cnv.storageserver;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafa32 on 11-05-2017.
 */
public class MetricStorageApp {

    private static AmazonDynamoDB dynamoDB;

    private List<Data> _data;
    private static String defaultTableName = "";

    public MetricStorageApp(){
        _data = new ArrayList<Data>();
    }


    public List<Data> data(){ return _data; }

    public void data(Data data){ _data.add(data); }



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


    public static void init() throws Exception {

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
}
