package pt.tecnico.cnv.loadbalancer;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.Instance;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

@SuppressWarnings("restriction")
public class WebServer {
    private static int port = 12000;
    private static AmazonEC2      ec2;
    private static AmazonCloudWatch cloudWatch;
    private final static String WORKER_AMI_ID = "ami-48b2a62c";


	public static void main(String[] args) throws Exception {
		System.out.println("Booting server");
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/instances/list", new ListInstances());
        server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
        server.start();
        init();
		System.out.println("Server is now running");
    }
	
    static class ListInstances implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            System.out.println("Query: " + query);

            String newline = "\n";
            String message = "ok" + newline;

            ListWorkerInstances executer = new ListWorkerInstances(ec2);
            List<Instance> instances = executer.listInstances();
            for(Instance instance : instances) {
                String name = instance.getInstanceId();
                message += name + newline;
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
