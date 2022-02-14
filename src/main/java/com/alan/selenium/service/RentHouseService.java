package com.alan.selenium.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.alan.selenium.entity.RentHouseDO;

/**
 * (RentHouse)表服务接口
 *
 * @author shiml
 * @since 2022-02-11 12:11:42
 */
public interface RentHouseService extends IService<RentHouseDO> {

    /**
     * 爬取租房数据
     */
    void reptileRentHouseData() throws Exception;

}

