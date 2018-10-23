package com.zym.springmvc.service.impl;

import com.zym.springmvc.annotation.Autowired;
import com.zym.springmvc.annotation.Service;
import com.zym.springmvc.dao.UserDao;
import com.zym.springmvc.service.UserService;

@Service(value = "userService")
public class UserServiceImpl implements UserService {

	@Autowired(value = "userDao")
	UserDao userDao;

	@Override
	public void insert() {
		System.out.println("start");
		userDao.insert();
		System.out.println("ending");
	}

}
