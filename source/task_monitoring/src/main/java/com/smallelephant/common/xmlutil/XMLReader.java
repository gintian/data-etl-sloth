package com.smallelephant.common.xmlutil;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/***
 * XMLReader工具类
 */
public class XMLReader {


    /**
     * editor: lzy
     * last modify: 2019-01-09
     *
     * @param tag <mysqljdbc & hivejdbc>
     * @return a list of jdbcConnectionArray for JDBCConnectionFactory to Create Connections
     */
    public static ArrayList getXMInfo(String className, String filePath, String tag) {
        SimpleDateFormat logSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<Object> xmlInfoList = new ArrayList<Object>();
        //XMLBuilder
        SAXBuilder xmlBuilder = new SAXBuilder();
        Document doc = null;
        File file = null;
        Object instance = null;
        try {
            //读取xml文件
            file = new File(filePath);
            //文件不存在
            if (!file.exists()) {
                System.out.println(logSdf.format(new Date()) + " DataManageMonitor " + "ERROR " + XMLReader.class + " XML文件不存在");
            }
            doc = xmlBuilder.build(file);
            //拿到根元素
            Element rootElement = doc.getRootElement();
            //拿到我们需要的数据库连接类型类型
            List elementList = rootElement.getChildren(tag);
            //遍历拿到类型匹配的子节点
            for (int i = 0; i < elementList.size(); i++) {
                //类型转换
                Element element = (Element) elementList.get(i);
                //通过反射创建对象实例
                Class classname = Class.forName(className);
                Constructor constructor = classname.getConstructor();
                instance = constructor.newInstance();
                //获取所有的成员变量
                Field[] fields = classname.getDeclaredFields();
                //加强版list复杂数据类型的封装
                HashMap<Object, ArrayList> firldMap = new HashMap<Object, ArrayList>();
                instance = encapsulationByReflex(instance, fields, firldMap, element);
                xmlInfoList.add(instance);
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return xmlInfoList;
        }
    }

    /***
     * editor: lzy
     * last modify: 2019-01-17
     * @param tag <mysqljdbc & hivejdbc & azkaban>
     * @param index find the element witch index equals user give
     * @return a list of jdbcConnectionArray for JDBCConnectionFactory to Create Connections
     */
    public static Object getXMInfo(String className, String filePath, String tag, int index) {
        //XMLBuilder
        SAXBuilder xmlBuilder = new SAXBuilder();
        Document doc = null;
        File file = null;
        Object instance = null;
        try {
            //读取xml文件
            file = new File(filePath);
            //文件不存在
            if (!file.exists()) {
                System.out.println(new Date() + " DataManageMonitor " + "ERROR " + XMLReader.class + " XML文件不存在");
            }
            doc = xmlBuilder.build(file);
            //拿到根元素
            Element rootElement = doc.getRootElement();
            //拿到我们需要的数据库连接类型类型
            List<Element> elementList = rootElement.getChildren(tag);
            //遍历拿到类型匹配的子节点
            for (int i = 0; i < elementList.size(); i++) {
                //添加index判断条件
                if (index == Integer.parseInt(elementList.get(i).getChild("comment").getAttributeValue("index"))) {
                    //通过反射创建对象实例
                    Class classname = Class.forName(className);
                    Constructor constructor = classname.getConstructor();
                    instance = constructor.newInstance();
                    //获取所有的成员变量
                    Field[] fields = classname.getDeclaredFields();
                    //加强版list复杂数据类型的封装
                    HashMap<Object, ArrayList> firldMap = new HashMap<Object, ArrayList>();
                    //循环获取成员变量的名称 split[length-1]
                    instance = encapsulationByReflex(instance, fields, firldMap, elementList.get(i));
                }
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return instance;
        }
    }


    public static Map<String, String> getXMLInfo(String filePath, String tag, String[] childrenTagList) {
        //XMLBuilder
        SAXBuilder xmlBuilder = new SAXBuilder();
        Document doc = null;
        File file = null;
        Object instance = null;
        HashMap<String, String> xmlInfoMap = new HashMap<String, String>();
        //读取xml文件
        file = new File(filePath);
        //文件不存在
        if (!file.exists()) {
            System.out.println(new Date() + " DataManageMonitor " + "ERROR " + XMLReader.class + " XML文件不存在");
        }
        try {
            doc = xmlBuilder.build(file);
            Element element = doc.getRootElement().getChildren(tag).get(0);
            for (String childrenTag : childrenTagList) {
                xmlInfoMap.put(childrenTag, element.getChildren(childrenTag).get(0).getValue());
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xmlInfoMap;
    }



    /***
     * editor: lzy
     * last modify: 2019-01-17
     *  通过反射->根据标签名 封装'标签的值'到对应的'对象成员变量'中
     * @param instance reflex instance
     * @param fields fields of instance
     * @param fieldMap
     * @param element
     * @return
     * @throws IllegalAccessException
     */
    private static Object encapsulationByReflex(Object instance, Field[] fields, HashMap<Object, ArrayList> fieldMap, Element element) throws IllegalAccessException {

        //循环获取成员变量的名称 split[length-1]
        for (int j = 0; j < fields.length; j++) {
            //私有成员变量开放操作权限
            fields[j].setAccessible(true);
            //拿到具体的对象名
            String[] split = fields[j].toString().split("\\.");
            //拿到对象名对应的xml信息,封装到map<key,Array>里
            List<Element> children = element.getChildren(split[split.length - 1]);
            if (children.size() >= 1) {
                for (Element child : children) {
                    ArrayList<String> container = null;
                    //System.out.println(child.getValue());
                    if (!fieldMap.containsKey(split[split.length - 1])) {
                        //key不存在的情况
                        container = new ArrayList<String>();
                        container.add(child.getValue());
                        fieldMap.put(split[split.length - 1], container);
                    } else {
                        //key存在的情况
                        container = fieldMap.get(split[split.length - 1]);
                        container.add(child.getValue());
                        fieldMap.put(split[split.length - 1], container);
                    }
                }
                //将map中的数据推到field里面
//                fields[j].set(instance, fieldMap.get(split[split.length - 1]).size() == 1 ? fieldMap.get(split[split.length - 1]).get(0) : fieldMap.get(split[split.length - 1]));
                fields[j].set(instance, !fields[j].toString().contains("List") ? fieldMap.get(split[split.length - 1]).get(0) : fieldMap.get(split[split.length - 1]));
                fields[j].setAccessible(false);
            } else {
                continue;
            }
        }
        return instance;
    }
}
