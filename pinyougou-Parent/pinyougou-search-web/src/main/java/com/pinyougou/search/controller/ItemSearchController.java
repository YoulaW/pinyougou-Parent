package com.pinyougou.search.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/itemSearch")
public class ItemSearchController {
    @Reference(timeout = 5000)
    private ItemSearchService itemSearchService;

    /**
     * 根據複製域 查詢
     */
    @RequestMapping("/search")
    public Map<String,Object> search(@RequestBody Map searchMap){
//        return itemSearchService.search(searchMap);/僅是查詢

        return itemSearchService.search(searchMap);//查詢並含高亮顯示
    }



}
