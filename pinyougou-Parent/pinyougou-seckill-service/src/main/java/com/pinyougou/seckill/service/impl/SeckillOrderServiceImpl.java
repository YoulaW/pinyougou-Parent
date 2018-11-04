package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pinyougou.common.IdWorker;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	/**
	 * 根据用户id 和商品id 生成秒杀订单
	 * @param userId
	 * @param id
	 */
	public Result submitOrder(String userId, Long id){
		//从缓存中查数据
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);
		//从缓存中查询该用户的订单信息  并将新的订单存放在缓存中
		//生成订单  一个用户可以秒杀多个商品 下多个订单 所以value 存储为map类型  seckillId为key order为value存储
		Map<String,TbSeckillOrder> orderMap = (Map) redisTemplate.boundHashOps("seckillOrder").get(userId);


		if (seckillGoods==null){
			return new Result(false,"商品不存在");
		}
		if(seckillGoods.getStockCount()<=0){
			return new Result(false,"商品已抢购一空");
		}
		//更新redis中的库存
		seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
		redisTemplate.boundHashOps("seckillGoods").put(id,seckillGoods);
		if(seckillGoods.getStockCount()==0){//如果已经被秒光
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);//同步到数据库
			redisTemplate.boundHashOps("seckillGoods").delete(id);//删除缓存数据
		}


		//生成订单
		long orderId = idWorker.nextId();
		TbSeckillOrder seckillOrder=new TbSeckillOrder();
		seckillOrder.setId(orderId);
		seckillOrder.setCreateTime(new Date());
		seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
		seckillOrder.setSeckillId(id);
		seckillOrder.setSellerId(seckillGoods.getSellerId());
		seckillOrder.setUserId(userId);//设置用户 ID
		seckillOrder.setStatus("0");//状态
		System.out.println(seckillGoods.getCostPrice()+"seckillgoods==========="+seckillGoods.getGoodsId());
		System.out.println(seckillOrder.getMoney()+"seckillotder============");
		if(orderMap==null){
			orderMap=new HashMap<>();
		}
		orderMap.put(id+"",seckillOrder);//将订单信息存放在map中  商品ID+秒杀订单

		redisTemplate.boundHashOps("seckillOrder").put(userId,orderMap);
		return new Result(true,"秒杀成功，请尽快支付");
	}
	/**
	 * 根据用户从缓存中 redis 取得该用户的秒杀订单
	 * @param userId
	 * @return
	 */
	@Override
	public Map<String, TbSeckillOrder> findSeckillOrderFromRedisByUser(String userId) {
		Map seckillOrderMap = (Map) redisTemplate.boundHashOps("seckillOrder").get(userId);
		return seckillOrderMap;
	}

	/**
	 * 支付成功保存订单
	 * @param userId
	 * @param orderId
	 */
	@Override
	public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
		System.out.println("saveOrderFromRedisToDb:"+userId);
		//根据用户 ID 查询日志
		Map<String,TbSeckillOrder> seckillOrderMap = (Map<String, TbSeckillOrder>) redisTemplate.boundHashOps("seckillOrder").get(userId);


			TbSeckillOrder seckillOrder = seckillOrderMap.get(orderId);
			if(seckillOrder==null){
				throw new RuntimeException("订单"+seckillOrder.getId()+"不存在");
			}
			//如果与传递过来的订单号不符
			if(seckillOrder.getId().longValue()!=orderId.longValue()){
				throw new RuntimeException("订单"+seckillOrder.getId()+"不相符");
			}
			seckillOrder.setTransactionId(transactionId);//交易流水号
			seckillOrder.setPayTime(new Date());//支付时间
			seckillOrder.setStatus("1");//状态
			seckillOrderMapper.insert(seckillOrder);//保存到数据库
			seckillOrderMap.remove(orderId);//付钱的订单 从 map中移除


		redisTemplate.boundHashOps("seckillOrder").put(userId,seckillOrderMap);//从 redis 中清除  最后更新redis中的订单明细
	}
	/**
	 * 订单超时未支付  将缓存中的数据删除 并将库存添加到goodsList中
	 * @param userId
	 * @param orderId
	 */
	@Override
	public void deleOrderFromRedis(String userId, Long orderId) {
		//查询秒杀订单
		Map<String,TbSeckillOrder> seckillOrderMap =
				(Map<String, TbSeckillOrder>) redisTemplate.boundHashOps("seckillOrder").get(userId);

		TbSeckillOrder seckillOrder = seckillOrderMap.get(orderId);
		if (seckillOrder!=null&& seckillOrder.getId().longValue()==orderId.longValue()){//查到 对应的订单信息 删除  并跟新到数据库中
			seckillOrderMap.remove(orderId);
			redisTemplate.boundHashOps("seckillOrder").put(userId,seckillOrderMap);
		}
		//查询缓存中 商品明细
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(orderId);
		if (seckillGoods!=null){
			seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
			redisTemplate.boundHashOps("seckillGoods").put(orderId,seckillGoods);
		}
	}

}
