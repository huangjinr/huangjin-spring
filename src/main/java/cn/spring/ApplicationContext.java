package cn.spring;


import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.rmi.NoSuchObjectException;
import java.util.HashMap;
import java.util.Map;

public class ApplicationContext {

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<String, BeanDefinition>();
    private Map<String, Object> singletonObjects = new HashMap<String, Object>();


    public ApplicationContext(Class configClass) {

        //扫描包
        scan(configClass);

        //创建bean
        doCreateBean();
    }

    private void doCreateBean() {
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            BeanDefinition beanDefinition = beanDefinitionEntry.getValue();
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getType();
        Object instance = null;
        try {
            instance = clazz.getConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        for (Field field : clazz.getFields()) {

            if (field.isAnnotationPresent(Autowired.class)){
                field.setAccessible(true);
                try {
                    field.set(instance,getBean(field.getName()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }

        return instance;
    }

    public Object getBean(String beanName) {

        if (!beanDefinitionMap.containsKey(beanName)){
            throw new RuntimeException("beanDefinitionMap没找到"+beanName);
        }

        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

        if (beanDefinition.getScope().equals("singleton")){
            Object singletonBean = singletonObjects.get(beanName);
            if (singletonBean == null){
                singletonBean = createBean(beanName, beanDefinition);
            }
            return singletonBean;
        } else if (beanDefinition.getScope().equals("prototype")){
            return createBean(beanName, beanDefinition);
        } else {
            return null;
        }

    }

    private void scan(Class configClass) {
        if (configClass.isAnnotationPresent(ComponentScan.class)) {

            ComponentScan ComponentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);

            String path = ComponentScanAnnotation.value();

            path = path.replace(".", "/");

            System.out.println(path);

            ClassLoader classLoader = ApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);

            File file = new File(resource.getFile());

            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    String absolutePath = f.getAbsolutePath();

                    absolutePath = absolutePath.substring(absolutePath.indexOf("cn"), absolutePath.indexOf(".class"));
                    absolutePath = absolutePath.replace("\\", ".");

                    Class<?> clazz = null;

                    try {
                        clazz = classLoader.loadClass(absolutePath);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (clazz.isAnnotationPresent(Component.class)) {

                        Component componentAnnotation = clazz.getAnnotation(Component.class);
                        String beanName = componentAnnotation.value();
                        if (beanName == null || "".equals(beanName)) {
                            beanName = Introspector.decapitalize(clazz.getSimpleName());
                        }

                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setType(clazz);

                        if (clazz.isAnnotationPresent(Scope.class)) {
                            Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                            String scopeValue = scopeAnnotation.value();
                            beanDefinition.setScope(scopeValue);
                        } else {
                            beanDefinition.setScope("singleton");
                        }

                        beanDefinitionMap.put(beanName, beanDefinition);

                    }
                }
            }
        }
    }
}
