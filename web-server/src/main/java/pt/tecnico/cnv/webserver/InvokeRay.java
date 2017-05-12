package pt.tecnico.cnv.webserver;

import pt.tecnico.cnv.common.InvalidArgumentsException;
import pt.tecnico.cnv.common.QueryParser;

/**
 * Created by miguel on 16/04/17.
 */
public class InvokeRay extends QueryParser {

    protected String _jobID;

    public InvokeRay(String query) throws InvalidArgumentsException {
        super(query);
        _jobID = _arguments.get("jobID");
    }

    public void execute() throws Exception {
        String[] args = {
                _inputFilename,
                this.outputFileName(),
                _sceneWidth_scols,
                _sceneHeight_srows,
                _windowWidth_wcols,
                _windowHeight_wrows,
                _columnOffset_coff,
                _rowsOffset_roff
        };
        new raytracer.Main().createImage(args);
    }

    public String jobID() {
        return _jobID;
    }

}
