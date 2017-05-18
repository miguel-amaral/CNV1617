package pt.tecnico.cnv.webserver;

import pt.tecnico.cnv.common.MetricCalculation;
import pt.tecnico.cnv.common.STATIC_VALUES;
import tool.ContainerManager;
import tool.DataContainer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by miguel on 18/05/17.
 */
public class WebServerTimerTask extends TimerTask {
    private Timer _timer;
    private long _threadID;

    @Override
    public void run() {
        DataContainer data = ContainerManager.getInstance(_threadID);
        long metric = MetricCalculation.calculate(data.bb_blocks,data.methods,data.branch_fail);
        System.out.println(STATIC_VALUES.NUMBER_SECONDS_INTERVAL_WEB_SERVER_CHECKS_METRIC+ " seconds have passed : metric gone: " + metric);
        _timer.scheduleAtFixedRate(this, STATIC_VALUES.NUMBER_MILI_SECONDS_INTERVAL_WEB_SERVER_CHECKS_METRIC, STATIC_VALUES.NUMBER_MILI_SECONDS_INTERVAL_WEB_SERVER_CHECKS_METRIC);

        //do stuff
    }

    public void execute(){
        _timer = new Timer();
        run();
    }

    public void set_threadID(long _threadID) {
        this._threadID = _threadID;
    }
}
