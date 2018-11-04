package com.pinyougou.page.service.impl;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfigurer freemarkerConfig;

    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {
        Configuration configuration = freemarkerConfig.getConfiguration();////配置对象
        configuration.setDefaultEncoding("UTF-8");
        try {
            Template template = configuration.getTemplate("item.ftl");

            Map map = new HashMap();
            // 商品
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            map.put("goods",goods);

            // 商品描述
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            map.put("goodsDesc",goodsDesc);

            // item 明细  SKU明细
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);
            criteria.andStatusEqualTo("1");//状态必须是有效的
            example.setOrderByClause("is_default DESC"); //默认顺序 按降序排列

            List<TbItem> itemList = itemMapper.selectByExample(example);
            map.put("itemList",itemList);

            // itemCat明细  查询商品分类
            String itemCat1Name = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2Name = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3Name = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();

            map.put("itemCat1Name",itemCat1Name);
            map.put("itemCat2Name",itemCat2Name);
            map.put("itemCat3Name",itemCat3Name);

            //写出对象
//            Writer writer = new FileWriter(pagedir+goodsId+".html");
            OutputStreamWriter writer =new OutputStreamWriter(new FileOutputStream(pagedir+goodsId+".html"),"UTF-8");

            template.process(map,writer);//模板处理
            writer.close(); //关闭流
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     *
     * 根据商品ID删除 静态页面
     * @param goodsIds
     * @return
     */
    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {

        try {
            for (Long goodsId : goodsIds) {
                new File(pagedir+goodsId+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }
}
