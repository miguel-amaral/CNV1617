package pt.tecnico.cnv.common;

/**
 * Created by miguel on 11/05/17.
 */
public class HttpAnswer {
    private int _status = 400;
    private String _message = "Error :(";

    public HttpAnswer(){}

    public HttpAnswer(int status, String message) {
        _message = message;
        _status = status;
    }

    public String response() {
        return _message;
    }

    public int status() {
        return _status;
    }

    public String toString() {
        return "HttpAnswer: " + _status + " : " + _message;
    }
}
