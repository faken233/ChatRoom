package org.example.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkUtils {

    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.getName().equalsIgnoreCase("en0")) {
                    continue;
                }
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    // 排除回环地址和非本地地址（如：127.0.0.1）
                    if (!(inetAddress instanceof Inet4Address)) {
                        continue;
                    }
                    return inetAddress.getHostAddress();
                }
            }
        } catch (Exception e) {
            return null;
        }
        return "127.0.0.1";
    }
}

