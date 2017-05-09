package pt.tecnico.cnv.loadbalancer;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by miguel on 09/05/17.
 */
public class ListWorkerInstances {
    private final static String WORKER_AMI_ID = "ami-48b2a62c";
    private static AmazonEC2 ec2;


    public ListWorkerInstances(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public List<Instance> listInstances() {
        List<Instance> instancesToReturn = new ArrayList<Instance>();
        try {
            List<String> instanceState = new ArrayList<String>();
            instanceState.add("running");
            Filter filter2 = new Filter("instance-state-name", instanceState);

            DescribeInstancesRequest request = new DescribeInstancesRequest();
            DescribeInstancesResult describeInstancesResult = ec2.describeInstances(request.withFilters(filter2));
            List<Reservation> reservations = describeInstancesResult.getReservations();
            Set<Instance> instances = new HashSet<Instance>();

            for (Reservation reservation : reservations) {
                instances.addAll(reservation.getInstances());
            }
            for (Instance instance : instances) {
                String ami = instance.getImageId();

                //ignore if not a worker
                if (!ami.equals(WORKER_AMI_ID)) {
                    continue;
                }
                instancesToReturn.add(instance);
            }
        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }
        return instancesToReturn;
    }
}
