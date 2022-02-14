package com.alan.selenium.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * (RentHouse)表实体类
 *
 * @author shiml
 * @since 2022-02-11 12:24:50
 */
@ApiModel("")
@TableName("rent_house")
@ToString(callSuper = true)
@Data
public class RentHouseDO {

    private static final long serialVersionUID = 845504501261102265L;


    @TableId(type = IdType.AUTO)
    @ApiModelProperty("${column.comment}")
    private Long id;

    /**
     * 标题
     */
    @ApiModelProperty("标题")
    private String title;
    /**
     * 链接
     */
    @ApiModelProperty("链接")
    private String link;

    @ApiModelProperty("${column.comment}")
    private String room;
    /**
     * 几室
     */
    @ApiModelProperty("几室")
    private String roomNum;
    /**
     * 面积
     */
    @ApiModelProperty("面积")
    private Integer roomArea;

    @ApiModelProperty("${column.comment}")
    private String infor;
    /**
     * 地区
     */
    @ApiModelProperty("地区")
    private String region;
    /**
     * 小区
     */
    @ApiModelProperty("小区")
    private String village;
    /**
     * 地铁信息
     */
    @ApiModelProperty("地铁信息")
    private String subwayInfo;
    /**
     * 地铁距离
     */
    @ApiModelProperty("地铁距离")
    private Integer subwayDistance;
    /**
     * 价格
     */
    @ApiModelProperty("价格")
    private Integer money;
    /**
     * 城市
     */
    @ApiModelProperty("城市")
    private String city;
    /**
     * 区
     */
    @ApiModelProperty("区")
    private String area;
    /**
     * 来源 58,beike
     */
    @ApiModelProperty("来源 58,beike")
    private String source;
}
