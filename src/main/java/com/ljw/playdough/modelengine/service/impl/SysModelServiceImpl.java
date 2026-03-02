package com.ljw.playdough.modelengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljw.playdough.modelengine.ddl.TableValidator;
import com.ljw.playdough.modelengine.entity.SysModel;
import com.ljw.playdough.modelengine.mapper.SysModelMapper;
import com.ljw.playdough.modelengine.service.SysModelService;
import org.springframework.stereotype.Service;

@Service
public class SysModelServiceImpl extends ServiceImpl<SysModelMapper, SysModel> implements SysModelService {

    private final TableValidator tableValidator;

    public SysModelServiceImpl(TableValidator tableValidator) {
        this.tableValidator = tableValidator;
    }

    @Override
    public boolean createModel(SysModel model) {
        return this.save(model);
    }

    @Override
    public SysModel getByCode(String code) {
        return this.getOne(
                new LambdaQueryWrapper<SysModel>()
                        .eq(SysModel::getCode, code)
                        .eq(SysModel::getDeleted, 0)
        );
    }


}