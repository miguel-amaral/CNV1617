package pt.tecnico.cnv.loadbalancer;

import pt.tecnico.cnv.common.HttpAnswer;

/**
 * Created by miguel on 11/05/17.
 */
public abstract class HttpStrategy {
    abstract HttpAnswer process(String query);
}
