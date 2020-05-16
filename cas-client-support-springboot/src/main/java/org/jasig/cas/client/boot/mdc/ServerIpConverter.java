package org.jasig.cas.client.boot.mdc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author lemon
 * @return
 * @description 获取服务器ip地址
 * @date 2020-05-09 16:01
 */
public class ServerIpConverter {
    private final static String serverIp;
    private static final Logger logger = LoggerFactory.getLogger(ServerIpConverter.class);

    static {
        serverIp = getLocalServerIp();
    }

    public static String getServerIp() {
        return ServerIpConverter.serverIp;
    }

    /**
     * @param
     * @return java.lang.String
     * @description 获取服务器IP
     * @author lemon
     * @date 2020-05-09 16:01
     */
    public static String getLocalServerIp() {
        String ip = "UNKNOWN";

        try {
            // 根据网卡取本机配置的IP 获得网络接口
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            // 声明一个InetAddress类型ip地址
            InetAddress inetAddress;

            // 遍历所有的网络接口
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                // 同样再定义网络地址枚举类
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    inetAddress = addresses.nextElement();

                    if (inetAddress.isLoopbackAddress()) {
                        continue;
                    }

                    // InetAddress类包括Inet4Address和Inet6Address
                    if (inetAddress instanceof Inet4Address) {
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Fetch host address failed", e);
        }

        return ip;
    }
}
