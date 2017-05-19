package pt.tecnico.cnv.loadbalancer;

import java.util.TimerTask;

/**
 * Created by miguel on 19/05/17.
 */
public class AutoScaleAnalistAux extends TimerTask {

    private LoadBalancer loadBalancer;

    AutoScaleAnalistAux(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @Override
    public void run() {
        loadBalancer.autoScaleAnalisis();
    }
}
