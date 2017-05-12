package pt.tecnico.cnv.common;

/**
 * Created by miguel on 11/05/17.
 */
public abstract class HttpStrategy {
    public abstract HttpAnswer process(String query) throws Exception;
}
