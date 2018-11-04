package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.order.service.PayLogService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
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
    @Reference(timeout = 5000)
    private PayLogService payLogService;
    @Reference(timeout = 5000)
    private OrderService orderService;


    /**
     * 生成二维码
     * @param
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = payLogService.findPayLogByUserId(userId);

        if (payLog!=null){
            return weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
//            return weixinPayService.createNative(payLog.getOutTradeNo(),"0.01");
        }else {
            return new HashMap();
        }

    }

    @RequestMapping("/queryOrderPayStatus")
    public Result queryOrderPayStatus(String out_trade_no){
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
                //支付成功 更改数据库中payLog的状态  和 order的状态  并从缓存中删除payLog
                orderService.updatePaymentStatus(out_trade_no,(String)payStatus.get("transaction_id"));
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
                break;
            }
        }

        return result;
    }

}
