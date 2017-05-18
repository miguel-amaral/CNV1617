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
import com.sun.net.httpserver.HttpServer;
import pt.tecnico.cnv.common.*;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
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
//        server.createContext("/r.html", new ImageRequestHandler());
        server.createContext("/instances/list", new GenericHandler(new ListInstancesStrategy()));
        server.createContext("/job/done", new GenericHandler(new JobDone()));
        server.createContext("/processer/status", new GenericHandler(new ProccesserStatusStrategy()));
        server.createContext("/status", new GenericHandler(new ProccesserStatusStrategy()));
        server.createContext("/r.html", new GenericHandler(new ProccessQueryStrategy()));
        server.createContext("/metrics", new GenericHandler(new GetMetricsStrategy()));
        server.createContext("/job/update", new GenericHandler(new UpdateMetricsStrategy()));
//        server.createContext("/launch", new GenericHandler(new LaunchInstanceStrategy()));
//        server.createContext("/terminate", new GenericHandler(new TerminateInstanceStrategy()));
        server.createContext("/time", new GenericHandler(new GetElapsedTimeStrategy()));

        server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
        server.start();
		System.out.println("Server is now running");
		System.out.println("Credentials loaded");
    }

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
        public HttpAnswer process(String query) throws Exception {
            String print = _proccesser.toString();
            return new HttpAnswer(200,print);
        }
    }

    private static class ProccessQueryStrategy extends HttpStrategy {
        public HttpAnswer process(String query) throws Exception {
            return _proccesser.processQuery(query,"r.html");
        }
    }
    private static class GetMetricsStrategy extends HttpStrategy {
        public HttpAnswer process(String query) throws Exception {
            return _proccesser.processQuery(query,"metrics");
        }
    }
    private static class ListInstancesStrategy extends HttpStrategy {
        public HttpAnswer process(String query) throws Exception{
            String newline = "\n";
            String message = "ok" + newline;
            ListWorkerInstances executer = new ListWorkerInstances(ec2);
            List<Instance> instances = executer.listInstances();
            for (Instance instance : instances) {
                String name = instance.getInstanceId();
                message += name + newline;
            }
            int requestStatus = 200;
            return new HttpAnswer(requestStatus,message);
        }
    }
    private static class JobDone extends HttpStrategy {
        public HttpAnswer process(String query) throws Exception {
            String[] arg = query.split("=");
            if(arg.length != 2) return new HttpAnswer();
            String jobId = arg[1];
            _proccesser.jobDone(jobId);
            return new HttpAnswer(200,"thanks");
        }
    }

    private static class LaunchInstanceStrategy extends HttpStrategy {
        public HttpAnswer process(String query) throws Exception {
            String[] arg = query.split("=");
            _proccesser.launchInstance(Integer.parseInt(arg[1]));
            return new HttpAnswer(200,"request received");
        }
    }
    private static class TerminateInstanceStrategy extends HttpStrategy {
        public HttpAnswer process(String query) throws Exception {
            String[] arg = query.split("=");
            _proccesser.terminateInstance(arg[1]);
            return new HttpAnswer(200,"request received");
        }
    }

    private static class GetElapsedTimeStrategy extends HttpStrategy {
        public HttpAnswer process(String query) throws Exception {
            return _proccesser.processQuery(query,"time");
        }
    }

    private static class UpdateMetricsStrategy extends HttpStrategy {
        public HttpAnswer process(String query) throws Exception {
            Map<String, String> arguments = QueryParser.queryToMap(query);
            String jobID = arguments.get("jobID");
            long metric = Long.parseLong(arguments.get("metric"));
            if(STATIC_VALUES.DEBUG_LOAD_BALANCER_JOB_UPDATE){ System.out.println("JOB UPDATE: " + jobID + " " + metric); }
            _proccesser.updateJob(jobID,metric);
            return new HttpAnswer(200,"request received");
        }
    }
}
