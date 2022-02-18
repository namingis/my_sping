package my_ioc.app_context;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import my_ioc.annotations.*;
import my_ioc.get_classes_util.PackageClassUtil;

public class ApplicationContext {
	private Map<Class<?>, Object> map = new HashMap<>();
	
	public ApplicationContext(Class<?>... classes) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		for(Class<?> cls: classes) {
			loadBeans(cls);
			scanComponents(cls);
			injectDependencies(cls); //依赖注入与scanComponents分开，因为有可能有未被扫描到的component需要被注入
		}
	}
	
	private void injectDependencies(Class<?> cls) throws IllegalArgumentException, IllegalAccessException {
		for(Map.Entry<Class<?>, Object> entry: map.entrySet()) {
			Class<?> c = entry.getKey();
			Object o = entry.getValue();
			Field[] fields = c.getDeclaredFields();
			for(Field f: fields) {
				Autowired aw = f.getAnnotation(Autowired.class);
				if(aw != null) {
					f.setAccessible(true);
					f.set(o, map.get(f.getType()));
				}
			}
			
		}
	}

	private void loadBeans(Class<?> cls) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Configuration conf = cls.getAnnotation(Configuration.class);
		if(conf != null) { //如果有被Configuration注解
			Object confObj = cls.getDeclaredConstructor().newInstance();
			Method[] methods = cls.getDeclaredMethods();
			for(Method m: methods) { 
				Bean bean = m.getAnnotation(Bean.class);
				if(bean != null) { //如果该方法有被@Bean标示
					Object ret = m.invoke(confObj); //执行对应的方法得到返回值
					map.put(m.getReturnType(), ret);
				}
			}
		}
		
	}

	private void scanComponents(Class<?> cls) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		ComponentScan cs = cls.getAnnotation(ComponentScan.class);
		if(cs != null) { //此处判断类有无被ComponentScan注解，如果有，则扫描
			String[] packages = new String[] {cls.getPackage().getName()};
			for(String pkg: packages) {
				Set<Class<?>> classes = PackageClassUtil.getClasses(pkg);
				for(Class<?> c: classes) {
					Component com = c.getAnnotation(Component.class);
					if(com != null) {
						Object o;
						o = c.getDeclaredConstructor().newInstance();
						map.put(c, o);
					}
				}
			}
		}	
	}
}