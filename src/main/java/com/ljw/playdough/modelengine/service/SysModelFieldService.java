package com.ljw.playdough.modelengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ljw.playdough.modelengine.entity.SysModelField;

import java.util.List;

public interface SysModelFieldService extends IService<SysModelField> {

    /**
     * 根据模型ID查询字段列表
     */
    List<SysModelField> listByModelId(Long modelId);


}