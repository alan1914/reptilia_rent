package com.alan.selenium.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author shiml
 * @date 2022/2/11 2:17 下午
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
class RentHouseServiceImplTest {

    @Autowired
    private RentHouseServiceImpl rentHouseService;


    @Test
    void reptileRentHouseData() {

        Integer counter = 0;
        run(counter);

    }

    private void run(Integer counter) {
        try {
            counter = counter++;
            rentHouseService.reptileRentHouseData();
        } catch (Exception e) {
            log.error("{}", e);
            log.error("失败计数器:[{}]", counter);
            if (counter < 5) {
                run(counter);
            }
        }
    }

}