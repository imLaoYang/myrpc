package com.yang.config;

import com.yang.constant.ZookeeperConstant;
import com.yang.enums.CompressType;
import com.yang.enums.SerializeType;
import com.yang.loadbalance.LoadBalancer;
import com.yang.loadbalance.impl.RoundRobin;
import com.yang.utils.IdWorker;
import lombok.Data;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * 全局配置类
 */
@Data
public class Configuration {

  // 默认端口
  private int port = 8098;

  // 默认配置信息
  private String applicationName = "myrpc";

  // 序列化协议配置
  private String serializerType = SerializeType.HESSIAN.getType();
  // 压缩协议
  private String compressType = CompressType.GZIP.getType();

  // 负载均衡器
  private LoadBalancer loadbalancer = new RoundRobin();

  // 雪花ID,用作请求id
  private IdWorker idWorker = new IdWorker(0, 0);

  // 注册中心
  private RegistryConfig registryConfig = new RegistryConfig(ZookeeperConstant.DEFAULT_ZK_CONNECTION);
  // 序列化协议配置类
  private ProtocolConfig protocolConfig;

  // 读取xml
  public Configuration() {

  }


  public static void main(String[] args) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputStream myrpcXml = ClassLoader.getSystemClassLoader().getResourceAsStream("myrpc.xml");
      Document document = builder.parse(myrpcXml);
      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xPath = xPathFactory.newXPath();

      XPathExpression compile = xPath.compile("/configuration/compressType");
      Node node  = (Node) compile.evaluate(document, XPathConstants.NODE);
      System.out.println(node);
      String nodeValue = node.getAttributes().getNamedItem("class").getNodeValue();
      System.out.println(nodeValue);

    } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
      throw new RuntimeException(e);
    }

  }


}
