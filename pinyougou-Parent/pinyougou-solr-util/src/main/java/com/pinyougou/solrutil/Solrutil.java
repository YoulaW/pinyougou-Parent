package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Solrutil {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private TbItemMapper itemMapper;
    /**
     * 导入商品数据
     */

    public void creatSolrCollection(){
        //查詢item表
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//已審核的才能查出  放到collention中
        List<TbItem> items = itemMapper.selectByExample(example);
        //商品規格解析為 JSON對象 存到 items中
        for (TbItem item : items) {
            String spec = item.getSpec();

            Map specMap = JSON.parseObject(spec);//将 spec 字段中的 json 字符串转换为 map


            item.setSpecMap(specMap);//给带注解的字段赋值
        }
        //再將items放入到solrTemplate bean 中
        solrTemplate.saveBeans(items);
        //再提交
        solrTemplate.commit();

    }



    //寫一個main方法  啓動creatSolrCollection方法 將商品數據存到solr中
    public static void main(String[] args){
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        Solrutil solrutil = ac.getBean("solrutil", Solrutil.class);
        solrutil.creatSolrCollection();

    }

}
