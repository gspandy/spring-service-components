/**
 * Developer: Kadvin Date: 14-5-19 下午2:19
 */
package net.happyonroad.util;

import net.happyonroad.model.SocketAddress;
import net.happyonroad.support.DefaultSocketAddress;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

/**
 * IP地址工具
 */
public final class IpUtils {


    public static List<String> getLocalAddresses(){
        List<IndexAndIp> localAddresses = new ArrayList<IndexAndIp>(2);
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                Enumeration<InetAddress> addresses = nic.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    String hostAddress = address.getHostAddress();
                    if(hostAddress.contains(":")) continue; // ipv6
                    if("127.0.0.1".equals(hostAddress)) continue;
                    localAddresses.add(new IndexAndIp(nic.getIndex() , hostAddress) );
                }
            }
        } catch (Exception e) {
            throw new UnsupportedOperationException("Can't find local address", e);
        }
        //按照网卡接口号先后排序
        Collections.sort(localAddresses);
        List<String> addresses = new ArrayList<String>();
        for (IndexAndIp indexAndIp : localAddresses) {
            addresses.add(indexAndIp.ip);
        }
        return addresses;
    }

    /**
     * <h3>规范化MAC地址</h3>
     * MAC地址有多种表达形式，可能以空格，减号，冒号分割，规范化之后，都以冒号分割
     *
     * @param macAddress mac地址
     * @return 规范化之后的mac地址
     */
    public static String regularMAC(String macAddress) {
        return macAddress.replaceAll("[\\s:-]", ":").toLowerCase();
    }

    /**
     * <h2>将形如 主机:端口 的地址转换为SocketAddress</h2>
     * @param address  形如 主机:端口
     * @return SocketAddress
     */
    public static SocketAddress parseSocketAddress(String address){
        String[] hostAndPort = address.split(":");
        if( hostAndPort.length != 2 ){
            throw new IllegalArgumentException("The address should be formatted as host:port, " +
                                               "instead of " + address);
        }
        String host = hostAndPort[0];
        int port;
        try {
            port = Integer.valueOf(hostAndPort[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The address should be formatted as host:port, " +
                                               "instead of " + address);
        }
        return new DefaultSocketAddress(host, port);
    }


    static class IndexAndIp implements Comparable<IndexAndIp>{
        private int index;
        private String ip;

        public IndexAndIp(int index, String ip) {
            this.index = index;
            this.ip = ip;
        }

        @Override

        public int compareTo(@SuppressWarnings("NullableProblems") IndexAndIp another) {
            return this.index - another.index;
        }
    }
}
