package com.yang.utils;

import com.yang.exception.NetException;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.util.Enumeration;

@Slf4j
public class NetUtils {


  public static final String DEFAULT_IP = "127.0.0.1";

  /**
   * 直接根据第一个网卡地址作为其内网ipv4地址，避免返回 127.0.0.1
   * @return
   */
  public static String getLocalIpByNetCard() {
    try {
      for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements(); ) {
        NetworkInterface item = e.nextElement();
        for (InterfaceAddress address : item.getInterfaceAddresses()) {
          if (item.isLoopback() || !item.isUp()) {
            continue;
          }
          if (address.getAddress() instanceof Inet4Address) {
            Inet4Address inet4Address = (Inet4Address) address.getAddress();
            log.debug("inet4Address是--->{}",inet4Address.getHostAddress());
            return inet4Address.getHostAddress();
          }
        }
      }
      log.debug("inet4Address是--->{}",InetAddress.getLocalHost().getHostAddress());
      return InetAddress.getLocalHost().getHostAddress();
    } catch (SocketException | UnknownHostException e) {
      log.error("获取ip异常",e);
      throw new NetException();
    }
  }

  public static String getLocalIP() {
    try {
      log.debug("inet4Address是--->{}",InetAddress.getLocalHost().getHostAddress());
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      log.error("获取ip异常",e);
      throw new NetException();
    }
  }


}
