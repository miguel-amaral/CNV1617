package pt.tecnico.cnv.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by miguel on 11/05/17.
 */
public class HttpRequest {

    private final static String USER_AGENT = "Mozilla/5.0";

    // HTTP GET request
    public static HttpAnswer sendGet(String url) {
        return HttpRequest.sendGet(url,new HashMap<String, String>());
    }

    public static HttpAnswer sendGet(String url, Map<String,String> arguments){
        try {
            if(!url.substring(0,5).equals("http")) {
                url = "http://" + url;
            }
            if (arguments.size() > 0) url += "?";
            for (Map.Entry<String, String> entry : arguments.entrySet()) {
                url += entry.getKey() + "=" + entry.getValue() + "&";
            }
            int length = url.length();
            if (arguments.size() > 0) url = url.substring(0, length-1);
            if(STATIC_VALUES.DEBUG_HTTP_REQUEST) {
                System.out.println("url: " + url);
            }
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();

            if(STATIC_VALUES.DEBUG_HTTP_REQUEST) {
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);
            }
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            if(STATIC_VALUES.DEBUG_HTTP_REQUEST) {
                System.out.println("Answer: " + response.toString());
            }
            return new HttpAnswer(responseCode, response.toString());
        }catch (Exception e ) {
            e.printStackTrace();
            return new HttpAnswer();
        }
    }
}
