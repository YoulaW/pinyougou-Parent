package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.sellergoods.service.GoodsService;
import domain.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service(timeout = 50000)
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
    @Autowired
	private TbItemMapper itemMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {

	    return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}
    @Autowired
    private TbBrandMapper brandMapper;
	@Autowired
    private TbItemCatMapper itemCatMapper;
	@Autowired
    private TbSellerMapper sellerMapper;
	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {


            //設置新增商品的狀態
            goods.getGoods().setAuditStatus("0");
            goodsMapper.insert(goods.getGoods());

            //獲得新增商品的ID
            Long goodsId = goods.getGoods().getId();
            goods.getGoodsDesc().setGoodsId(goodsId);
            goodsDescMapper.insert(goods.getGoodsDesc());

            saveItems(goods);//保存 items


//		//item插入
//		for (TbItem item : goods.getItems()) {
//			//title
//			String  title = goods.getGoods().getGoodsName();
//            String spec = item.getSpec();
//            Map<String,Object> maps = JSON.parseObject(spec);
//            for (String key:maps.keySet()) {
//                title+=" "+maps.get(key);
//            }
//            item.setTitle(title);//
//
//            item.setCreateTime(new Date());
//            item.setUpdateTime(new Date());
//            item.setGoodsId(goodsId);
//            item.setSellerId(goods.getGoods().getSellerId());
//            item.setCategoryid(goods.getGoods().getCategory3Id());
//            //品牌名称
//            TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
//            item.setBrand(brand.getName());
//            //分类名称
//            TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
//            item.setCategory(itemCat.getName());
//            //商家名称
//            TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
//            item.setSeller(seller.getNickName());
//
//            //图片地址（取 spu 的第一个图片）
//            List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class) ;
//            if(imageList.size()>0){
//                item.setImage ( (String)imageList.get(0).get("url"));
//            }
//            itemMapper.insert(item);
//        }

    }

    /**
     * 保存 items的方法
     * @param goods
     */

    private void saveItems(Goods goods){
        if ("1".equals(goods.getGoods().getIsEnableSpec())){
            //啓用規格
            for (TbItem item : goods.getItems()) {
                //标题
                String title= goods.getGoods().getGoodsName();
                Map<String,Object> specMap = JSON.parseObject(item.getSpec());

                for(String key:specMap.keySet()){
                    title+=" "+ specMap.get(key);
                }
                item.setTitle(title);

                setItemValus(goods,item);
                itemMapper.insert(item);
            }
        }else{
            TbItem item=new TbItem();
            item.setTitle(goods.getGoods().getGoodsName());//商品 KPU+规格描述串作为SKU 名称
            item.setPrice( goods.getGoods().getPrice() );//价格
            item.setStatus("1");//状态
            item.setIsDefault("1");//是否默认
            item.setNum(99999);//库存数量
            item.setSpec("{}");
            setItemValus(goods,item);
            itemMapper.insert(item);
        }
    }

    /**
     * 保存items 中specValue的方法
     * @param goods
     * @param item
     */
    private void setItemValus(Goods goods, TbItem item) {
        item.setGoodsId(goods.getGoods().getId());//商品 SPU 编号
        item.setSellerId(goods.getGoods().getSellerId());//商家编号
        item.setCategoryid(goods.getGoods().getCategory3Id());//商品分类编号（3 级）
        item.setCreateTime(new Date());//创建日期
        item.setUpdateTime(new Date());//修改日期

         //品牌名称
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());
            //分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());
         //商家名称
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(seller.getNickName());
            //图片地址（取 spu 的第一个图片）
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class) ;
        if(imageList.size()>0){
        item.setImage ( (String)imageList.get(0).get("url"));
        }
    }


    /**
	 * 修改
	 */
	@Override
	public void update(Goods goods){

		goodsMapper.updateByPrimaryKey(goods.getGoods());
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//根據goodsId查詢所有的items   然後再重新set前臺傳的數據
        Long id = goods.getGoods().getId();

        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> tbItems = itemMapper.selectByExample(example);
        for (TbItem item : tbItems) {
            itemMapper.updateByPrimaryKey(item);
        }
        saveItems(goods);

    }
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods =new Goods();
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(tbGoodsDesc);
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> tbItems = itemMapper.selectByExample(example);
        goods.setItems(tbItems);
        return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
		    // 將goods中is_delete改爲1 後臺邏輯是不顯示 為1的  同時將item中的s'tatus改爲0
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            goods.setIsDelete("1");
            goodsMapper.updateByPrimaryKey(goods);

            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(id);
            List<TbItem> items = itemMapper.selectByExample(example);
            for (TbItem item : items) {
                item.setStatus("0");
                itemMapper.updateByPrimaryKey(item);
            }

        }

	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();

		//查  isDelete 不爲1  就是邏輯不刪除   查詢未刪除的數據
            criteria.andIsDeleteIsNull();

		if(goods!=null){			
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
//				criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
             //不是該商家的而商品不能查詢  所以是 必須相同 不是like
             criteria.andSellerIdEqualTo(goods.getSellerId());

			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);
            PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
            return pageResult;
	}
	/**
	 * 修改 商品的狀態  是否審核通過
	 * @param ids
	 */
	public void updateStatus(Long[] ids,String status){
		for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(tbGoods);
            //更新商品审核状态的同时  更新SPU  item中的状态
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdIn(Arrays.asList(ids));
            List<TbItem> items = itemMapper.selectByExample(example);
            if ("1".equals(status)){
                for (TbItem item : items) {
                    item.setStatus("1");
                    itemMapper.updateByPrimaryKey(item);
                }

            }else {
                for (TbItem item : items) {
                    item.setStatus("0");
                    itemMapper.updateByPrimaryKey(item);
                }
            }


        }
	};
    /**
     * 商品是否上架
     */
    public void isMarketable(Long[] ids,String isMarketable){
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsMarketable(isMarketable);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    };
    /**
     * 根据商品ID 和 status查询 SKU  ItemList
     * @param goodsIds
     * @param status
     * @return
     */

    public List<TbItem> findItemByGoodsIdAndStatus(Long[] goodsIds,String status){

        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdIn(Arrays.asList(goodsIds));
        criteria.andStatusEqualTo(status);

        List<TbItem> tbItems = itemMapper.selectByExample(example);
        return tbItems;
    }



}
