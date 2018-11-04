package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.CookieUtil;
import com.pinyougou.pojo.TbOrderItem;
import domain.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 从cookie//redis 中获取购物车明细
     * @return
     */
    @RequestMapping("/findCartList")
    public Map findCartList(){
        String loginName = loginName();

        String cartList = CookieUtil.getCookieValue(request, "cartList", "UTF-8");

        if (cartList==null || cartList.equals("")){ //如果cartList不存在 则设置[]
            cartList="[]";
        }
        List<Cart> cartList_cookie  = JSON.parseArray(cartList, Cart.class);;
        //将购物车明细和用户名封装在map中返回
        Map map = new HashMap();
        if (loginName.equals("anonymousUser")){//未登录  从cookie中获取
            map.put("cartList",cartList_cookie);


        }else {//如果登陆了从缓存中获取
            List<Cart>  cartList_redis = cartService.findFromRedis(loginName);
            if(cartList_cookie.size()>0){//如果cookie中有值 则合并到redis中  再删除
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);

                //将cookie中的数据删除
                CookieUtil.deleteCookie(request,response,"cartList");
                //将数据重新存到redis中
                cartService.saveCartToRedis("cartList",loginName,cartList_redis);

            }

            map.put("cartList",cartList_redis);
        }

        map.put("loginName",loginName);

        return map;
    }

    /**
     * 将商品添加到购物车   因添加购物车是有9105 page-web 跨域访问  故需要带跨域解决 此处用CORS  用spring 4.2 可用注解 //可直接配置
     * @CrossOrigin 注解配置  4.2才可用   allowCredentials = "true"默认为true 默认开启客服端可携带cookie凭证
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
//    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num){
        //不用注解  配置
        response.setHeader("Access-Control-Allow-Origin","http://localhost:9105");//允许跨域访问该方法的 域  (该方法不用操作cookie)
        response.setHeader("Access-Control-Allow-Credentials","true"); //如果操作cookie必须加上该配置

        String userName = (String) findCartList().get("loginName");

        System.out.println(userName+"==========");

        try {
            List<Cart> cartList = (List<Cart>) findCartList().get("cartList");
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if ("anonymousUser".equals(userName)) {//未登录  购物车存到cookie中
                CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartList),3600*24,"UTF-8");
                System.out.println("向 cookie 存入数据");
            }else {//将购物车明细添加到redis中
                cartService.saveCartToRedis("cartList",userName,cartList);
            }

            return  new Result(true,"添加成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"添加失败");
        }

    }

    /**
     * 根据选中的商品ids 查找购物车明细 并返回前台 用于计算勾选的商品金额和数量
     * @param ids
     * @return
     */
    @RequestMapping("/findSelect")
    public List<Cart> findSelect(Long[] ids){
        //先查所有的购物车明细
        List<Cart> cartList = (List<Cart>) findCartList().get("cartList");
        String loginName = (String) findCartList().get("loginName");
        //遍历明细  将itemIds相同的 筛选出来放到另外一个orderItemList集合中  最终将新集合返回

//        List<TbOrderItem> orderItemList1 = new ArrayList<>();
        for (int i = 0; i < cartList.size(); i++) {

            List<TbOrderItem> orderItemList = cartList.get(i).getOrderItemList();

            for (int j = 0; j < orderItemList.size(); j++) {

                for (int z = 0; z < ids.length; z++) {
                    if (orderItemList.get(j).getItemId().longValue()!=ids[z].longValue()){
//                        orderItemList1.add(orderItemList.get(j));
                        orderItemList.remove(j);
                    }
                }
            }
            //如果orderItemList.size==0 时 删除这条数据
            if (orderItemList.size()==0){
                cartList.remove(i);
            }
        }

        // 将选中的商品存入redis中
        cartService.saveCartToRedis("cartListSelected",loginName,cartList);


        return cartList;
    }
    @RequestMapping("/longinName")
    public String loginName(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登陆人是："+name);
        return name;
    }

}
