package pt.tecnico.cnv.storageserver;

import pt.tecnico.cnv.common.InvalidArgumentsException;
import pt.tecnico.cnv.common.QueryParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by miguel on 16/04/17.
 */
public class InstQueryParser extends QueryParser{


    protected String _instructions;
    protected String _bb_blocks;
    protected String _methods ;
    protected String _branch_fail;
    protected String _branch_success;
    protected String _jobID;


    public InstQueryParser(String query) throws InvalidArgumentsException {

        super(query);

        _instructions 		= _arguments.get("instructions");
        _bb_blocks 	= _arguments.get("bb_blocks");
        _methods 	= _arguments.get("methods");
        _branch_fail 	= _arguments.get("branch_fail");
        _branch_success = _arguments.get("branch_success");
        _jobID = _arguments.get("jobID");



        if(_instructions == null      || _instructions.equals("")) throw new InvalidArgumentsException();
        if(_bb_blocks == null   || _bb_blocks.equals("")) throw new InvalidArgumentsException();
        if(_methods == null  || _methods.equals("")) throw new InvalidArgumentsException();
        if(_branch_fail == null  || _branch_fail.equals("")) throw new InvalidArgumentsException();
        if(_branch_success == null || _branch_success.equals("")) throw new InvalidArgumentsException();
        if(_jobID == null || _jobID.equals("")) throw new InvalidArgumentsException();


    }

    @Override
    public String toString(){
        return "query: " + _query + "\n\n" +
                "f: " + _inputFilename + "\n" +
                "sc: " + _sceneWidth_scols + "\n" +
                "sr: " + _sceneHeight_srows + "\n" +
                "wc: " + _windowWidth_wcols + "\n" +
                "wr: " + _windowHeight_wrows + "\n" +
                "coff: " + _columnOffset_coff + "\n" +
                "instructions: " + _instructions + "\n" +
                "bb_blocks: " + _bb_blocks + "\n" +
                "methods: " + _methods + "\n" +
                "branch_fail: " + _branch_fail + "\n" +
                "branch_success: " + _branch_success + "\n" +
                "jobID: " + _jobID + "\n";
    }


    @Override
    public String toHMTLString(){
        return "query: " + _query + "</br></br>\n\n" +
                "f: " + _inputFilename + "</br>\n" +
                "sc: " + _sceneWidth_scols + "</br>\n" +
                "sr: " + _sceneHeight_srows + "</br>\n" +
                "wc: " + _windowWidth_wcols + "</br>\n" +
                "wr: " + _windowHeight_wrows + "</br>\n" +
                "coff: " + _columnOffset_coff + "</br>\n" +
                "roff: " + _rowsOffset_roff + "</br>\n" +
                "instructions: " + _instructions + "</br>\n" +
                "bb_blocks: " + _bb_blocks + "</br>\n" +
                "methods: " + _methods + "</br>\n" +
                "branch_fail: " + _branch_fail + "</br>\n" +
                "branch_success: " + _branch_success + "</br>\n" +
                "jobID: " + _jobID + "</br>\n" ;
    }

}
