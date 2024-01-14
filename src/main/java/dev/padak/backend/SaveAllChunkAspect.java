package dev.padak.backend;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.spi.CurrentSessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.aspectj.AnnotationTransactionAspect;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.springframework.orm.hibernate5.SessionFactoryUtils.getDataSource;

@Aspect
@Component
public class SaveAllChunkAspect {


    @Pointcut("execution(* saveAll(..)) && args(entities)")
    public void saveAllMethod(List<?> entities) {}

    @Around("saveAllMethod(entities)")
    public void interceptSaveAll(ProceedingJoinPoint joinPoint, List<?> entities) throws Throwable {

        System.out.println("saveAllMethod çalıştı");

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

                            System.out.println("kayıt değeri:" + chunkSize);

                            //joinPoint.proceed(new Object[]{chunk,session});
                            joinPoint.proceed(new Object[]{chunk});



                        }

                    }
                }
            }

        } catch (Exception e) {
            throw e;
        }


        if (!sectionFound) {
            joinPoint.proceed();
        }

    }


}
