package com.ketang.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ketang.system.mapper.DictionaryMapper;
import com.ketang.system.model.po.Dictionary;
import com.ketang.system.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, Dictionary> implements DictionaryService {
    @Override
    public List<Dictionary> queryAll() {
        List<Dictionary> list = this.list();
        return list;
    }

    @Override
    public Dictionary getByCode(String code) {
        LambdaQueryWrapper<Dictionary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Dictionary::getCode, code);

        Dictionary res = this.getOne(lqw);
        return res;
    }
}
