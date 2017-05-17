package pt.tecnico.cnv.loadbalancer;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miguel on 17/05/17.
 */
public class InstanceLauncher {

    private static final String ami_id = "ami-48b2a62c";
    private static final String instance_type = "t2.micro";
    private static final String SECURITY_GROUP = "SecurityGroup1";
    private final AmazonEC2 ec2;

    public InstanceLauncher(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public void launchNewInstance(int numberOfInstance) {
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(ami_id)
                .withInstanceType(instance_type)
                .withMinCount(numberOfInstance)
                .withMaxCount(numberOfInstance)
                .withSecurityGroups(SECURITY_GROUP);
        RunInstancesResult result = ec2.runInstances(runInstancesRequest);
    }

    public void destroyInstances(String instances_ids) {
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
        List<String> ids = new ArrayList<String>();
        ids.add(instances_ids);

        terminateInstancesRequest.setInstanceIds(ids);

        TerminateInstancesResult result = ec2.terminateInstances(terminateInstancesRequest);
    }
}
