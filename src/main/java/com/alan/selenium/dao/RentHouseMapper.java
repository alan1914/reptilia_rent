package com.alan.selenium.dao;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.alan.selenium.entity.RentHouseDO;

/**
 * (RentHouse)表数据库访问层
 *
 * @author shiml
 * @since 2022-02-11 12:24:52
 */
@Mapper
public interface RentHouseMapper extends BaseMapper<RentHouseDO> {

}

