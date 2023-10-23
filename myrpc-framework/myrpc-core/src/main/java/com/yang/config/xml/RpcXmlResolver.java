package com.yang.config.xml;

import com.yang.config.Configuration;
import com.yang.config.RegistryConfig;
import com.yang.constant.ZookeeperConstant;
import com.yang.enums.CompressType;
import com.yang.enums.SerializeType;
import com.yang.loadbalance.LoadBalancer;
import com.yang.loadbalance.impl.RoundRobin;
import com.yang.utils.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * xml解析器
 * 使用xpath
 */
@Slf4j
public class RpcXmlResolver {

  // xpath表达式
  private final String baseExpression = "/configuration/";

  /**
   * 加载xml的配置注入Configuration
   *
   * @param configuration 全局配置类
   */
  public void loadXml(Configuration configuration) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // 关闭dtd校验
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      DocumentBuilder builder = factory.newDocumentBuilder();
      // 加载myrpc.xml
      InputStream myrpcXml = ClassLoader.getSystemClassLoader().getResourceAsStream("myrpc.xml");
      Document document = builder.parse(myrpcXml);
      XPathFactory xPathFactory = XPathFactory.newInstance();
      // xpath解析器
      XPath xPath = xPathFactory.newXPath();

      // 解析xml,注入配置类
      configuration.setPort(resolverPort(xPath, document, baseExpression + "port"));
      configuration.setApplicationName(resolverApplicationName(xPath, document, baseExpression + "applicationName"));
      RegistryConfig registryConfig = configuration.getRegistryConfig();
      registryConfig.setUrl(resolverRegistryConfig(xPath, document, baseExpression + "registryConfig", "url"));
      configuration.setSerializerType(resolverSerializerType(xPath, document, baseExpression + "serializerType", "type"));
      configuration.setCompressType(resolverCompressType(xPath, document, baseExpression + "compressType", "type"));
      configuration.setLoadbalancer(resolverLoadbalancer(xPath, document, baseExpression + "loadbalancer", "class"));
      configuration.setIdWorker(resolverIdWorker(xPath, document, baseExpression + "idWorker", "class", "workerId", "datacenterId"));

