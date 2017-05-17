package pt.tecnico.cnv.loadbalancer;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import pt.tecnico.cnv.common.HttpAnswer;
import pt.tecnico.cnv.common.HttpRequest;
import pt.tecnico.cnv.common.STATIC_VALUES;

import java.util.*;

/**
 * Created by miguel on 10/05/17.
 */
public class LoadBalancer {

    private AmazonEC2 ec2;
    private InstanceLauncher instanceLauncher;
    private int _counterOfBootingInstances = 0;
    List<Instance> _known_instances = new ArrayList<Instance>();
    List<String> _instances_set_to_removal = new ArrayList<String>();

    Map<String, Container> _instances = new HashMap<String, Container>();

    Map<String,Container> _jobs = new HashMap<String, Container>();


    public LoadBalancer(AmazonEC2 ec2)  {
        this.ec2 = ec2;
        this.instanceLauncher = new InstanceLauncher(ec2);
        updateInstances();
    }

    private void increaseMetric(Instance instance, long metricValue) {
        synchronized (_instances) {
            _instances.get(instance.getInstanceId()).metric = _instances.get(instance.getInstanceId()).metric + metricValue;
        }
    }
    private void decreaseMetric(Instance instance, long metricValue) {
        synchronized (_instances) {
            _instances.get(instance.getInstanceId()).metric = _instances.get(instance.getInstanceId()).metric - metricValue;
        }
    }


    public void launchInstance(int numberOfInstances) {
        instanceLauncher.launchNewInstance(numberOfInstances);
    }

    public void terminateInstance(String instanceId) {
        instanceLauncher.destroyInstances(instanceId);
    }

    public HttpAnswer processQuery(String query, String letter) {
        while(true) {
            updateInstances();
            Instance lowestInsance = getLightestMachine();

            GetMetricValue requester = new GetMetricValue(query);

            long metricValue = requester.getMetric();
            boolean alreadyInstrumented = requester.isAlreadyIntrumented();
            this.increaseMetric(lowestInsance, metricValue);
            String jobID = newJobID();
            _jobs.put(jobID, new Container(lowestInsance, metricValue));

            String ip = lowestInsance.getPublicIpAddress();
            if (STATIC_VALUES.DEBUG_LOAD_BALANCER_CHOSEN_MACHINE) {
                System.out.println("ip: " + ip + " chosen for " + jobID);
            }

            //String letter = alreadyInstrumented ? "alreadyInstrumented" : "r" ;


            if (STATIC_VALUES.DEBUG_LOAD_BALANCER_JOB_ALREADY_INSTRUCTED) {
                System.out.println("jobID: " + jobID + " already instrument: " + alreadyInstrumented);
            }
            HttpAnswer answer = HttpRequest.sendGet(ip + ":8000/" + letter + "?" + query + "&jobID=" + jobID, new HashMap<String, String>());
            if(answer.status() != 200) {
                continue;
            } else {
                return answer;
            }
        }
    }

    private synchronized String newJobID(){
        return UUID.randomUUID().toString();
    }

    private void updateInstances() {
        List<Instance> instances = new ListWorkerInstances(ec2).listInstances();
        synchronized (_instances) {
            List<String> keys = new ArrayList<String>(_instances.keySet());
            for (Instance instance : instances) {
                String id = instance.getInstanceId();
                if (!_instances.containsKey(id)) {
                    this.newInstance(instance);
                } else {
                    keys.remove(id);
                }
            }
            for (String zombieId : keys) {
                System.out.println("We have " + zombieId + " down :(");
                _instances.remove(zombieId);
            }
        }
    }

    private void newInstance(Instance instance) {
        String id = instance.getInstanceId();
        if(_instances_set_to_removal.contains(id)) {
            //it is shutting down ..
            return;
        }
        if(this.instanceIsReady(instance)){
            synchronized (_instances) {
                _instances.put(id,new Container(instance,0));
            }
        }
    }

    private boolean instanceIsReady(Instance instance) {
        String publicIpAddress = instance.getPublicIpAddress();
        HttpAnswer answer = HttpRequest.sendGet(publicIpAddress+":8000/ping", new HashMap<String, String>());
        if(STATIC_VALUES.DEBUG_LOAD_BALANCER_WEB_SERVER_READY) {
            System.out.println("instance: " + publicIpAddress + " is ready: " + (answer.status() == 200));
        }
        return answer.status() == 200;
    }

    public String toString() {
        String newLine = "\n";
        StringBuilder toReturn = new StringBuilder("Instances:" + newLine);
        synchronized (_instances) {
            for (Map.Entry<String, Container> entry : _instances.entrySet()) {
                toReturn.append(entry.getKey()).append(" : ").append(entry.getValue().metric).append(newLine);
            }
        }
        toReturn.append(newLine).append("Removed:").append(newLine);
        for (String id : _instances_set_to_removal) {
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
        long min = Long.MAX_VALUE; //max int

        List<Instance> minInstance = null;
        synchronized (_instances) {
            for (Map.Entry<String, Container> entry : _instances.entrySet()) {
                if (entry.getValue().metric < min || minInstance == null) {
                    minInstance = new ArrayList<Instance>();
                    minInstance.add(entry.getValue().instance);
                    min = entry.getValue().metric;
                } else if (entry.getValue().metric == min) {
                    minInstance.add(entry.getValue().instance);
                }
            }
        }
        int index = 0;
        int size = minInstance.size();
        if(size > 1) {
            Random rn = new Random();
            index = rn.nextInt(size);
        }
        return minInstance.get(index);
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
    }
}
