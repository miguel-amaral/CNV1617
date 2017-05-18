package pt.tecnico.cnv.webserver;

import pt.tecnico.cnv.common.HttpRequest;
import pt.tecnico.cnv.common.MetricCalculation;
import pt.tecnico.cnv.common.STATIC_VALUES;
import tool.ContainerManager;
import tool.DataContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

/**
 * Created by miguel on 18/05/17.
 */
public class WebServerTimerTask extends TimerTask {
    private long _threadID;
    private String _jobID;

    @Override
    public void run() {
        DataContainer data = ContainerManager.getInstance(_threadID);
        long metric = MetricCalculation.calculate(data.bb_blocks,data.methods,data.branch_fail);
        if(STATIC_VALUES.DEBUG_LOAD_BALANCER_JOB_UPDATE) {System.out.println(_threadID + " : +" + STATIC_VALUES.NUMBER_SECONDS_INTERVAL_WEB_SERVER_CHECKS_METRIC+ " seconds : metric : " + metric); }

        Map<String,String> args =new HashMap<>();
        args.put("jobID",_jobID);
        args.put("metric", String.valueOf(metric));
        HttpRequest.sendGet("load-balancer-cnv.tk:8000/job/update",args);
    }

    public void set_threadID(long _threadID) {
        this._threadID = _threadID;
    }

    public void set_jobID(String _jobID) {
        this._jobID = _jobID;
    }
}
