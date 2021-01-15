package com.luqi.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

public class Ip {
	public static String CURRENT_SERVER="124.237.121.46";//当前服务器的ip地址
	public static void main(String[] args) {
        try {
            //用 getLocalHost() 方法创建的InetAddress的对象
            InetAddress address = InetAddress.getLocalHost();
            System.out.println(address.getHostName());//主机名
            System.out.println(address.getCanonicalHostName());//主机别名
            System.out.println(address.getHostAddress());//获取IP地址
            System.out.println("===============");
            
            //用域名创建 InetAddress对象
            InetAddress address1 = InetAddress.getByName("www.wodexiangce.cn");
            //获取的是该网站的ip地址，如果我们所有的请求都通过nginx的，所以这里获取到的其实是nginx服务器的IP地址
            System.out.println(address1.getHostName());//www.wodexiangce.cn
            System.out.println(address1.getCanonicalHostName());//124.237.121.122
            System.out.println(address1.getHostAddress());//124.237.121.122
            System.out.println("===============");
            
            //用IP地址创建InetAddress对象
            InetAddress address2 = InetAddress.getByName("220.181.111.188");
            System.out.println(address2.getHostName());//220.181.111.188
            System.out.println(address2.getCanonicalHostName());//220.181.111.188
            System.out.println(address2.getHostAddress());//220.181.111.188
            System.out.println("===============");
            
            //根据主机名返回其可能的所有InetAddress对象
            InetAddress[] addresses = InetAddress.getAllByName("www.baidu.com");
            for (InetAddress addr : addresses) {
                System.out.println(addr);
                //www.baidu.com/220.181.111.188
                //www.baidu.com/220.181.112.244
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
	
	/**
	 * 获取IP
	 * @param request
	 * @return
	 */
	public static String getIP(HttpServletRequest request){
		String ip = request.getRemoteAddr();
		System.out.println("ip:"+ip);
		String headerIP = request.getHeader("x-real-ip");
		if(headerIP == null || "".equals(headerIP) || "null".equals(headerIP)){
			headerIP = request.getHeader("x-forwarded-for");
		}
		System.out.println("headerIP:"+headerIP);
		if(headerIP !=null && !"".equals(headerIP) && !"null".equals(headerIP)){
			ip = headerIP;
		}
		return ip;
	}
	public static String getIp(HttpServletRequest request) throws Exception {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null){
            if (!ip.isEmpty() && !"unKnown".equalsIgnoreCase(ip)) {
                int index = ip.indexOf(",");
                if (index != -1) {
                    return ip.substring(0, index);
                } else {
                    return ip;
                }
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null) {
            if (!ip.isEmpty() && !"unKnown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null) {
            if (!ip.isEmpty() && !"unKnown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip != null) {
            if (!ip.isEmpty() && !"unKnown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        ip =  request.getRemoteAddr();
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }
}
