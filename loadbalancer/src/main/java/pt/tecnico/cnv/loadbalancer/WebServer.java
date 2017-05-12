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
import pt.tecnico.cnv.common.HttpAnswer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

@SuppressWarnings("restriction")
public class WebServer {
    private static int port = 8000;
    private static AmazonEC2      ec2;
    private static LoadBalancer _proccesser;
    private static AmazonCloudWatch cloudWatch;

	public static void main(String[] args) throws Exception {
		System.out.println("Booting server");
		try {
            boolean success = init();
        }catch (Exception e ) {
    		e.printStackTrace();
    		System.out.println("Init Failed!!");
    		return;

        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/instances/list", new GenericHandler(new ListInstancesStrategy()));
        server.createContext("/job/done", new GenericHandler(new ListInstancesStrategy()));
//        server.createContext("/r.html", new ImageRequestHandler());
        server.createContext("/processer/status", new GenericHandler(new ProccesserStatusStrategy()));
        server.createContext("/status", new GenericHandler(new ProccesserStatusStrategy()));
        server.createContext("/r.html", new GenericHandler(new ProccessQueryStrategy()));

        server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
        server.start();
		System.out.println("Server is now running");
		System.out.println("Credentials loaded");
    }

    static class GenericHandler implements HttpHandler {

        private final HttpStrategy _strategy;

        GenericHandler(HttpStrategy strategy) {
	        _strategy = strategy;
        }

        public void handle(HttpExchange httpExchange) throws IOException {
                String query = httpExchange.getRequestURI().getQuery();

            HttpAnswer answer = null;
            try {
                answer = _strategy.process(query);
            } catch (Exception e) {
                e.printStackTrace();
                answer = new HttpAnswer();
            }
            int requestStatus = answer.status();
            String message = answer.response();


            OutputStream os = httpExchange.getResponseBody();
            httpExchange.sendResponseHeaders(requestStatus, message.length());
            os.write(message.getBytes());
            os.close();

        }
    }

//    static class ImageRequestHandler implements HttpHandler {
//        public void handle(HttpExchange t) throws IOException {
//            String query = t.getRequestURI().getQuery();
//
//            _proccesser.processQuery(query);
//            int requestStatus = _proccesser.status();
//            String message = _proccesser.response();
//
//
//            OutputStream os = t.getResponseBody();
//            t.sendResponseHeaders(requestStatus, message.length());
//            os.write(message.getBytes());
//            os.close();
//        }
//    }

    private static boolean init() {


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

        _proccesser = new LoadBalancer(ec2);
        return true;
    }

    private static class ProccesserStatusStrategy extends HttpStrategy {
        HttpAnswer process(String query) throws Exception {
            String print = _proccesser.toString();
            return new HttpAnswer(200,print);
        }
    }

    private static class ProccessQueryStrategy extends HttpStrategy {
        HttpAnswer process(String query) throws Exception {
            return _proccesser.processQuery(query);
        }
    }
    private static class ListInstancesStrategy extends HttpStrategy {
        HttpAnswer process(String query) throws Exception{

            String newline = "\n";
            String message = "ok" + newline;
            System.out.println("message: " + message);
            ListWorkerInstances executer = new ListWorkerInstances(ec2);
            List<Instance> instances = executer.listInstances();
            for (Instance instance : instances) {
                String name = instance.getInstanceId();
                message += name + newline;
                System.out.println("message: " + message);
            }
            System.out.println("message: " + message);
            int requestStatus = 200;
            return new HttpAnswer(requestStatus,message);
        }
    }
}
