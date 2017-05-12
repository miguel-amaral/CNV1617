package pt.tecnico.cnv.loadbalancer;

import pt.tecnico.cnv.common.HttpAnswer;
import pt.tecnico.cnv.common.HttpRequest;
import pt.tecnico.cnv.common.STATIC_VALUES;

import java.util.HashMap;

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

        HttpAnswer answer = HttpRequest.sendGet("storage-server-cnv.tk:8000/metric/value?"+query,new HashMap<String, String>());
        if(answer.status() != 200) {
            System.out.println("Storage Server error!!!");
            return;
        } else {
            String message = answer.response();
            String[] params = message.split(STATIC_VALUES.SEPARATOR_STORAGE_METRIC_REQUEST);

            alreadyIntrumented = Boolean.parseBoolean(params[0]);
            metric = Long.parseLong(params[1]);
        }
    }


    public boolean isAlreadyIntrumented() {
        return alreadyIntrumented;
    }

    public long getMetric() {
        return metric;
    }
}
