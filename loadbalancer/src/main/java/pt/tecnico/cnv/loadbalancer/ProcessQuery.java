package pt.tecnico.cnv.loadbalancer;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;

import java.util.List;

/**
 * Created by miguel on 10/05/17.
 */
public class ProcessQuery {

    protected String _query;
    private int _status = 400;
    private String _message = "Error :(";
    private AmazonEC2 ec2;

    List<Instance>

    public ProcessQuery(AmazonEC2 ec2, String query)  {
        _query = query;
        this.ec2 = ec2;
    }

    public int metricValue() {
        //TODO ask mss the real value
        return 1;
    }

    public String process() {



    }


    private void updateInstances() {
        List<Instance> instances = new ListWorkerInstances(ec2).listInstances();
        for(Instance instance : instances) {

        }
    }

    public String response() {
        return _message;
    }

    public int status() {
        return _status;
    }
}
