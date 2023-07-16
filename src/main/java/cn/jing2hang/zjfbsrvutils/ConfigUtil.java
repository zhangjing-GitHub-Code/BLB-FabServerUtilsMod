package cn.jing2hang.zjfbsrvutils;

import com.alibaba.fastjson2.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigUtil {
    /*static HashMap<String,String> J2CMAP(JSONObject jo){
        jo.
    }*/
    static HashMap<String,String> LoadConf(){
        char cbuf[] = new char[10000];
        File conf=new File("config//qcmd.json");
        if(!conf.exists()){
            return new HashMap<String,String> ();//JSONObject();
        }
        InputStreamReader confin;
        try {
             confin = new InputStreamReader(new FileInputStream(conf), "UTF-8");
        }catch(FileNotFoundException e){
            return new HashMap<String,String> ();//JSONObject();
            //return new JSONObject();
        }catch(UnsupportedEncodingException e) {
            conf.delete();
            return new HashMap<String,String> ();//JSONObject();
            //return new JSONObject();
        }
        int len = 0;
        try {
            len = confin.read(cbuf);
            confin.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String text = new String(cbuf, 0, len);
            //1.构造一个json对象
            //TypeReference<HashMap<String,String>> a=new TypeReference<HashMap<String,String>>();
            HashMap<String,String> nmap = JSONObject.parseObject(text.substring(text.indexOf("{")),new TypeReference<HashMap<String,String>>(){});   //过滤读出的utf-8前三个标签字节,从{开始读取
            //2.通过getXXX(String key)方法获取对应的值
            //System.out.println("FLAG:" + obj.getString("FLAG"));
            //System.out.println("NAME:" + obj.getString("NAME"));

            //获取数组
            /*JSONArray arr = obj.getJSONArray("ARRAYS");
            System.out.println("数组长度:" + arr.size());
            for (int i = 0; i < arr.size(); i++) {
                JSONObject subObj = arr.getJSONObject(i);
                System.out.println("数组Name:" + subObj.getString("Name") + " String:" + subObj.getString("String"));
            }*/
        return nmap;

    }
    static boolean /*wtrieConf()*/writeConf(HashMap<String,String> im){
        //char cbuf[] = new char[10000];
        File conf=new File("config//qcmd.json");
        if(!conf.exists()){
            try {
                conf.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        OutputStreamWriter confin=null;
        try {
            confin = new OutputStreamWriter(new FileOutputStream(conf), "UTF-8");
        }catch(FileNotFoundException e){
            // Do YOU THINK NEWLY CREATED FILE CAN DISAPPEAR?
            //return new JSONObject();
        }catch(UnsupportedEncodingException e) {
            // THEN I CAN F*** U
            //return new JSONObject();
        }
        assert confin!=null;
        int len = 0;
        String text = JSON.toJSONString(im, JSONWriter.Feature.WriteMapNullValue);
        //1.构造一个json对象
        //TypeReference<HashMap<String,String>> a=new TypeReference<HashMap<String,String>>();
        //HashMap<String,String> nmap = JSONObject.parseObject(text.substring(text.indexOf("{")),new TypeReference<HashMap<String,String>>(){});   //过滤读出的utf-8前三个标签字节,从{开始读取
        try {
            confin.write(text);
            confin.close();
        } catch (IOException e) {
            return false;
            //throw new RuntimeException(e);
        }
        return true;


        //2.通过getXXX(String key)方法获取对应的值
        //System.out.println("FLAG:" + obj.getString("FLAG"));
        //System.out.println("NAME:" + obj.getString("NAME"));

        //获取数组
            /*JSONArray arr = obj.getJSONArray("ARRAYS");
            System.out.println("数组长度:" + arr.size());
            for (int i = 0; i < arr.size(); i++) {
                JSONObject subObj = arr.getJSONObject(i);
                System.out.println("数组Name:" + subObj.getString("Name") + " String:" + subObj.getString("String"));
            }*/
    }
}
