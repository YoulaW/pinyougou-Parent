package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.HttpClient;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;
    @Value("${notifyurl}")
    private String notifyurl;
    /**
     * 生成二维码 传递到前台
     * @param out_trade_no 订单编号
     * @param total_fee  订单金额（分）
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1.创建参数
        Map map = new HashMap();
        map.put("appid",appid);//公众号
        map.put("mch_id",partner);//商户号
        map.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        map.put("body","品优购");//商品描述
        map.put("out_trade_no",out_trade_no);//订单
        map.put("total_fee",total_fee);//订单金额
        map.put("spbill_create_ip","127.0.0.1");//终端IP
        map.put("notify_url","http://test.itcast.cn");//通知地址  回调地址(随便写)
        map.put("trade_type","NATIVE");//交易类型
        System.out.println(map+"参数为==============");
        try {
            //2.生成要发送的 xml
            String mapToXml = WXPayUtil.generateSignedXml(map,partnerkey);

            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);//开启https
            httpClient.setXmlParam(mapToXml);//将参数set到客服端
            httpClient.post();//提交请求

            //3.获得结果
            String content = httpClient.getContent(); //获得生成的结果String
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(content);//将结果集转为map集合
            System.out.println(xmlToMap+"结果集为=====");
            //创建新map返回  只传输必要属性
            Map mapResult = new HashMap();
            mapResult.put("out_trade_no",out_trade_no);//订单
            mapResult.put("total_fee",total_fee);//订单 金额
            mapResult.put("code_url",xmlToMap.get("code_url"));
            return mapResult;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据订单查询  支付状态
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryOrderPayStatus(String out_trade_no) {
        //查询订单支付结果
        Map paramMap = new HashMap();
        paramMap.put("appid",appid);//公众账号ID
        paramMap.put("mch_id",partner);//商户号
        paramMap.put("out_trade_no",out_trade_no);//商户订单号
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());//随机字符串
        System.out.println(paramMap+"=========订单支付状态查询参数");
        try {
            //
            String signedXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            //查询订单接口 客户端
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();

            //获取访问结果集
            String httpClientContent = httpClient.getContent();
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(httpClientContent);

            System.out.println(xmlToMap+"===========订单支付查询的结果集");

            return xmlToMap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
    /**
     * 根据订单删除微信支付
     * @param out_trade_no
     * @return
     */
    @Override
    public Map closeOrderPay(String out_trade_no) {
        Map deleMap = new HashMap();
        deleMap.put("appid",appid);
        deleMap.put("mch_id",partner);
        deleMap.put("out_trade_no",out_trade_no);
        deleMap.put("nonce_str",WXPayUtil.generateNonceStr());
        try {
            String generateSignedXml = WXPayUtil.generateSignedXml(deleMap, partnerkey);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder ");
            httpClient.setHttps(true);
            httpClient.setXmlParam(generateSignedXml);
            httpClient.post();

            //获得结果
            String httpClientContent = httpClient.getContent();
            Map<String, String> stringMap = WXPayUtil.xmlToMap(httpClientContent);
            return stringMap;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }


    }
}
