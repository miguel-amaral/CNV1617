package pt.tecnico.cnv.webserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;



@SuppressWarnings("restriction")
public class WebServer {
	public static void main(String[] args) throws Exception {
		System.out.println("Booting server");
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/r.html", new MyHandler());
        server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
        server.start();
		System.out.println("Server is now running");

    }
    static class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();

            // InstrumentationData data = InstrumentationData.getInstance(Thread.currentThread().getId());
            // System.err.println(data.i_count + " instructions in " + data.b_count + " basic blocks were executed.");
            System.out.println(query);
            //TODO send data to MetricStorage
            OutputStream os = t.getResponseBody();
            String message = null;
            int requestStatus = 0;
			try {
				InvokeRay ray = new InvokeRay(query);
				ray.execute();
				message = "file: " + ray.outputFileName();
                requestStatus = 200;
			} catch (InvalidArgumentsException e) {
				e.printStackTrace();
				message = "Error: InvalidArgumentsException";
				requestStatus = 400;
                System.err.println(message);
			} catch (Throwable e) {
			    message = "Error: " + e.getMessage();
                System.err.println(message);
			}
            t.sendResponseHeaders(requestStatus, message.length());
            os.write(message.getBytes());

            os.close();
        }
    }



}
