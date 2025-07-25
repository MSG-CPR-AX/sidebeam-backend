package com.sidebeam.common.core.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class ApplicationContextUtil {

    private static ApplicationContext applicationContext;
    /* sonarqube ignore rule :
     * - Utility classes should not have public constructors (java:S1118),
     * - Static fields should not be updated in constructors (java:S3010)
     * this constructor is used to initialze static valiable applicationContext */
    @SuppressWarnings({"java:S1118", "java:S3010"})
    public ApplicationContextUtil(ApplicationContext ctx) {
        applicationContext = ctx;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext ctx) throws BeansException {
        applicationContext = ctx;
    }

    public static Object getBean(Class<?> class1) {
        return applicationContext.getBean(class1);
    }

    public static Object getBean(String class1) {
        return applicationContext.getBean(class1);
    }

}
