package pt.tecnico.cnv.webserver;

import pt.tecnico.cnv.common.InvalidArgumentsException;
import pt.tecnico.cnv.common.QueryParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by miguel on 16/04/17.
 */
public class InvokeRay extends QueryParser {

    public InvokeRay(String query) throws InvalidArgumentsException {
        super(query);
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


}