      System.out.println(configuration);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      log.error("加载xml异常", e);
      throw new RuntimeException(e);
    }


  }

  /**
   * 解析IdWorker和参数
   *
   * @param xPath      xpath解析器
   * @param document   xml文件
   * @param expression xpath表达式
   * @param namedItem  节点属性名称
   * @param attr1      参数1 idWork
   * @param arrt2      参数2 datacenterId
   * @return IdWorker实例
   */
  private IdWorker resolverIdWorker(XPath xPath, Document document, String expression, String namedItem, String attr1, String arrt2) {
    String workId = parseString(xPath, document, expression, attr1);
    String datacenterId = parseString(xPath, document, expression, arrt2);
    if (workId == null && datacenterId == null) {
      return new IdWorker(0,0);
    }
    Class<?>[] classes = {long.class, long.class};
    return  (IdWorker) parseObject(xPath, document, expression, namedItem, classes, Long.parseLong(workId), Long.parseLong(datacenterId));
  }


  /**
   * 解析负载均衡器
   *
   * @param xPath      xpath解析器
   * @param document   xml文件
   * @param expression xpath表达式
   * @param namedItem  节点属性名称
   * @return 负载均衡器
   */
  private LoadBalancer resolverLoadbalancer(XPath xPath, Document document, String expression, String namedItem) {
    LoadBalancer loadbalancer = (LoadBalancer) parseObject(xPath, document, expression, namedItem, null);
    if (loadbalancer == null) {
      return new RoundRobin();
    }
    return loadbalancer;
  }

  /**
   * 解析压缩协议
   *
   * @param xPath      xpath解析器
   * @param document   xml文件
   * @param expression xpath表达式
   * @param namedItem  节点属性名称
   * @return 压缩协议名
   */
  private String resolverCompressType(XPath xPath, Document document, String expression, String namedItem) {
    String type = parseString(xPath, document, expression, namedItem);
    if (type == null) {
      return CompressType.GZIP.getType();
    }
    return type;
  }

  /**
   * 解析序列化协议
   *
   * @param xPath      xpath解析器
   * @param document   xml文件
   * @param expression xpath表达式
   * @param namedItem  节点属性名称
   * @return 协议类型
   */
  private String resolverSerializerType(XPath xPath, Document document, String expression, String namedItem) {
    String type = parseString(xPath, document, expression, namedItem);
    if (type == null) {
      return SerializeType.HESSIAN.getType();
    }
    return type;

  }

  /**
   * 解析RegistryConfig
   *
   * @param xPath      xpath解析器
   * @param document   xml文件
   * @param expression xpath表达式
   * @param namedItem  节点属性名称
   * @return 注册中心的url
   */
  private String resolverRegistryConfig(XPath xPath, Document document, String expression, String namedItem) {
    String url = parseString(xPath, document, expression, namedItem);
    if (url == null){
      return ZookeeperConstant.DEFAULT_ZK_CONNECTION;
    }
    return url;
  }

  /**
   * 解析applicationName
   *
   * @param xPath      xpath解析器
   * @param document   xml文件
   * @param expression xpath表达式
   * @return 实例名称
   */
  private String resolverApplicationName(XPath xPath, Document document, String expression) {
    String name = parseString(xPath, document, expression);
    if (name == null){
      return "default";
    }
    return name;
  }

  /**
   * 解析端口
   *
   * @param xPath      xpath解析器
   * @param document   xml文件
   * @param expression xpath表达式
   * @return 端口号
   */
  private int resolverPort(XPath xPath, Document document, String expression) {
    String port = parseString(xPath, document, expression);
    if (port == null){
      return 8091;
    }
    return Integer.parseInt(port);
  }


  /*
   * ---------------------------------------解析xml的方法封装-------------------------------------
   */


  /**
   * 获得xml节点的属性值 <port num="8099"/>
   *
   * @param xPath      xpath解析器
   * @param document   xml文件
   * @param expression xpath表达式
   * @param NamedItem  xml节点的属性名称
   * @return 节点的值
   */
  private static String parseString(XPath xPath, Document document, String expression, String NamedItem) {


    try {
      XPathExpression compile = xPath.compile(expression);
      Node node = (Node) compile.evaluate(document, XPathConstants.NODE);
      if (node == null){
        return null;
      }
      return node.getAttributes().getNamedItem(NamedItem).getNodeValue();
    } catch (XPathExpressionException e) {
      log.error("xml节点解析异常", e);
      throw new RuntimeException(e);
    }
  }


  /**
   * 获取节点的文本值  <port>8099</port>
   *
   * @param xPath      xpath解析器
   * @param document   xml文件
   * @param expression xpath表达式
   * @return <config>返回值</config>
   */
  private static String parseString(XPath xPath, Document document, String expression) {
    try {
      XPathExpression compile = xPath.compile(expression);
      Node node = (Node) compile.evaluate(document, XPathConstants.NODE);
      if (node == null){
        return null;
      }
      return node.getTextContent();
    } catch (XPathExpressionException e) {
      log.error("xml节点解析异常", e);
      throw new RuntimeException(e);
    }
  }


  /**
   * 根据读取的xml配置,解析成对象实例
   *
   * @param xPath      xpath解析器
   * @param document   xml文件
   * @param expression xpath表达式
   * @param NamedItem  xml节点的属性名称
   * @param pramsType  参数类型
   * @param prams      参数
   * @return 反射的实例
   */
  private static Object parseObject(XPath xPath, Document document, String expression, String NamedItem, Class<?>[] pramsType, Object... prams) {
    try {
      XPathExpression compile = xPath.compile(expression);
      Node node = (Node) compile.evaluate(document, XPathConstants.NODE);
      if (node == null) {
        return null;
      }
      // 类路径
      String className = node.getAttributes().getNamedItem(NamedItem).getNodeValue();
      // 加载类
      Class<?> aClass = Class.forName(className);
      Object instance = null;
      if (pramsType == null) {
        // 根据无参构造创建实例
        instance = aClass.getConstructor().newInstance();
      } else {
        // 根据有参构造创建实例
        instance = aClass.getConstructor(pramsType).newInstance(prams);
      }

      return aClass.cast(instance);
    } catch (XPathExpressionException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
             IllegalAccessException | InvocationTargetException e) {
      log.error("xml节点解析异常", e);
      throw new RuntimeException(e);
    }
  }


}
