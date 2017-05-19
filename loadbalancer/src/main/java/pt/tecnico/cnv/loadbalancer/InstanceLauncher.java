package pt.tecnico.cnv.loadbalancer;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import pt.tecnico.cnv.common.STATIC_VALUES;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miguel on 17/05/17.
 */
public class InstanceLauncher {

    private static final String AMI_ID = STATIC_VALUES.AMI_ID ;
    private static final String INSTANCE_TYPE = "t2.micro";
    private static final String SECURITY_GROUP = "SecurityGroup1";
    private final AmazonEC2 ec2;

    public InstanceLauncher(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }


    //Returns the number of launched instances
    public void launchNewInstance(int numberOfInstance) {
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(AMI_ID)
                .withInstanceType(INSTANCE_TYPE)
                .withMinCount(numberOfInstance)
                .withMaxCount(numberOfInstance)
                .withSecurityGroups(SECURITY_GROUP);
        RunInstancesResult result = ec2.runInstances(runInstancesRequest);
        System.out.println("new instances: " + result);
    }

    //Returns the number of destroyed instances

    public List<InstanceStateChange> destroyInstances(List<String> instances_ids) {
        if(instances_ids.size() > 0) {
            TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
            terminateInstancesRequest.setInstanceIds(instances_ids);

            TerminateInstancesResult result = ec2.terminateInstances(terminateInstancesRequest);
            System.out.println("delete instances: " + result);
            List<InstanceStateChange> terminatedInstances = result.getTerminatingInstances();
            System.out.println("terminatedInstanceslist: " + terminatedInstances);
            return terminatedInstances;
        }
        return null;


    }
    public List<InstanceStateChange> destroyInstances(String instances_ids) {
        List<String> ids = new ArrayList<String>();
        ids.add(instances_ids);
        return destroyInstances(ids);
    }
}
