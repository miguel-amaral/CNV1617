package pt.tecnico.cnv.webserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.tecnico.cnv.common.HttpAnswer;
import pt.tecnico.cnv.common.HttpRequest;
import pt.tecnico.cnv.common.InvalidArgumentsException;
import tool.ContainerManager;
import tool.DataContainer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;



@SuppressWarnings("restriction")
public class WebServer {
    private static int port = 8000;

	public static void main(String[] args) throws Exception {
		System.out.println("Booting server");
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/ping", new PingHandler());
        server.createContext("/r.html", new MyHandler());
        server.createContext("/images/", new GetImageHandler());
        server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
        server.start();
		System.out.println("Server is now running");
    }
	
    static class PingHandler implements HttpHandler {
	public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();

            System.out.println(query);
            OutputStream os = t.getResponseBody();
            String message = "ok";
            int requestStatus = 200;
            t.sendResponseHeaders(requestStatus, message.length());
            os.write(message.getBytes());
            os.close();
        }
    }
    static class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();

            OutputStream os = t.getResponseBody();
            String message = null;
            int requestStatus = 0;

            long start = System.currentTimeMillis();

			try {
			    System.out.println("thread: "+Thread.currentThread().getId());
			    ContainerManager.clearInstance(Thread.currentThread().getId());
				InvokeRay ray = new InvokeRay(query);
				message = "<pre>"+ray.toHMTLString()+"</br>";
//				System.out.println(message);
				ray.execute();
                String ip = "http://"+IpFinder.getMyIp() + ":"+port+"/";
                String localhost =  "http://localhost:"+port+"/";
				message += "<b>Amazon AWS context: </b><a href=\""+ip+ray.outputFileName()+"\">"+ip+ray.outputFileName()+"</a></br></br></br></br></br></br>";
				message += "      <b>Home context: </b><a href=\""+localhost +ray.outputFileName()+"\">"+localhost +ray.outputFileName()+"</a></pre>";
                requestStatus = 200;

                DataContainer data = ContainerManager.getInstance(Thread.currentThread().getId());
                message += "<pre>" +
                        "instru: " + data.instructions + "\n" +
                        "blocks: " + data.bb_blocks + "\n" +
                        "method: " + data.methods + "\n" +
                        "b_fail: " + data.branch_fail + "\n" +
                        "b_hits: " + data.branch_success + "\n" +

                        "</pre>";

                  String jobID = ray.jobID();
//                System.out.println("instructions: " + data.instructions);
//                System.out.println("bb_blocks: " + data.bb_blocks);
//                System.out.println("methods: " + data.methods);
//                System.out.println("branch_fail: " + data.branch_fail);
//                System.out.println("branch_success: " + data.branch_success);


                long elapsedTimeMillis = System.currentTimeMillis()-start;

                // Get elapsed time in seconds
                float elapsedTimeSec = elapsedTimeMillis/1000F;

                // Get elapsed time in minutes
                float elapsedTimeMin = elapsedTimeMillis/(60*1000F);

                message += "<b>Elapsed time: </b>" +  "<pre>" + elapsedTimeSec + "\n </pre>";

                Map<String,String> args =new HashMap<>();
                args.put("jobID",jobID);
                HttpAnswer answer_load_balancer = HttpRequest.sendGet("load-balancer-cnv.tk:8000/job/done",args);
                System.out.println(answer_load_balancer);


                String storageEndpoint = "data.html";
                String instructions_key = "instructions";
                String bb_blocks_key = "bb_blocks";
                String methods_key = "methods";
                String branch_fail_key = "branch_fail";
                String branch_success_key = "branch_success";

                List<String> keys = new ArrayList<>();
                keys.add(instructions_key);
                keys.add(bb_blocks_key);
                keys.add(methods_key);
                keys.add(branch_fail_key);
                keys.add(branch_success_key);

                List<String> values = new ArrayList<>();
                values.add(data.instructions+"");
                values.add(data.bb_blocks+"");
                values.add(data.methods+"");
                values.add(data.branch_fail+"");
                values.add(data.branch_success+"");

                StringBuilder finalEndpoint = new StringBuilder("storage-server-cnv.tk:8000/" + storageEndpoint + "?" + query);
                for(int i = 0 ; i<keys.size();i++) {
                    finalEndpoint.append("&").append(keys.get(i)).append("=").append(values.get(i));
                }

                HttpAnswer answer_storage_server = HttpRequest.sendGet(finalEndpoint.toString());
                System.out.println(finalEndpoint.toString());
                System.out.println(answer_storage_server);
                message += "<b>Storage server answer: </b>" +  "<pre>" + answer_storage_server+ "\n </pre>";

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
