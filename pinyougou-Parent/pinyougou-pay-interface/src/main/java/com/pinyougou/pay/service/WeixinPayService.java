package com.pinyougou.pay.service;


import java.util.Map;

public interface WeixinPayService {

    /**
     * 生成二维码 传递到前台
     * @param out_trade_no 订单编号
     * @param total_fee  订单金额（分）
     * @return
     */
    public Map createNative(String out_trade_no,String total_fee);

    /**
     * 根据订单查询  支付状态
     * @param out_trade_no
     * @return
     */
    public Map queryOrderPayStatus(String out_trade_no);

    /**
     * 根据订单删除微信支付
     * @param out_trade_no
     * @return
     */
    public Map closeOrderPay(String out_trade_no);
}
