package com.ketang.system.controller;

import com.ketang.system.model.po.Dictionary;
import com.ketang.system.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class DictionaryController {

    @Autowired
    private DictionaryService dictionaryService;

    @GetMapping("/dictionary/all")
    public List<Dictionary> queryAll(){
        return dictionaryService.queryAll();
    }

    @GetMapping("/dictionary/code/{code}")
    public Dictionary queryByCode(@PathVariable String code){
        return dictionaryService.getByCode(code);
    }
}
