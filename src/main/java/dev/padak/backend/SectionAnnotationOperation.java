package dev.padak.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.lang.reflect.Method;

public class SectionAnnotationOperation implements BeanPostProcessor {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {

        if (bean.getClass().isAnnotationPresent(Repository.class)) {

            Method[] methods = bean.getClass().getMethods();

            for (Method method : methods) {
                Section sectionAnnotation = method.getAnnotation(Section.class);

                if (sectionAnnotation != null) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length > 0) {
                        Class<?> entityType = parameterTypes[0];

                        Object repositoryInstance = applicationContext.getBean(beanName);

                        return Proxy.newProxyInstance(
                                this.getClass().getClassLoader(),
                                new Class[]{entityType},
                                new BatchInvocationHandler(repositoryInstance, sectionAnnotation.value())
                        );
                    }
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    private static class BatchInvocationHandler implements InvocationHandler {

        private final Object target;
        private final int batchSize;

        public BatchInvocationHandler(Object target, int batchSize) {
            this.target = target;
            this.batchSize = batchSize;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("saveAll".equals(method.getName()) && args != null && args.length == 1 && args[0] instanceof List) {

                List<?> entities = (List<?>) args[0];

                int size = entities.size();
                for (int i = 0; i < size; i += batchSize) {
                    int end = Math.min(i + batchSize, size);
                    List<?> batch = entities.subList(i, end);
                    method.invoke(target, batch);
                }
                return null;
            }

            return method.invoke(target, args);
        }
    }
}