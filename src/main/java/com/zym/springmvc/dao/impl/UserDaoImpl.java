package com.zym.springmvc.dao.impl;

import com.zym.springmvc.annotation.Repository;
import com.zym.springmvc.dao.UserDao;

@Repository(value = "userDao")
public class UserDaoImpl implements UserDao {

	@Override
	public void insert() {
		System.out.println("ING!!!!");
	}

}
