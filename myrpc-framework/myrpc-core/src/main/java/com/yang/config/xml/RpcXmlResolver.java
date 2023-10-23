package com.yang.config.xml;

import com.yang.config.Configuration;
import com.yang.config.RegistryConfig;
import com.yang.loadbalance.LoadBalancer;
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

  private String baseExpression = "/configuration/";

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
      // 解析xml
      configuration.setPort(resolverPort(xPath, document, baseExpression + "port"));
      configuration.setApplicationName(resolverApplicationName(xPath, document, baseExpression + "applicationName"));
      RegistryConfig registryConfig = configuration.getRegistryConfig();
      registryConfig.setUrl(resolverRegistryConfig(xPath, document, baseExpression + "registryConfig", "url"));
      configuration.setSerializerType(resolverSerializerType(xPath, document, baseExpression + "serializerType", "type"));
      configuration.setCompressType(resolverCompressType(xPath, document, baseExpression + "compressType", "type"));
      configuration.setLoadbalancer(resolverLoadbalancer(xPath, document, baseExpression + "loadbalancer", "class"));
      configuration.setIdWorker(resolverIdWorker(xPath, document, baseExpression + "idWorker", "class", "workerId", "datacenterId"));

      System.out.println("configuration = " + configuration);

    } catch (ParserConfigurationException | SAXException | IOException e) {
      log.error("加载xml异常", e);
      throw new RuntimeException(e);
    }


  }

  private IdWorker resolverIdWorker(XPath xPath, Document document, String expression, String namedItem, String attr1, String arrt2) {
    String workId = parseString(xPath, document, expression, attr1);
    String datacenterId = parseString(xPath, document, expression, arrt2);
    Class<?>[] classes = {long.class,long.class};
    return (IdWorker) parseObject(xPath, document, expression, namedItem, classes, Long.parseLong(workId), Long.parseLong(datacenterId));
  }


  private LoadBalancer resolverLoadbalancer(XPath xPath, Document document, String expression, String namedItem) {
    return (LoadBalancer) parseObject(xPath, document, expression, namedItem, null);
  }

  private String resolverCompressType(XPath xPath, Document document, String expression, String namedItem) {
    return parseString(xPath, document, expression, namedItem);
  }

  private String resolverSerializerType(XPath xPath, Document document, String expression, String namedItem) {
    return parseString(xPath, document, expression, namedItem);

  }

  private String resolverRegistryConfig(XPath xPath, Document document, String expression, String namedItem) {
    return parseString(xPath, document, expression, namedItem);
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
    return parseString(xPath, document, expression);
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
    return Integer.parseInt(port);
  }

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
        instance =  aClass.getConstructor(pramsType).newInstance(prams);
      }

      return aClass.cast(instance);
    } catch (XPathExpressionException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
             IllegalAccessException | InvocationTargetException e) {
      log.error("xml节点解析异常", e);
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {

  }


}
