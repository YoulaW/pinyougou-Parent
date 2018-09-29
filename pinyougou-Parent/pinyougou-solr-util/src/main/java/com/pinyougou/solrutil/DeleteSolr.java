package com.pinyougou.solrutil;

import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

@Component
public class DeleteSolr {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private TbItemMapper itemMapper;

    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        DeleteSolr deleteSolr = ac.getBean("deleteSolr", DeleteSolr.class);
//        deleteSolr.delete();
        deleteSolr.selectAll();
    }

    public void delete(){
        //删除全部
        Query query =new SimpleQuery("*:*");

        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    public void selectAll(){
        Query query =new SimpleQuery("*:*");
        TbItem tbItem = solrTemplate.queryForObject(query, TbItem.class);
        System.out.println(tbItem);
    }

}
