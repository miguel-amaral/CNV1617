package pt.tecnico.cnv.loadbalancer;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import pt.tecnico.cnv.common.HttpAnswer;
import pt.tecnico.cnv.common.HttpRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by miguel on 10/05/17.
 */
public class ProcessQuery {

    private AmazonEC2 ec2;

    List<Instance> _known_instances = new ArrayList<Instance>();
    List<String> _removed_instances = new ArrayList<String>();

    Map<String, Container> _instances = new HashMap<String, Container>();

    public ProcessQuery(AmazonEC2 ec2)  {
        this.ec2 = ec2;
        updateInstances();
    }

    public int metricValue() {
        //TODO ask mss the real value
        return 1;
    }

    public HttpAnswer process(String query) {

        //update all instances ??
        updateInstances();
        Instance lowestInsance = getLighestMachine();
        String ip = lowestInsance.getPublicIpAddress();
        System.out.println("lowest ip: " + ip);
        HttpAnswer answer = HttpRequest.sendGet(ip+":8000/r.html?"+query,new HashMap<String, String>());
        return answer;
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
            _instances.put(id,new Container(instance,0));
        }
    }

    private boolean instanceIsReady(Instance instance) {
        String publicIpAddress = instance.getPublicIpAddress();

        return true; //TODO

    }

    public String toString() {
        String newLine = "\n";
        StringBuilder toReturn = new StringBuilder("Instances:" + newLine);
        for(Map.Entry<String,Container> entry : _instances.entrySet()){
            toReturn.append(entry.getKey()).append(" : ").append(entry.getValue().metric);
        }
        toReturn.append(newLine).append("Removed:").append(newLine);
        for (String id : _removed_instances) {
            toReturn.append(id).append(newLine);
        }
        return toReturn.toString();
    }

    public Instance getLighestMachine() {
        int min = 0; //max int
        Instance minInstance = null;
        for(Map.Entry<String,Container> entry : _instances.entrySet()){
            if(entry.getValue().metric < min || minInstance == null) {
                minInstance = entry.getValue().instance;
                min = entry.getValue().metric;
            }
        }
        return minInstance;
    }

    class Container {
        Instance instance;
        int metric;
        Container(Instance instance, int metric) {
            this.instance = instance;
            this.metric = metric;
        }
    }



}
