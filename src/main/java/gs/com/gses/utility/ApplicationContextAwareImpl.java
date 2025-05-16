package gs.com.gses.utility;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 注释此实现类 直接在引用的地方注入  ApplicationContext applicationContext 可以访问到
从容器中获取bean

 动态获取 Bean：在无法通过依赖注入获取 Bean 的情况下（例如工具类或静态方法中），可以通过 ApplicationContextAware 获取 Bean。

 获取 Spring 容器中的 Bean 或访问容器功能。它的主要使用场景包括动态获取 Bean 和访问容器功能，但应避免滥用，优先使用依赖注入。
 * @author lirui
 */
@Component
public class ApplicationContextAwareImpl implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (applicationContext == null) {
            applicationContext = context;
        }
    }

    /**
     * 获取applicationContext
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过name获取 Bean.
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过class获取Bean.
     *
     * @param clazz
     * @return
     */
    public static  <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param name
     * @param clazz
     * @return
     */
    public static  <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

}