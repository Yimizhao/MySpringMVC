package com.zym.springmvc.controller;

import com.zym.springmvc.annotation.Autowired;
import com.zym.springmvc.annotation.Controller;
import com.zym.springmvc.annotation.RequestMapping;
import com.zym.springmvc.service.UserService;

@RequestMapping(value = "/user")
@Controller(value = "userController")
public class UserController {
	
	@Autowired(value = "userService")
	UserService userService;
	
	@RequestMapping("/insert")
	public void insert() {
		userService.insert();
	}

}
