package com.pinyougou.cart.service;

import domain.Cart;

import java.util.List;

public interface CartService {

    /**
     * 将商品添加到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);

    /**
     * 登陆后 根据用户 名 从redis中获取
     * @param username
     * @return
     */
    public List<Cart> findFromRedis(String username);

    /**
     * 登陆后根据用户名将 cookie中的list存入redis中
     * @param username
     * @param cartList
     */
    public void saveCartToRedis(String redisName,String username,List<Cart> cartList);

    /**
     * 合并两个购物车
     * @param cartList1
     * @param cartList2
     */
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);

}
