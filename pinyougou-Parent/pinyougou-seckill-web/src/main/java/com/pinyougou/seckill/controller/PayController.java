package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference(timeout = 5000)
    private WeixinPayService weixinPayService;
    @Reference
    private SeckillOrderService seckillOrderService;

    /**
     * 生成二维码
     * @param
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
//        TbPayLog payLog = payLogService.findPayLogByUserId(userId);
//
//        if (payLog!=null){
//            return weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
//        }else {
//            return new HashMap();
//        }
        Map<String, TbSeckillOrder> seckillOrderMap = seckillOrderService.findSeckillOrderFromRedisByUser(userId);
        Map orderMapToPay = new HashMap();
        Map map = new HashMap();
        if (seckillOrderMap!=null&&seckillOrderMap.size()>0){
            //遍历秒订单map  分别获取该订单的 支付信息  封装在orderMapToPay 中返回前台
            for(String key:seckillOrderMap.keySet()){
                 map = weixinPayService.createNative(seckillOrderMap.get(key).getId()+"",
                        (long)(seckillOrderMap.get(key).getMoney().doubleValue()*100) + "");//分转为元

                System.out.println(seckillOrderMap.get(key).getId()+" 价格是   "+
                        ((long)seckillOrderMap.get(key).getMoney().doubleValue()*100));
                orderMapToPay.put(seckillOrderMap.get(key).getId()+"",map);//存放订单Id和该订单信息
            }
            return orderMapToPay;
        }else {
            return new HashMap<>();
        }
    }

    @RequestMapping("/queryOrderPayStatus")
    public Result queryOrderPayStatus(String out_trade_no){
        //获取当前用户
        String userId=SecurityContextHolder.getContext().getAuthentication().getName();

        Result result = null;
        int x=0;//为了记录循环的次数
        //无限循环调用支付状态接口
        while(true){
            Map payStatus = weixinPayService.queryOrderPayStatus(out_trade_no);

            if (payStatus==null){//如果查询到为null
                result = new Result(false,"支付出错了");
                break;
            }
            if (payStatus.get("trade_state").equals("SUCCESS")){//支付成功了
                result = new Result(true,"支付成功");
//                //支付成功 更改数据库中payLog的状态  和 order的状态  并从缓存中删除payLog
//                orderService.updatePaymentStatus(out_trade_no,(String)payStatus.get("transaction_id"));

                seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), payStatus.get("transaction_id")+"");

                break;
            }


            //每隔几秒查询一次
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            System.out.println(x+"=====x====");
            if(x>=100){//如果循环大于100  就报异常
                result = new Result(false,"二维码超时");
                //二维码超时  支付失败  将库存还原   从redis中删除  更新到数据库中
                // 关闭订单 并更新库存
                Map closeOrderPay = weixinPayService.closeOrderPay(out_trade_no);

                if (!"SUCCESS".equals(closeOrderPay.get("return_code"))){//如果 关闭微信异常
                    if ("ORDERPAID".equals(closeOrderPay.get("err_code"))){//如果 错误代码 为 支付成功
                        result = new Result(true,"支付成功");
                        //保存订单
                        seckillOrderService.saveOrderFromRedisToDb(userId,
                                Long.valueOf(out_trade_no), closeOrderPay.get("transaction_id")+"");
                    }
                }

                if(result.isSuccess()==false){
                    System.out.println("超时，取消订单");
                    //2.调用删除
                    seckillOrderService.deleOrderFromRedis(userId,
                            Long.valueOf(out_trade_no));
                }


                break;
            }
        }

        return result;
    }

}
