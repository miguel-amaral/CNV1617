package pt.tecnico.cnv.webserver;

import pt.tecnico.cnv.common.MetricCalculation;
import pt.tecnico.cnv.common.STATIC_VALUES;
import tool.ContainerManager;
import tool.DataContainer;

/**
 * Created by miguel on 18/05/17.
 */
public class WebServerRunnable implements Runnable {
    private long _threadID;

    @Override
    public void run() {
        DataContainer data = ContainerManager.getInstance(_threadID);
        long metric = MetricCalculation.calculate(data.bb_blocks,data.methods,data.branch_fail);
        System.out.println(STATIC_VALUES.NUMBER_SECONDS_INTERVAL_WEB_SERVER_CHECKS_METRIC+ " seconds have passed : metric gone: " + metric);
        //do stuff
    }

    public void set_threadID(long _threadID) {
        this._threadID = _threadID;
    }
}
