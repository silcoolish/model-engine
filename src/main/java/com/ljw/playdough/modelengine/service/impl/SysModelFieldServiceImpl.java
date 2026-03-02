package com.ljw.playdough.modelengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljw.playdough.modelengine.entity.SysModelField;
import com.ljw.playdough.modelengine.mapper.SysModelFieldMapper;
import com.ljw.playdough.modelengine.service.SysModelFieldService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysModelFieldServiceImpl
        extends ServiceImpl<SysModelFieldMapper, SysModelField>
        implements SysModelFieldService {

    @Override
    public List<SysModelField> listByModelId(Long modelId) {
        return this.list(
                new LambdaQueryWrapper<SysModelField>()
                        .eq(SysModelField::getModelId, modelId)
                        .eq(SysModelField::getDeleted, 0)
                        .orderByAsc(SysModelField::getId)
        );
    }
}