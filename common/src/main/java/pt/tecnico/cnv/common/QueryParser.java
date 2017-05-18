package pt.tecnico.cnv.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by miguel on 16/04/17.
 */
public class QueryParser {
    protected String _inputFilename;
    protected String _sceneWidth_scols;
    protected String _sceneHeight_srows;
    protected String _windowWidth_wcols;
    protected String _windowHeight_wrows;
    protected String _columnOffset_coff;
    protected String _rowsOffset_roff;
    protected String _query;
    protected Map<String,String > _arguments;

    public QueryParser(String query) throws InvalidArgumentsException {
        _arguments = this.queryToMap(query);
        _query = query;
        _inputFilename 		= _arguments.get("f");
        _sceneWidth_scols 	= _arguments.get("sc");
        _sceneHeight_srows 	= _arguments.get("sr");
        _windowWidth_wcols 	= _arguments.get("wc");
        _windowHeight_wrows = _arguments.get("wr");
        _columnOffset_coff 	= _arguments.get("coff");
        _rowsOffset_roff 	= _arguments.get("roff");


        if(_inputFilename == null      || _inputFilename.equals("")) throw new InvalidArgumentsException();
        if(_sceneWidth_scols == null   || _sceneWidth_scols.equals("")) throw new InvalidArgumentsException();
        if(_sceneHeight_srows == null  || _sceneHeight_srows.equals("")) throw new InvalidArgumentsException();
        if(_windowWidth_wcols == null  || _windowWidth_wcols.equals("")) throw new InvalidArgumentsException();
        if(_windowHeight_wrows == null || _windowHeight_wrows.equals("")) throw new InvalidArgumentsException();
        if(_columnOffset_coff == null  || _columnOffset_coff.equals("")) throw new InvalidArgumentsException();
        if(_rowsOffset_roff == null    || _rowsOffset_roff.equals("")) throw new InvalidArgumentsException();

    }



    public static Map<String, String> queryToMap(String query){
        Map<String, String> result = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length>1) {
                result.put(pair[0], pair[1]);
            }else{
                result.put(pair[0], "");
            }
        }
        return result;
    }

    public String toString(){
        return "query: " + _query + "\n\n" +
                "f: " + _inputFilename + "\n" +
                "sc: " + _sceneWidth_scols + "\n" +
                "sr: " + _sceneHeight_srows + "\n" +
                "wc: " + _windowWidth_wcols + "\n" +
                "wr: " + _windowHeight_wrows + "\n" +
                "coff: " + _columnOffset_coff + "\n" +
                "roff: " + _rowsOffset_roff + "\n" ;
    }
    public String toHMTLString(){
        return "query: " + _query + "</br></br>\n\n" +
                "f: " + _inputFilename + "</br>\n" +
                "sc: " + _sceneWidth_scols + "</br>\n" +
                "sr: " + _sceneHeight_srows + "</br>\n" +
                "wc: " + _windowWidth_wcols + "</br>\n" +
                "wr: " + _windowHeight_wrows + "</br>\n" +
                "coff: " + _columnOffset_coff + "</br>\n" +
                "roff: " + _rowsOffset_roff + "</br>\n" ;
    }

    public String outputFileName() {
        return "images/" + "f_" + _inputFilename +
        "_sc_" + _sceneWidth_scols +
        "_sr_" + _sceneHeight_srows +
        "_wc_" + _windowWidth_wcols +
        "_wr_" + _windowHeight_wrows +
        "_coff_" + _columnOffset_coff +
        "_roff_" + _rowsOffset_roff+".bmp";
    }
}
