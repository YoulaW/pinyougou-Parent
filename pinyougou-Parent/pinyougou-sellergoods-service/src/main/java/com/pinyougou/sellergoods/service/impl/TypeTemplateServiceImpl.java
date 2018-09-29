package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	


	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	/**
	 * 根据ID获取实体  並根據Spec_Id查詢 specOptions
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
        TbTypeTemplate template = typeTemplateMapper.selectByPrimaryKey(id);
//        String specIds = template.getSpecIds();
//        List<Map> specList = JSON.parseArray(specIds, Map.class);
//        for (Map map : specList) {
//
//            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
//            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
//            criteria.andSpecIdEqualTo(new Long((Integer)map.get("id")));
//            List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(example);
//            map.put("options",options);
//        }
//
//        template.setSpecIds(JSON.toJSONString(specIds));

        return template;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();


		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}
			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}
			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}
			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}
	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);

		//调用方法 品牌和spec存到redis中
		saveToRedis();
		return new PageResult(page.getTotal(), page.getResult());
	}

    @Override
    public List<Map> findTempSpec(Long id) {
        TbTypeTemplate template = typeTemplateMapper.selectByPrimaryKey(id);
        String specIds = template.getSpecIds();
        List<Map> maps = JSON.parseArray(specIds, Map.class);
        for (Map map : maps) {

            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(new Long((Integer)map.get("id")));
            List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(example);
            map.put("options",options);
        }
		System.out.println(maps);

        return maps;
    }

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 将type中的 brand 和 spec 存入到缓存中
	 * 大key 为各自名字   小key 为typeId  值为 各自值
	 * 再让findPage 调用方法
	 */
	public void saveToRedis(){
		List<TbTypeTemplate> typeTemplates = findAll();
		//遍历模板  分别将品牌和spec装到缓存中
		for (TbTypeTemplate typeTemplate : typeTemplates) {
			//装 品牌 先转为 JSON
			List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(),Map.class);

			redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(),brandList);
			
			//装 spec   因还有option也也需要存储 调用findTempSpec(Long id)
			List<Map> specList = findTempSpec(typeTemplate.getId());
			redisTemplate.boundHashOps("specList").put(typeTemplate.getId(),specList);
		}
		System.out.println("缓存 品牌和spec");
	}
    

}
