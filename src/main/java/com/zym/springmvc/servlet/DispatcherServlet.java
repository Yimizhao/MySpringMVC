package com.zym.springmvc.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zym.springmvc.annotation.Autowired;
import com.zym.springmvc.annotation.Controller;
import com.zym.springmvc.annotation.Repository;
import com.zym.springmvc.annotation.RequestMapping;
import com.zym.springmvc.annotation.Service;

/**
 * Servlet implementation class DispatcherServlet
 */
@WebServlet(name = "dispatcherServlet", loadOnStartup = 1, urlPatterns = { "/*" }, initParams = {
		@WebInitParam(name = "base-package", value = "com.zym.springmvc") })
public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// 扫描的基包
	private String basePackage;

	// 基包下的所有带包路径权限定类名
	private List<String> packageNames = new ArrayList<>();

	// 注解事例化 注解上的名称：事例对象
	private Map<String, Object> instanceMap = new HashMap<>();

	// 带包路径的权限定名称：注解名称
	private Map<String, String> nameMap = new HashMap<>();

	// url地址和方法的映射关系
	private Map<String, Method> urlMethodMap = new HashMap<>();

	// Method与权限定类名映射关系
	private Map<Method, String> methodPackageMap = new HashMap<>();

	public void init(ServletConfig config) throws ServletException {
		basePackage = config.getInitParameter("base-package");

		try {
			// 扫描基包得到全部的权限定类名
			scanBasePackage(basePackage);
			// 实例化带有Controller,Service,Respository的类
			instance(packageNames);
			// springIOC(依赖注入)
			springIOC();
			// url地址与方法的映射关系的对应
			handlerUrlMethodMap();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// url地址与方法的映射关系的对应
	private void handlerUrlMethodMap() throws ClassNotFoundException {
		if (packageNames.isEmpty()) {
			return;
		}
		for (Iterator<String> iterator = packageNames.iterator(); iterator.hasNext();) {
			String packageName = (String) iterator.next();

			Class<?> clazz = Class.forName(packageName);
			if (clazz.isAnnotationPresent(Controller.class)) {
				StringBuffer urlStringBuffer = new StringBuffer();

				if (clazz.isAnnotationPresent(RequestMapping.class)) {
					urlStringBuffer.append(clazz.getAnnotation(RequestMapping.class).value());
				}

				Method[] methods = clazz.getMethods();

				for (Method method : methods) {
					if (method.isAnnotationPresent(RequestMapping.class)) {
						urlStringBuffer.append(method.getAnnotation(RequestMapping.class).value());

						urlMethodMap.put(urlStringBuffer.toString(), method);

						methodPackageMap.put(method, packageName);
					}
				}
			}
		}
	}

	private void springIOC() throws IllegalArgumentException, IllegalAccessException {
		if (instanceMap.isEmpty()) {
			return;
		}
		for (Entry<String, Object> entry : instanceMap.entrySet()) {
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {

				if (field.isAnnotationPresent(Autowired.class)) {
					String className = field.getAnnotation(Autowired.class).value();
					field.setAccessible(true);
					field.set(entry.getValue(), instanceMap.get(className));
				}
			}
		}

	}

	// 实例化带有Controller,Service,Respository的类
	private void instance(List<String> packageNames)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (packageNames.isEmpty()) {
			return;
		}

		for (Iterator<String> iterator = packageNames.iterator(); iterator.hasNext();) {
			String packageName = (String) iterator.next();

			Class<?> className = Class.forName(packageName);

			if (className.isAnnotationPresent(Controller.class)) {
				Controller controllerAnnotation = className.getAnnotation(Controller.class);
				String value = controllerAnnotation.value();
				instanceMap.put(value, className.newInstance());
				// 带包路径的权限定名称：注解名称
				nameMap.put(packageName, value);
				System.out.println("Controller: " + packageName);
			} else if (className.isAnnotationPresent(Service.class)) {
				Service serviceAnnotation = className.getAnnotation(Service.class);
				String value = serviceAnnotation.value();
				instanceMap.put(value, className.newInstance());
				// 带包路径的权限定名称：注解名称
				nameMap.put(packageName, value);
				
				System.out.println("Service: " + packageName);
			} else if (className.isAnnotationPresent(Repository.class)) {
				Repository repositoryAnnotation = className.getAnnotation(Repository.class);
				String value = repositoryAnnotation.value();
				instanceMap.put(value, className.newInstance());
				// 带包路径的权限定名称：注解名称()
				nameMap.put(packageName, value);
				
				System.out.println("Repository: " + packageName);
			}

		}
	}

	// 扫描基包得到全部的权限定类名
	private void scanBasePackage(String basePackage) {
		URL url = this.getClass().getClassLoader().getResource(basePackage.replaceAll("\\.", "/"));
		
		// springmvc层
		File basePackagefile = new File(url.getPath());
		// springmvc下的所有文件与目录
		File[] listFiles = basePackagefile.listFiles();

		for (File file : listFiles) {
			if (file.isDirectory()) {
				scanBasePackage(basePackage + "." + file.getName());
			} else if (file.isFile()) {
				packageNames.add(basePackage + "." + file.getName().split("\\.")[0]);
			}
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {

			String requestURI = request.getRequestURI();

			String contextPath = request.getContextPath();

			String path = requestURI.replaceAll(contextPath, "");

			Method method = urlMethodMap.get(path);

			String string = methodPackageMap.get(method);

			// 获取类的别名（Controller的value所设定的值）
			String controllerName = nameMap.get(string);

			// 获取对应的控制器
			Object object = instanceMap.get(controllerName);

			method.invoke(object);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}

}
