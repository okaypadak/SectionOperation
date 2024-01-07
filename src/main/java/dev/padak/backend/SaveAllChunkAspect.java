package dev.padak.backend;

import jakarta.transaction.Transactional;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.aspectj.AnnotationTransactionAspect;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.springframework.orm.hibernate5.SessionFactoryUtils.getDataSource;

@Aspect
@Component
public class SaveAllChunkAspect {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Pointcut("execution(* saveAll(..)) && args(entities)")
    public void saveAllMethod(List<?> entities) {}

    @Around("saveAllMethod(entities)")
    public void interceptSaveAll(ProceedingJoinPoint joinPoint, List<?> entities) throws Throwable {

        System.out.println("saveAllMethod çalıştı");

        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus = this.transactionManager.getTransaction(transactionDefinition);

        int chunkSize = 0;
        boolean sectionFound = false;


        try {
            Class<?>[] interfaces = joinPoint.getTarget().getClass().getInterfaces();

            for (Class<?> interfaceClass : interfaces) {

                Annotation[] annotations = interfaceClass.getAnnotations();

                for (Annotation annotation : annotations) {
                    if (annotation instanceof Section) {
                        Section section = (Section) annotation;
                        System.out.println("section değeri: " + section.value());
                        chunkSize = section.value();
                        sectionFound = true;

                        for (int i = 0; i < entities.size(); i += chunkSize) {
                            int endIndex = Math.min(i + chunkSize, entities.size());
                            List<?> chunk = entities.subList(i, endIndex);
                  
                            joinPoint.proceed(new Object[]{chunk});
                            System.out.println("kayıt değeri:" + chunkSize);
                        }
                    }
                }
            }
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }

        if (!sectionFound) {
            joinPoint.proceed();
        }

    }


}
