package pt.tecnico.cnv.loadbalancer;

/**
 * Created by miguel on 10/05/17.
 */
public class ProcessQuery {

    protected String _query;
    private int _status = 400;
    private String _message = "Error :(";

    public ProcessQuery(String query)  {
        _query = query;
    }

    public int metricValue() {
        //TODO ask mss the real value
        return 1;
    }

    public String process() {
        new ListWorkerInstances();
    }


    public String response() {
        return _message;
    }

    public int status() {
        return _status;
    }
}
