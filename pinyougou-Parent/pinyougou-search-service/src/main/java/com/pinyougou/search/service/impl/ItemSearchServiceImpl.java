package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

     /**
     * 封装 列表结果集  分类列表 品牌列表  spec列表
     * @return
     */


    public Map search(Map searchMap){
        Map map = new HashMap();

        String keywords = (String) searchMap.get("keywords");

        if (keywords!=null&&!"".equals(keywords)) {
            //多关键字查询  将空格去掉
            searchMap.put("keywords", keywords.replace(" ", ""));
        }

        //1. 高亮查询显示
        Map highlightMap = searchList(searchMap);
        map.putAll(highlightMap);

        //2. 商品分类显示
        List list = searchCategoryList(searchMap);
        map.put("categoryList",list);

        //3. 品牌 spec  根据商品的模板ID 查询  从redis中获取  在商品分类不为“” >0的时候再查询

//
        //3.1  判断是否有传分类名  如果有则按照分类名进行查询 品牌和spec  如果没有按照 默认0元素查找
        String categoryName = (String)searchMap.get("category");
        if (categoryName!=null){
            Map brandAndSpecMap = searchBrandAndSpec(categoryName);
            map.putAll(brandAndSpecMap);
        }else {
            if (list.size()>0) {
                Map brandAndSpecMap = searchBrandAndSpec((String) list.get(0));
                map.putAll(brandAndSpecMap);
            }
        }

        return map;
    }




    /**
     * 高亮顯示   根据各个条件  封装查询结果集
     * @return
     */
    public Map searchList(Map searchMap){
        Map map = new HashMap();


        //1. 高亮

        //高亮查詢
        HighlightQuery query=new SimpleHighlightQuery();

        //設置高亮選項    比如  設置高亮的域  高亮的前綴   高亮的後綴  將高亮的選項set到query對象中
        HighlightOptions highlightOptions = new HighlightOptions();

        highlightOptions.addField("item_title");

        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");

        query.setHighlightOptions(highlightOptions);
        Criteria criteria=null;

        if (searchMap.get("keywords")!=null&&!"".equals(searchMap.get("keywords"))) {
            //複製域 查詢  得到參數值
            criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        }else {
            criteria = new Criteria().expression("*:*");//如果无就查所有
        }


        query.addCriteria(criteria);

        //2.根据商品分类  添加查询过滤条件
        if (!"".equals(searchMap.get("category"))){
            //创建查询条件
            Criteria criteriaFilter = new Criteria("item_category").is(searchMap.get("category"));
            //将条件放到  过滤查询中
            FilterQuery filterQuery = new SimpleFilterQuery(criteriaFilter);
            //将过滤查询条件 添加到查询中
            query.addFilterQuery(filterQuery);
        }

        //3. 根据 品牌——spec  添加过滤条件
        if (!"".equals(searchMap.get("brand"))){
            //创建查询条件
            Criteria criteriaFilter = new Criteria("item_brand").is(searchMap.get("brand"));
            //将条件放到  过滤查询中
            FilterQuery filterQuery = new SimpleFilterQuery(criteriaFilter);
            //将过滤查询条件 添加到查询中
            query.addFilterQuery(filterQuery);
        }
        //4. 根据 spec  添加过滤条件
        if (searchMap.get("spec")!=null){
            //先去searchMap中 查询
            Map<String,Object> mapSpec = (Map)searchMap.get("spec");
            //遍历  添加过滤条件
            for (String key : mapSpec.keySet()) {
                //创建查询条件
                Criteria criteriaFilter = new Criteria("item_spec_"+key).is(searchMap.get(key));
                //将条件放到  过滤查询中
                FilterQuery filterQuery = new SimpleFilterQuery(criteriaFilter);
                //将过滤查询条件 添加到查询中
                query.addFilterQuery(filterQuery);
            }
        }
        //5. 根据price 添加过滤条件
        if(!"".equals(searchMap.get("price"))){
            String pirceStr = (String) searchMap.get("price");
            String[] priceArray = pirceStr.split("-");
            if (!"0".equals(priceArray[0])){//不等于0
                Criteria criteriaFilter = new Criteria("item_price").greaterThanEqual(priceArray[0]);
                FilterQuery filterQuery = new SimpleFacetQuery(criteriaFilter);
                query.addFilterQuery(filterQuery);
            }
            if(!"*".equals(priceArray[1])){
                Criteria criteriaFilter = new Criteria("item_price").lessThanEqual(priceArray[1]);
                FilterQuery filterQuery = new SimpleFacetQuery(criteriaFilter);
                query.addFilterQuery(filterQuery);
            }


        }
        //6. 页码查询 &显示  当前页  和 pageSize
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageNo==null){
            pageNo=1;
        }
        if(pageSize==null){
            pageNo=20;
        }
        query.setOffset((pageNo-1)*pageSize);//开始查询的索引
        query.setRows(pageSize);//查询的条数

        //7. 根据排序查询
        String sortV = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");

        if (sortV!=null&&!"".equals(sortV)){
            if (sortField.equals("ASC")){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortV);
                query.addSort(sort);
            }
            if (sortField.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortV);
                query.addSort(sort);
            }
        }





        //***********  获取高亮结果集  ***********
        //高亮查詢
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //原生getContent 在此實現類為空
        //將高亮入口查詢的結果集  為高亮結果集 （條條數據）
        List<HighlightEntry<TbItem>> highlighted = highlightPage.getHighlighted();
        for (HighlightEntry<TbItem> entry : highlighted) {
            //獲取的高亮列表  就是高亮域集合
            List<HighlightEntry.Highlight> highlights = entry.getHighlights();
//            for (HighlightEntry.Highlight h : highlights) {
//                List<String> snipplets = h.getSnipplets();//每個高亮域中的值  可以有多個
//            }
            if (highlights.size()>0&&highlights.get(0).getSnipplets().size()>0){
                //有設置高亮
                TbItem item = entry.getEntity();
                //獲得高亮的值 並set到原本類中 再將類 set到map中
                item.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }

        map.put("rows",highlightPage.getContent());//将查询的 列表返回
        map.put("totalPages",highlightPage.getTotalPages());//总页数
        map.put("totalRows",highlightPage.getTotalElements());//总条数
        return map;
    }

    /**
     * 根据关键字 查询分类
     * @param searchMap
     * @return
     */
    public List searchCategoryList(Map searchMap){
        List list = new ArrayList();


        Query query = new SimpleQuery();
        //分组对象查询的条件  相当于 groupBy
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //关键字查询的条件   相当于 where
        Criteria criteria = null;


        if (searchMap.get("keywords")!=null&&!"".equals(searchMap.get("keywords"))) {
            //複製域 查詢  得到參數值
            criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        }else {
            criteria = new Criteria().expression("*:*");//如果无就查所有
        }

        query.addCriteria(criteria);

        //获得 分组也  可包含多个域的 分组结果
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获得特定域的 分组结果
        GroupResult<TbItem> groupPageGroupResult = groupPage.getGroupResult("item_category");

        // 获得分组的入口
        Page<GroupEntry<TbItem>> entries = groupPageGroupResult.getGroupEntries();

        //遍历入口 得到 每条数据
        for (GroupEntry<TbItem> entry : entries) {
            String groupValue = entry.getGroupValue();//获取分组字段的 每个值
            list.add(groupValue);
        }


        return list;
    }

    /**
     * 根据模板查询 品牌和spec list  显示在面包屑 中
     * @param category
     * @return
     */
    private Map searchBrandAndSpec(String category){
        Map map = new HashMap();

        //先从缓存中获取 模板ID
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);

        //判断  如果模板ID不为null  再去查询 品牌和sepc
        if (typeId!=null){
            List brandList = (List)redisTemplate.boundHashOps("brandList").get(typeId);

            List specList = (List)redisTemplate.boundHashOps("specList").get(typeId);
            map.put("brandList",brandList);
            map.put("specList",specList);
        }

        return map;
    }

    /**
     * 根据list 跟新solr索引库
     * @param list
     */
    public void updateSolr(List list){
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    };

    /**
     * 根据 goodsIds从索引库中查询  根据查询 删除索引库
     * @param goodsIds
     */
    public void deleteSolr(List goodsIds){
        //创建查询
        Query query = new SimpleQuery();
        //创建条件
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        //根据查询条件删除
        solrTemplate.delete(query);
        solrTemplate.commit();
    };


}
