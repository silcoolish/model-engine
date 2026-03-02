package com.ljw.playdough.modelengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ljw.playdough.modelengine.entity.SysModel;


public interface SysModelService extends IService<SysModel> {

    /**
     * 创建模型
     */
    boolean createModel(SysModel model);

    /**
     * 根据 code 查询模型
     */
    SysModel getByCode(String code);


}