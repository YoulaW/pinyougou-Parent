package com.pinyougou.manager.controller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
//import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbItem;
//import com.pinyougou.search.service.ItemSearchService;
import domain.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
//	@Reference
//	private ItemSearchService itemSearchService;

	@Autowired
	private Destination queueSolrDestination;//队列  用于发送solr 删除solr

	@Autowired
	private JmsTemplate jmsTemplate;//jms模板操作activeMQ

	@Autowired
	private Destination queueSolrDeleteDestination;//用户在索引库中删除记录
	@Autowired
	private Destination topicPageDestination;//页面生成  topic

	@Autowired
	private Destination topicPageDeleteDestination;//用于将页面删除

	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			//goodsService.delete(ids);   最后删除  商品
			//删除的同时  根据goodsId 删除 solr索引库中信息
//			itemSearchService.deleteSolr(Arrays.asList(ids));  改为activeMq  消息中间件
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					//必须实现 Serializable
					HashMap map =new HashMap();
					map.put("ids",ids);
					return session.createObjectMessage(map);
				}
			});

			//删除索引的同时  删除页面
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});



			goodsService.delete(ids);

			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}

	/**
	 * 修改 商品的狀態  是否審核通過
	 * @param ids
	 */

	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids,String status){
		try {
			goodsService.updateStatus(ids,status);
			//更新商品状态的同时更新索引库  如果status为1
			if ("1".equals(status)){
				List<TbItem> tbItems = goodsService.findItemByGoodsIdAndStatus(ids, status);
				if (tbItems.size()>0){
//					itemSearchService.updateSolr(tbItems);改为activeMq  消息中间件  将tbTems集合通过消息中间件的 生产者传输
					// 因传输 有对象  文本 map  此处选择本文  通过JSON转为字符串传递
					final String jsonString = JSON.toJSONString(tbItems);
					//通过JmsTemplate 生产消息
					jmsTemplate.send(queueSolrDestination, new MessageCreator(){
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(jsonString);
                        }
                    });

				}else {
					System.out.println("无明细");
				}

				//同时生 成静态页面
				for (final Long id : ids) {
				//	getHtml(id); 用active MQ替代

					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(id+"");
						}
					});
				}


			}
			return new Result(true, "審核成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "審核失败");
		}
	}


//	@Reference
//	private ItemPageService itemPageService;  用activeMq替换

	/**
	 * 在商品审核状态改变的时候调用 如下方法
	 * 根据商品ID生成商品详情的静态页面  通过freeMarker
	 * @param goodsId
	 */
//	@RequestMapping("/getHtml")
//	public void getHtml(Long goodsId){
//		//itemPageService.genItemHtml(goodsId);
//
////		topicPageDestination
//
//
//
//	}


}
