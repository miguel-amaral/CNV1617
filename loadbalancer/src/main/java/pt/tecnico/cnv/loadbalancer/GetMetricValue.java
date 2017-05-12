package pt.tecnico.cnv.loadbalancer;

/**
 * Responsible for communicating with the MSS
 *
 * Created by miguel on 12/05/17.
 *
 */
public class GetMetricValue {

    private long metric;
    private boolean alreadyIntrumented;

    public GetMetricValue(String query) {
        metric = 1;
        alreadyIntrumented = false;
        //TODO
    }


    public boolean isAlreadyIntrumented() {
        return alreadyIntrumented;
    }

    public long getMetric() {
        return metric;
    }
}
