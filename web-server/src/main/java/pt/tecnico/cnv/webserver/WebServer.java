package pt.tecnico.cnv.webserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;



@SuppressWarnings("restriction")
public class WebServer {
	public static void main(String[] args) throws Exception {
		System.out.println("Booting server");
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/r.html", new MyHandler());
        server.createContext("/images/", new GetImageHandler());
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
                String ip = IpFinder.getMyIp() + ":8000/";
                String localhost =  "localhost:8000/";
				message = "<p><b>Amazon AWS context: </b><a href=\""+ip+ray.outputFileName()+"\">"+ip+ray.outputFileName()+"</a></br></br></br></br></br></br>";
				message += "<b>Home context: </b><a href=\""+localhost +ray.outputFileName()+"\">"+localhost +ray.outputFileName()+"</a></p>";
                requestStatus = 200;
			} catch (InvalidArgumentsException e) {
				e.printStackTrace();
				message = "Error: InvalidArgumentsException";
				requestStatus = 400;
                System.err.println(message);
			} catch (Throwable e) {
                e.printStackTrace();
                message = "Error: " + e.getMessage();
                System.err.println(message);
			}
            t.sendResponseHeaders(requestStatus, message.length());
            os.write(message.getBytes());

            os.close();
        }
    }
    static class GetImageHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange he) throws IOException {
            String filePath = he.getRequestURI().toString().substring(1);;
            System.out.println("The filePath: " + filePath );

            Headers headers = he.getResponseHeaders();
            headers.add("Content-Type", "image/bmp");

            File file = new File(filePath);
            byte[] bytes  = new byte [(int)file.length()];
            System.out.println(file.getAbsolutePath());
            System.out.println("length:" + file.length());

            OutputStream outputStream = he.getResponseBody();
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                bufferedInputStream.read(bytes, 0, bytes.length);
                if(bytes.length == 0) {
                    throw new IOException("File not found : " + filePath);
                }
                he.sendResponseHeaders(200, file.length());
                outputStream.write(bytes, 0, bytes.length);
            } catch (IOException e) {
                e.printStackTrace();
                String message = "Error: " + e.getMessage();
                he.sendResponseHeaders(400, message.length());
                outputStream.write(message.getBytes());
            }
            outputStream.close();


        }
    }

}
