package pt.tecnico.cnv.loadbalancer;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import pt.tecnico.cnv.common.HttpAnswer;
import pt.tecnico.cnv.common.HttpRequest;

import java.util.*;

/**
 * Created by miguel on 10/05/17.
 */
public class LoadBalancer {

    private AmazonEC2 ec2;

    List<Instance> _known_instances = new ArrayList<Instance>();
    List<String> _removed_instances = new ArrayList<String>();

    Map<String, Container> _instances = new HashMap<String, Container>();
    Map<String,Container> _jobs = new HashMap<String, Container>();


    public LoadBalancer(AmazonEC2 ec2)  {
        this.ec2 = ec2;
        updateInstances();
    }

    private synchronized void increaseMetric(Instance instance, long metricValue) {
        _instances.get(instance.getInstanceId()).metric = _instances.get(instance.getInstanceId()).metric + metricValue;
    }
    private synchronized void decreaseMetric(Instance instance, long metricValue) {
        _instances.get(instance.getInstanceId()).metric = _instances.get(instance.getInstanceId()).metric - metricValue;
    }


    public HttpAnswer processQuery(String query) {

        //update all instances ??
        updateInstances();
        Instance lowestInsance = getLightestMachine();

        GetMetricValue requester = new GetMetricValue(query);

        long metricValue = requester.getMetric();
        boolean alreadyInstrumented = requester.isAlreadyIntrumented();
        this.increaseMetric(lowestInsance,metricValue);
        String jobID = newJobID();
        _jobs.put(jobID,new Container(lowestInsance,metricValue));

        String ip = lowestInsance.getPublicIpAddress();
        System.out.println("lowest ip: " + ip);

        String letter = alreadyInstrumented ? "alreadyInstrumented" : "r" ;
        HttpAnswer answer = HttpRequest.sendGet(ip+":8000/"+letter+".html?"+query+"&jobID="+jobID,new HashMap<String, String>());
        return answer;
    }

    private synchronized String newJobID(){
        return UUID.randomUUID().toString();
    }

    private void updateInstances() {
        List<Instance> instances = new ListWorkerInstances(ec2).listInstances();
        for(Instance instance : instances) {
            String id = instance.getInstanceId();
            if(!_instances.containsKey(id)) {
                this.newInstance(instance);
            }
        }
    }

    private void newInstance(Instance instance) {
        String id = instance.getInstanceId();
        if(_removed_instances.contains(id)) {
            //it is shutting down ..
            return;
        }
        if(this.instanceIsReady(instance)){
            System.out.println("New instance: " + instance.getPublicIpAddress());
            _instances.put(id,new Container(instance,0));
        }
    }

    private boolean instanceIsReady(Instance instance) {
        String publicIpAddress = instance.getPublicIpAddress();
        HttpAnswer answer = HttpRequest.sendGet(publicIpAddress+":8000/ping", new HashMap<String, String>());
        System.out.println("instance: "+publicIpAddress+" is ready: " + (answer.status() == 200));
        return answer.status() == 200;
    }

    public String toString() {
        String newLine = "\n";
        StringBuilder toReturn = new StringBuilder("Instances:" + newLine);
        for(Map.Entry<String,Container> entry : _instances.entrySet()){
            toReturn.append(entry.getKey()).append(" : ").append(entry.getValue().metric).append(newLine);
        }
        toReturn.append(newLine).append("Removed:").append(newLine);
        for (String id : _removed_instances) {
            toReturn.append(id).append(newLine);
        }
        toReturn.append(newLine);
        toReturn.append(newLine).append("Current Jobs:").append(newLine);
        for(Map.Entry<String,Container> entry : _jobs.entrySet()){
            toReturn.append(entry.getKey()).append(" : ").append(entry.getValue().instance.getPublicIpAddress()).append(newLine);
        }
        return toReturn.toString();
    }

    public Instance getLightestMachine() {
        long min = 0; //max int
        Instance minInstance = null;
        for(Map.Entry<String,Container> entry : _instances.entrySet()){
            if(entry.getValue().metric < min || minInstance == null) {
                minInstance = entry.getValue().instance;
                min = entry.getValue().metric;
            }
        }
        return minInstance;
    }

    public void jobDone(String jobId) {
        Container job = _jobs.get(jobId);
        this.decreaseMetric(job.instance,job.metric);
        _jobs.remove(jobId);
    }


    class Container {
        Instance instance;
        long metric;
        Container(Instance instance, long metric) {
            this.instance = instance;
            this.metric = metric;
        }
    }}
