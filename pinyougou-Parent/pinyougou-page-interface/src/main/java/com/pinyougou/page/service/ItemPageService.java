package com.pinyougou.page.service;

/**
 * freeMarker 生成静态化页面
 */
public interface ItemPageService {
    /**
     * 根据商品Id  生成静态页面
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);

}
