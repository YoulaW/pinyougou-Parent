package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;

import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
//		redisTemplate.delete("content");  刪除所有的  content相關

		contentMapper.insert(content);
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());//根據id刪除
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//CategoryId 可能會變  所以先根據id查找到數據 再刪除
		Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();

		redisTemplate.boundHashOps("content").delete(categoryId);
		//
		contentMapper.updateByPrimaryKey(content);
		//如果修改前和修改后 categoryId 不一樣   則將  修改后的categoryId也要刪除緩存
		if(categoryId.longValue()!=content.getCategoryId().longValue()){
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {

		for(Long id:ids){
			//根據ID  查找categoryId  再遍歷刪除
			Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
			redisTemplate.boundHashOps("content").delete(categoryId);
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {

		List<TbContent> contents = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);

		if (contents==null){
			System.out.println("从数据库读取数据放入缓存");
			TbContentExample example = new TbContentExample();
			Criteria criteria = example.createCriteria();

			criteria.andCategoryIdEqualTo(categoryId);//根據categoryId 查詢所有
			criteria.andStatusEqualTo("1");//開啓狀態  的 才能查詢到
			example.setOrderByClause("sort_order");//排序

			contents = contentMapper.selectByExample(example);
			redisTemplate.boundHashOps("content").put(categoryId,contents);
		}else {
			System.out.println("從緩存中查找");
		}

		return contents;
	}



}
