package pt.tecnico.cnv.loadbalancer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import tool.ContainerManager;
import tool.DataContainer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.auth.AWSStaticCredentialsProvider;

import java.util.ArrayList;
import java.util.Date;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;

@SuppressWarnings("restriction")
public class WebServer {
    private static int port = 80;
    private static AmazonEC2      ec2;
    private static AmazonCloudWatch cloudWatch;

	public static void main(String[] args) throws Exception {
		System.out.println("Booting server");
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/ping", new PingHandler());
        server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
        server.start();
		System.out.println("Server is now running");

    }
	
    static class PingHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            System.out.println(query);

            String newline = "<br/>"
            String message = "ok";

            try {
                init();
//                if(startInstance){
//                    System.out.println("Starting a new instance.");
//                    RunInstancesRequest runInstancesRequest=
//                            new RunInstancesRequest();
//
//                    runInstancesRequest.withImageId("ami-a0e9d7c6")
//                            .withInstanceType("t2.micro")
//                            .withMinCount(1)
//                            .withMaxCount(1)
//                            .withKeyName("jog-aws")
//                            .withSecurityGroups("ssh+http8000");
//                    RunInstancesResult runInstancesResult=
//                            ec2.runInstances(runInstancesRequest);
//                    String newInstanceId=runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
//                }
                DescribeInstancesResult describeInstancesResult=ec2.describeInstances();
                List<Reservation> reservations=describeInstancesResult.getReservations();
                Set<Instance> instances=new HashSet<Instance>();

                System.out.println("total reservations = "+reservations.size());
                for(Reservation reservation:reservations){
                    instances.addAll(reservation.getInstances());
                }
                for(Instance instance:instances) {
                    String name=instance.getInstanceId();
                    message += name + newline;
//                    String state=instance.getState().getName();
//                    if(state.equals("running")){
//                        System.out.println("running instance id = "+name);
//                        instanceDimension.setValue(name);
//                        GetMetricStatisticsRequest request=new GetMetricStatisticsRequest()
//                                .withStartTime(new Date(new Date().getTime()-offsetInMilliseconds))
//                                .withNamespace("AWS/EC2")
//                                .withPeriod(60)
//                                .withMetricName("CPUUtilization")
//                                .withStatistics("Average")
//                                .withDimensions(instanceDimension)
//                                .withEndTime(new Date());
//                        GetMetricStatisticsResult getMetricStatisticsResult=
//                                cloudWatch.getMetricStatistics(request);
//                        List<Datapoint> datapoints=getMetricStatisticsResult.getDatapoints();
//                        for(Datapoint dp:datapoints){
//                            System.out.println(" CPU utilization for instance "+name+
//                                    " = "+dp.getAverage());
//                        }
//                    }
//                    else{
//                        System.out.println("instance id = "+name);
//                    }
//                    System.out.println("Instance State : "+state+".");
                }

            } catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
            }


            OutputStream os = t.getResponseBody();
            int requestStatus = 200;
            t.sendResponseHeaders(requestStatus, message.length());
            os.write(message.getBytes());
            os.close();
        }
    }


    private static void init() throws Exception {

        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
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
        ec2 = AmazonEC2ClientBuilder.standard().withRegion("eu-west-2").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        cloudWatch = AmazonCloudWatchClientBuilder.standard().withRegion("eu-west-2").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }
}
