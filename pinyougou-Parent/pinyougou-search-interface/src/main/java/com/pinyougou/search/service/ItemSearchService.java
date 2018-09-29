package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /**
     * 根據關鍵字進行搜索
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map searchMap);


    /**
     * 高亮顯示
     * @return
     */


    public Map searchList(Map searchMap);


    /**
     * 根据list 跟新solr索引库
     * @param list
     */
    public void updateSolr(List list);

    /**
     * 根据
     * @param goodsIds
     */
    public void deleteSolr(List goodsIds);

}
