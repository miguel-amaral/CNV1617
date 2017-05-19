package pt.tecnico.cnv.common;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by miguel on 12/05/17.
 */
public class GenericHandler implements HttpHandler {

    private static final int BUFFER_SIZE = 1024;
    private final HttpStrategy _strategy;
    private boolean booleanSetHeaders = false;
    private String header = "";

    public GenericHandler(HttpStrategy strategy) {
        _strategy = strategy;
    }

    public GenericHandler(HttpStrategy strategy, String header){
        this(strategy);
        this.header = header;
        this.booleanSetHeaders  = true;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        String query = httpExchange.getRequestURI().getQuery();

        if(booleanSetHeaders) {
            Headers headers = httpExchange.getResponseHeaders();
            headers.add("Content-Type", this.header);
        }
        HttpAnswer answer = null;
        try {
            answer = _strategy.process(query);
        } catch (Exception e) {
            e.printStackTrace();
            answer = new HttpAnswer();
        }
        int requestStatus = answer.status();
        String message = answer.response();
        System.out.println(message);


        try {
            OutputStream os = httpExchange.getResponseBody();
            httpExchange.sendResponseHeaders(requestStatus, message.length());
            ByteArrayInputStream bis = new ByteArrayInputStream(message.getBytes());
            byte [] buffer = new byte [BUFFER_SIZE];
            int count ;
            while ((count = bis.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }



            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
