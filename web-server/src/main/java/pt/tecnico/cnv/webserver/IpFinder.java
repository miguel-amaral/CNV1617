package pt.tecnico.cnv.webserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.*;


/**
 * Created by miguel on 16/04/17.
 */
public class IpFinder {
    private static String ip = null;
    private static final String error = "ERROR";
    public static String getMyIp() {
        if(ip != null) return ip;
        String systemipaddress = "";
        try {
            URL url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
            systemipaddress = sc.readLine().trim();
            if (!(systemipaddress.length() > 0)) {
                try {
                    InetAddress localhost = InetAddress.getLocalHost();
                    System.out.println((localhost.getHostAddress()).trim());
                    systemipaddress = (localhost.getHostAddress()).trim();
                } catch (Exception e1) {
                    systemipaddress = error;
                }
            }
        } catch (Exception e2) {
            systemipaddress = error;
        }
        if(systemipaddress != error) ip = systemipaddress;
        else ip = "localhost";
        return ip;
    }
}
