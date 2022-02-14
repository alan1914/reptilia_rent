package com.alan.selenium.service.impl;

import com.alan.selenium.dao.RentHouseMapper;
import com.alan.selenium.entity.RentHouseDO;
import com.alan.selenium.service.RentHouseService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * (RentHouse)表服务实现类
 *
 * @author shiml
 * @since 2022-02-11 12:24:57
 */
@Service
@Slf4j
public class RentHouseServiceImpl extends ServiceImpl<RentHouseMapper, RentHouseDO> implements RentHouseService {

    /**
     * 品易账号名
     */
    private final String PY_USERNAME = "17338730223";

    /**
     * 品易密码
     */
    private final String PY_PASSWORD = "Test@123";

    /**
     * 爬取地址
     */
//    private final String REPTILE_URL = "https://sz.58.com/nanshan/chuzu/j2/";
    private String REPTILE_URL = "https://sz.58.com/nanyou/chuzu/j2/?minprice=3000_6000&sourcetype=5";

    private Integer counter = 1;

    @Override
    public void reptileRentHouseData() throws Exception {


        runDriver(counter);

    }

    private void runDriver(int counter) throws Exception {
        // 代理服务 IP:PORT
        String proxyServer = null;
        try {
            proxyServer = this.getProxyServer(1);
            log.warn("本次获取的代理服务地址为:[{}]", proxyServer);
        } catch (Exception e) {
            log.error("获取代理服务器地址失败，终止爬取");
            throw new Exception("获取代理服务器地址失败，终止爬取");
        }

        System.setProperty("webdriver.chrome.driver", "/Users/alan1914/Downloads/chromedriver");

        // 设置代理信息
        Proxy proxy = new org.openqa.selenium.Proxy();
        proxy.setSslProxy(proxyServer);
        proxy.setHttpProxy(proxyServer);
        proxy.setSocksUsername(PY_USERNAME);
        proxy.setSocksPassword(PY_PASSWORD);

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setProxy(proxy);
        chromeOptions.addArguments("user-agent='Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.72 Safari/537.36'");

        WebDriver driver = new ChromeDriver(chromeOptions);

        if (counter != 1) {
            // 构造指定分页URL
            REPTILE_URL = REPTILE_URL.replace("pn" + (counter - 1), "pn" + counter);
        }
        log.warn("第{}次请求,url为:[{}]", counter, REPTILE_URL);

        driver.get(REPTILE_URL);

        run(driver, counter);

        driver.close();

        // 累加计数器，记录当前分页数
        counter = counter + 1;
        runDriver(counter);
    }

    private void run(WebDriver driver, Integer counter) throws Exception {

        if (counter == 1) {
            log.warn("当前为第一次请求，获取分页链接");
            REPTILE_URL = driver.findElement(By.xpath("//*[@id=\"pager_wrap\"]/div/a[1]")).getAttribute("href");
            log.warn("获取翻页链接为:[{}]", REPTILE_URL);
        }


        // 随机睡眠
        Thread.sleep(new Random().nextInt(5000) + 1000);

        List<WebElement> houseList = driver.findElements(By.className("house-cell"));

        if (CollectionUtils.isEmpty(houseList)) {
            log.error("爬取失败，失败原因：获取集合为空");
            throw new Exception("爬取失败，失败原因：获取集合为空");
        }

        List<RentHouseDO> rentHouseList = houseList.stream().map(item -> {

            WebElement strongbox = item.findElement(By.className("strongbox"));
            WebElement room = item.findElement(By.className("room"));
            WebElement infor = item.findElement(By.className("infor"));
            WebElement money = item.findElement(By.className("money"));

            RentHouseDO rentHouseDO = new RentHouseDO();
            rentHouseDO.setTitle(strongbox.getText());
            rentHouseDO.setLink(strongbox.getAttribute("href"));
            String[] roomArr = room.getText().split("\\s+");
            rentHouseDO.setRoom(room.getText());
            rentHouseDO.setRoomNum(roomArr[0].trim());
            rentHouseDO.setRoomArea(Double.valueOf(roomArr[1].split("㎡")[0]).intValue());
            String[] inforArr = infor.getText().split("\\s+");
            rentHouseDO.setInfor(infor.getText());
            if (inforArr.length > 0) {
                rentHouseDO.setRegion(inforArr[0].trim());
            }
            if (inforArr.length > 1) {
                rentHouseDO.setVillage(inforArr[1].trim());
            }
            if (inforArr.length > 2) {
                rentHouseDO.setSubwayInfo(inforArr[2].trim());
                rentHouseDO.setSubwayDistance(this.getNumByString(inforArr[2].trim()));
            }
            rentHouseDO.setMoney(this.getNumByString(money.getText()));

            rentHouseDO.setSource("58");
            rentHouseDO.setCity("深圳");
            rentHouseDO.setArea("nanyou");
            return rentHouseDO;
        }).collect(Collectors.toList());

        // 打印
//        Gson gson = new Gson();
//        rentHouseList.stream().forEach(item -> log.warn("{}", gson.toJson(item)));
        log.warn("本次新增记录：{}", houseList.size());

        if (CollectionUtils.isNotEmpty(rentHouseList)) {
            this.saveBatch(rentHouseList);
        }

        // 最后一页无下一页，会抛出异常，达到终止程序的效果
        driver.findElement(By.className("next")).click();

    }

    /**
     * 获取代理IP
     *
     * @param num 指定数量
     * @return
     */
    private String getProxyServer(int num) throws Exception {


        RequestConfig requestConfig = RequestConfig.custom()
                // 一、连接超时：connectionTimeout-->指的是连接一个url的连接等待时间
                .setConnectTimeout(5000)
                // 二、读取数据超时：SocketTimeout-->指的是连接上一个url，获取response的返回等待时间
                .setSocketTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();

        HttpClient client = new DefaultHttpClient();

        // 品易，直接获取代理IP接口
        // todo 传入 num 待实现
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
    private Integer getNumByString(String text) {
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(text);
        return Integer.valueOf(m.replaceAll("").trim());
    }

}

