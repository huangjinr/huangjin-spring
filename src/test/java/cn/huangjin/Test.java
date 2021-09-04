package cn.huangjin;


import cn.huangjin.config.AppConfig;
import cn.huangjin.service.UserService;
import cn.spring.ApplicationContext;

public class Test {

    public static void main(String[] args) {

        ApplicationContext appletContext = new ApplicationContext(AppConfig.class);
        UserService userService = (UserService) appletContext.getBean("userService");
        UserService userService1 = (UserService) appletContext.getBean("userService");
        UserService userService2 = (UserService) appletContext.getBean("userService");

        System.out.println(userService);
        System.out.println(userService1);
        System.out.println(userService2);
    }
}
