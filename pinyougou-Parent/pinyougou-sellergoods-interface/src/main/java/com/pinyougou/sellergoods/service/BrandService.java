package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;
import entity.Result;

import java.util.List;
import java.util.Map;

/**
 * 品牌接口
 * @author Youla
 *
 */
public interface BrandService {

		public List<TbBrand> findAll();

		public PageResult findByPage(int page, int size);

		public Result add(TbBrand tbBrand);

		public TbBrand findById(long id);

		public Result update(TbBrand tbBrand);

		public Result delete(long[] ids);

		public PageResult searchByExample(TbBrand tbBrand,int page,int size);

		public List<Map> findBrandToMap();
}
