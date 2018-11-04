package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import domain.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service(timeout=6000)
public class CartServiceImpl implements CartService{

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 将商品添加到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

        //1查看商家是否存在
        //1.1不存在  就直接添加
        //1.2存在
        //1.2.0查看 商家的该商品是否存在
        //1.2.1 不存在  直接存在cartlist中
        //1.2.2 存在  直接加数量




        //1.根据商品 SKU ID 查询 SKU 商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        //商品 不存在  或 状态不为1时
        if(item==null){
            throw new RuntimeException("商品不存在");
        }
        if(!item.getStatus().equals("1")){
            throw new RuntimeException("商品无效");
        }

        //2.获取商家 ID
        String sellerId = item.getSellerId();

        //3.根据商家 ID 判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(sellerId, cartList);

        if (cart==null){
            //4.如果购物车列表中不存在该商家的购物车
            //4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item, num);
            List<TbOrderItem> orderItemList = new ArrayList<>();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);
        }else{
            //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品  订单
            TbOrderItem orderItem=searchOrderItemByItemId(cart.getOrderItemList(),itemId);
            if (orderItem==null){
                //5.1. 如果没有，新增购物车明细
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);//将新商品加到购物车中的list中

            }else {
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+num);

                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().longValue()));

                //如果数量操作后小于等于 0，则移除
                if(orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);
                }

                //如果移除后 cart 的明细数量为 0，则将 cart 移除
                if (cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }
        }




        return cartList;
    }


    /**
     * 根据itemId查看  orderItemList中是否存在该商品 并返回
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            // 因Long为包装类型 equals比较的是地址值 所以转换为long来比较值
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 根据商家ID查看 list中是否存在该商家  最后返回
     * @return
     */
    private Cart searchCartBySellerId(String sellerId,List<Cart> cartList){
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)){//列表中不含有该商家
                return cart;
            }
        }
        return null;
    }

    //创建TbOrderItem
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        if (num<=0){
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        //因double运算会有精度损失  金额比较敏感 故用BigDecimal类转换
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }


    /**
     * 登陆后 根据用户 名 从redis中获取
     * @param username
     * @return
     */
    @Override
    public List<Cart> findFromRedis(String username) {
        System.out.println("从 redis 中提取购物车数据....."+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);

        if (cartList==null){
            cartList=new ArrayList<>();
        }


        return cartList;
    }
    /**
     * 登陆后根据用户名将 cookie中的list存入redis中
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartToRedis(String redisName,String username, List<Cart> cartList) {
        System.out.println("向 redis 存入购物车数据....."+username);
        redisTemplate.boundHashOps(redisName).put(username,cartList);
    }


    /**
     * 合并两个购物车
     * @param cartList1
     * @param cartList2
     */
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2){
        //遍历list2  从中取得OrderItem
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList1=addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
            }
        }

        return cartList1;
    };

}
