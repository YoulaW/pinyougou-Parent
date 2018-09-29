package com.pinyougou.manager.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 需要獲取  登陸著 的姓名  展示在頁面上
 * @author Youla
 *
 */
@RestController
@RequestMapping("/login")
public class LoginController {
		@RequestMapping("/findName")
		public Map<String,String> name(){
				String name = SecurityContextHolder.getContext().getAuthentication().getName();
				Map<String,String>  map = new HashMap();
				 map.put("loginName",name);
				 return map;
		}
}
