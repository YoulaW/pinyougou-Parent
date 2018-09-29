package com.pinyougou.manager.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.omg.CORBA.Request;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
		@Reference
		private BrandService brandService;
		@RequestMapping("/findAll")
		public List<TbBrand> findAll(){
			return brandService.findAll();
		}


	/**
	 * 分页查询
	 * @param page
	 * @param size
	 * @return
	 */
		@RequestMapping("/findByPage")
		public PageResult findByPage(int page,int size){
            PageResult byPage = brandService.findByPage(page, size);

            return byPage;
		}
		@RequestMapping("/add")
		public Result add(@RequestBody TbBrand tbBrand){
			return brandService.add(tbBrand);
		}
		@RequestMapping("/findById")
		public TbBrand findById(Long id){
			return brandService.findById(id);
		}
		@RequestMapping("/update")
		public Result update(@RequestBody TbBrand tbBrand){
			return brandService.update(tbBrand);
		}
		@RequestMapping("delete")
		public Result deleteSelect(long[] ids){
			return brandService.delete(ids);
		}
		@RequestMapping("/search")
		public PageResult searchByExample(@RequestBody TbBrand tbBrand,int page,int size){
			return brandService.searchByExample(tbBrand,page,size);
		}
		@RequestMapping("/findBrandToMap")
		public List<Map> findBrandToMap(){
			return brandService.findBrandToMap();
		}

		
}
