package pt.tecnico.cnv.common;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by miguel on 12/05/17.
 */
public class GenericHandler implements HttpHandler {

    private final HttpStrategy _strategy;

    public GenericHandler(HttpStrategy strategy) {
        _strategy = strategy;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        String query = httpExchange.getRequestURI().getQuery();

        HttpAnswer answer = null;
        try {
            answer = _strategy.process(query);
        } catch (Exception e) {
            e.printStackTrace();
            answer = new HttpAnswer();
        }
        int requestStatus = answer.status();
        String message = answer.response();


        OutputStream os = httpExchange.getResponseBody();
        httpExchange.sendResponseHeaders(requestStatus, message.length());
        os.write(message.getBytes());
        os.close();
    }
}
