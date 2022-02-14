package com.alan.selenium.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author shiml
 * @date 2022/1/25 5:55 下午
 */
@Slf4j
public class MyTest {


    /**
     * 获取代理IP
     *
     * @param num 指定数量
     * @return
     */
    private static String getProxyServer(int num) throws Exception {


        RequestConfig requestConfig = RequestConfig.custom()
                // 一、连接超时：connectionTimeout-->指的是连接一个url的连接等待时间
                .setConnectTimeout(5000)
                // 二、读取数据超时：SocketTimeout-->指的是连接上一个url，获取response的返回等待时间
                .setSocketTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();

        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet("http://tiqu.pyhttp.taolop.com/getip?count=1&neek=21588&type=2&yys=0&port=1&sb=&mr=1&sep=1&time=2");
        get.setConfig(requestConfig);
        HttpResponse response = client.execute(get);

        String ip = null;
        String port = null;
        if (response.getStatusLine().getStatusCode() == 200) {
            HttpEntity resEntity = response.getEntity();
            String message = EntityUtils.toString(resEntity, "utf-8");
            log.warn("响应体：{}", message);

            JSONObject jsonObject = JSON.parseObject(message);
            if (null != jsonObject.get("data")) {
                JSONArray jsonArray = JSON.parseArray(jsonObject.get("data").toString());
                JSONObject jsonData = JSON.parseObject(jsonArray.get(0).toString());
                ip = jsonData.get("ip").toString();
                port = jsonData.get("port").toString();
            }

        } else {
            log.error("请求失败");
        }

        if (!StringUtils.isEmpty(ip) && !StringUtils.isEmpty(port)) {
            return String.format("%s:%s", ip, port);
        } else {
            log.error("获取代理IP失败");
            throw new Exception("获取代理IP失败");
        }

    }


    /**
     * 获取字符串中的数字
     *
     * @param text
     * @return
     */
    private static Integer getNumByString(String text) {
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(text);
        return Integer.valueOf(m.replaceAll("").trim());
    }


    public static void main(String[] args) throws Exception {

        // 品易账号名
        final String PY_USERNAME = "17338730223";
        // 品已密码
        final String PY_PASSWORD = "Test@123";

        String proxyServer = MyTest.getProxyServer(1);
        log.warn("本次获取的代理服务地址为:[{}]", proxyServer);

        System.setProperty("webdriver.chrome.driver", "/Users/alan1914/Downloads/chromedriver");

        //创建无Chrome无头参数
        ChromeOptions options = new ChromeOptions();
//        String proxyServer = ipsArr[new Random().nextInt(20)];
//        Proxy proxy = new Proxy().setHttpProxy(proxyServer).setSslProxy(proxyServer);
//        options.setProxy(proxy);

        Proxy proxy = new org.openqa.selenium.Proxy();
        proxy.setSslProxy(proxyServer);
        proxy.setHttpProxy(proxyServer);
        proxy.setSocksUsername(PY_USERNAME);
        proxy.setSocksPassword(PY_PASSWORD);

        DesiredCapabilities desiredCapabilities = DesiredCapabilities.chrome();
        desiredCapabilities.setCapability(CapabilityType.PROXY, proxy);

        WebDriver driver = new ChromeDriver(desiredCapabilities);
        driver.get("https://sz.58.com/nanshan/chuzu/j2/");

        // wait 20s
//        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        Thread.sleep(new Random().nextInt(5000) + 1000);

        // 自动登陆
//        try {
//            WebElement btnTj = driver.findElement(By.className("btn_tj"));
//            log.warn("---访问频繁被拦截，自动登陆开始---");
//            driver.findElement(By.className("btn_tj")).click();
//            Thread.sleep(1000);
//            driver.findElement(By.id("username")).sendKeys("18cz3o2bd");
//            Thread.sleep(2000);
//            driver.findElement(By.id("password")).sendKeys("Test@123");
//            Thread.sleep(2000);
//            driver.findElement(By.id("btn_account")).click();
//            Thread.sleep(2000);
//            log.warn("---访问频繁被拦截，自动登陆完成---");
//            driver.get("https://sz.58.com/nanshan/chuzu/j2/");
//        } catch (NoSuchElementException e) {
//            log.warn("---进入页面---");
//        }

        List<WebElement> houseList = driver.findElements(By.className("house-cell"));

        if (CollectionUtils.isEmpty(houseList)) {
            log.error("爬取失败，失败原因：获取集合为空");
            return;
        }

        List<Map> mapList = houseList.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            WebElement strongbox = item.findElement(By.className("strongbox"));
            WebElement room = item.findElement(By.className("room"));
            WebElement infor = item.findElement(By.className("infor"));
            WebElement money = item.findElement(By.className("money"));

            map.put("title", strongbox.getText());
            map.put("link", strongbox.getAttribute("href"));

            String[] roomArr = room.getText().split("\\s+");
            map.put("room", room.getText());
            map.put("room_num", roomArr[0].trim());
            map.put("room_area", MyTest.getNumByString(roomArr[1].trim()));

            String[] inforArr = infor.getText().split("\\s+");
            log.info("{}", infor.getText());
            map.put("infor", infor.getText());
            if (inforArr.length > 0) {
                map.put("region", inforArr[0].trim());
            }
            if (inforArr.length > 1) {
                map.put("village", inforArr[1].trim());
            }
            if (inforArr.length > 2) {
                map.put("subway_info", inforArr[2].trim());
                map.put("subway_distance", MyTest.getNumByString(inforArr[2].trim()));
            }

            map.put("money", MyTest.getNumByString(money.getText()));
            return map;
        }).collect(Collectors.toList());

        Gson gson = new Gson();
        mapList.stream().forEach(item -> log.info("{}", gson.toJson(item)));

        driver.close();

    }

}