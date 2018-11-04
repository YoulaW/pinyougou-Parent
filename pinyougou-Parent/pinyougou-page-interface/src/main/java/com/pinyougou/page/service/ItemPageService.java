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

    /**
     * 根据商品ID 将静态页面删除
     * @param goodsIds
     * @return
     */
    public boolean deleteItemHtml(Long[] goodsIds);

}
