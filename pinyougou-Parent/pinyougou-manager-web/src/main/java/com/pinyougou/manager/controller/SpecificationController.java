package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.sellergoods.service.SpecificationOptionService;
import com.pinyougou.sellergoods.service.SpecificationService;
import domain.SpecificationP;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/specification")
public class SpecificationController {

	@Reference
	private SpecificationService specificationService;
	@Reference
	private SpecificationOptionService specificationOptionService;
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbSpecification> findAll(){			
		return specificationService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return specificationService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody SpecificationP specificationp){
		try {//添加规格的同时  添加选项详情
            specificationService.add(specificationp);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param specification
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody SpecificationP specification){
		try {
			specificationService.update(specification.getSpecification());
            Long id = specification.getSpecification().getId();
            //先删除
             specificationOptionService.deleteBySpecId(id);
            //再添加
            List<TbSpecificationOption> options = specification.getSpecificationOptions();
            for (TbSpecificationOption option : options) {
                option.setSpecId(id);
                specificationOptionService.add(option);
            }

			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbSpecification findOne(Long id){

	    return specificationService.findOne(id);
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			specificationService.delete(ids);

			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbSpecification specification, int page, int rows  ){
		return specificationService.findPage(specification, page, rows);		
	}

    /**
     * 根据specId查询spec和options
     * @param id
     * @return
     */
    @RequestMapping("/findById")
	public SpecificationP findById(Long id){
        TbSpecification specification = specificationService.findOne(id);
        List<TbSpecificationOption> options = specificationOptionService.findBySpecId(id);
        return new SpecificationP(specification,options);
	}
    /**
     * 将所有信息转换为Map集合
     * @return
     */
    @RequestMapping("/findSpecToMap")
	public List<Map> findSpecToMap(){
	    return specificationService.findSpecToMap();
    }

}
