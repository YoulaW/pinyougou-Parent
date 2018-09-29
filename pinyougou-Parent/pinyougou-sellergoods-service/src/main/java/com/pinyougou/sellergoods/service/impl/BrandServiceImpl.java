package com.pinyougou.sellergoods.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {

	@Autowired
	private TbBrandMapper tbBrandMpper;
	@Override
	public List<TbBrand> findAll() {
	
		return  tbBrandMpper.selectByExample(null);
	}

	@Override
	public PageResult findByPage(int page, int size) {
		PageHelper.startPage(page,size);

		Page<TbBrand> tbBrands = (Page<TbBrand>) tbBrandMpper.selectByExample(null);

        return new PageResult(tbBrands.getTotal(),tbBrands.getResult());
	}

	@Override
	public Result add(TbBrand tbBrand) {
	    Result result = new Result();

	    if (tbBrand.getName().length()==0||tbBrand.getName()==null){
            result.setSuccess(false);
            result.setMessage("name不能为空");
            return result;
        }
        if (tbBrand.getFirstChar().length()==0||tbBrand.getFirstChar()==null){
            result.setSuccess(false);
            result.setMessage("FirstChar不能为空");
            return result;
        }

        try {
            //待实现的  不能重名
            int i = tbBrandMpper.insert(tbBrand);
            if (i>0){
                result.setSuccess(true);
                result.setMessage("添加成功");
            }else {
                result.setSuccess(false);
                result.setMessage("添加失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage("添加失败");
        }
        return result;
    }

    @Override
    public TbBrand findById(long id) {

        TbBrand tbBrand = tbBrandMpper.selectByPrimaryKey(id);

        return tbBrand ;
    }

    @Override
	public Result update(TbBrand tbBrand) {
        Result result = new Result();
        try {

            int i = tbBrandMpper.updateByPrimaryKey(tbBrand);
            if (i>0){
                result.setSuccess(true);
                result.setMessage("修改成功");
            }else {
                result.setSuccess(false);
                result.setMessage("修改失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage("修改失败");
        }
        return result;
	}

	@Override
	public Result delete(long[] ids) {
        Result result = new Result();
        try {
            int i =0;
            for (long id : ids) {
                i = tbBrandMpper.deleteByPrimaryKey(id);
            }
            if (i>0){
                result.setSuccess(true);
                result.setMessage("修改成功");
            }else {
                result.setSuccess(false);
                result.setMessage("修改失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage("修改失败");
        }
        return result;
	}

    @Override
    public PageResult searchByExample(TbBrand tbBrand, int page, int size) {

	    TbBrandExample tbBrandExample = new TbBrandExample();
	    Criteria criteria = tbBrandExample.createCriteria();
	    if (tbBrand!=null){
            if (tbBrand.getName()!=null&&tbBrand.getName().length()>0){
                criteria.andNameLike("%"+tbBrand.getName()+"%");
            }
            if (tbBrand.getFirstChar()!=null&&tbBrand.getFirstChar().length()>0){
                criteria.andFirstCharLike("%"+tbBrand.getFirstChar()+"%");
            }
        }
        PageHelper.startPage(page, size);
        Page<TbBrand> tbBrands = (Page<TbBrand>) tbBrandMpper.selectByExample(tbBrandExample);

        return new PageResult(tbBrands.getTotal(),tbBrands.getResult());
    }

    @Override
    public List<Map> findBrandToMap() {
        return tbBrandMpper.findBrandToMap();
    }


}
