package com.yang.config.spi;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.common.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SpiHandler {


  // 基础路径
  private static final String BASE_PATH = "META-INF/services";

  /**
   * 保存spi相关的原始内容
   * key:接口全限定名
   * v:实现全限定名
   */
  private static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>(8);

  /**
   * 接口对应实现类的缓存
   * k:接口名
   * v:实例
   */
  private static final Map<Class<?>, List<Object>> SPI_IMPL = new ConcurrentHashMap<>(32);


  // 初始化加载spi相关信息
  static {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    URL fileUrl = classLoader.getResource(BASE_PATH);
    if (fileUrl != null) {
      File file = new File(fileUrl.getPath());
      // services下的文件数组
      File[] childFiles = file.listFiles();
      if (childFiles != null) {
        for (File childFile : childFiles) {
          String interfaces = childFile.getName();
          // 拿到文件下的接口实现名字list
          List<String> implName = getImplName(childFile);
          // 添加缓存
          SPI_CONTENT.put(interfaces, implName);
        }
      }
    }
  }


  /**
   * 获得实例
   *
   * @param clazz 类
   * @return 实例对象
   */
  public synchronized static <T> T get(Class<T> clazz) {
    // 先拿缓存
    List<Object> impl = SPI_IMPL.get(clazz);
    if (impl != null && impl.size() > 0) {
      // 返回第一实现
      return clazz.cast(impl.get(0));
    }
    // 构建缓存
    buildImplCache(clazz);

    // 再次获取
    List<Object> list = SPI_IMPL.get(clazz);
    if (list != null && list.size() > 0) {
      // 返回第一实现
      return clazz.cast(list.get(0));
    } else {
      return null;
    }
  }

  /**
   * 获得传入接口的所有实例集合
   *
   * @param clazz 接口实例
   * @return 实例集合
   */
  public synchronized static List<Object> getList(Class<?> clazz) {
    // 先拿缓存
    List<Object> impl = SPI_IMPL.get(clazz);
    if (impl != null && impl.size() > 0) {
      // 返回第一实现
      return impl;
    }
    // 构建缓存
    buildImplCache(clazz);

    // 再次获取
    List<Object> list = SPI_IMPL.get(clazz);
    if (list != null && list.size() > 0) {
      // 返回第一实现
      return list;
    }
    return null;
  }


  /**
   * 构建SPI_IMPL缓存
   *
   * @param clazz
   */
  private static void buildImplCache(Class<?> clazz) {

    // 接口名
    String name = clazz.getName();
    // 拿到实现类全限定名缓存
    List<String> implNames = SPI_CONTENT.get(name);
    if (implNames == null) {
      return;
    }
    List<Object> intantList = new ArrayList<>();

    // 遍历
    for (String implName : implNames) {
      try {
        Class<?> implClass = Class.forName(implName);
        Object instance = implClass.getConstructor().newInstance();
        intantList.add(instance);
      } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
               InvocationTargetException e) {
        log.error("spi创建实例失败", e);
      }
    }
    SPI_IMPL.put(clazz, intantList);
  }


  /**
   * 拿到文件下的接口实现名字
   *
   * @param childFile
   */
  private static List<String> getImplName(File childFile) {
    try (FileReader fileReader = new FileReader(childFile);
         BufferedReader bufferedReader = new BufferedReader(fileReader)
    ) {
      List<String> list = new ArrayList<>();
      while (true) {
        String line = bufferedReader.readLine();
        if (StringUtils.isEmpty(line)) break;
        list.add(line);
      }
      return list;
    } catch (IOException e) {
      log.warn("读取spi文件失败", e);
    }
    return null;
  }


}
