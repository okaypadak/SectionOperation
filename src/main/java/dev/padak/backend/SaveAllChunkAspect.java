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

    //@Pointcut("@within(@org.springframework.stereotype.Repository @dev.padak.backend.Section *) && execution(* saveAll(..)) && args(entities)")
    //@Pointcut("execution(* org.springframework.stereotype.Repository.*.*(..)) && @within(dev.padak.backend.Section) && execution(* saveAll(..)) && args(entities)")

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
        } finally {

        }



        if (!sectionFound) {
            // If Section annotation is not found, proceed with the original joinPoint
            joinPoint.proceed();
        }

    }

    private boolean checkTransactionalAnnotation(JoinPoint joinPoint) {

        Class<?> targetClass = joinPoint.getTarget().getClass();

        // Repository sınıfı üzerinde @Transactional anotasyonunu kontrol et

            Repository repositoryAnnotation = targetClass.getAnnotation(Repository.class);
            // @Transactional anotasyonunu kontrol et
            if (repositoryAnnotation != null && targetClass.isAnnotationPresent(Section.class)) {
                System.out.println("Repository has @Transactional annotation");
                return true;
            } else {
                System.out.println("Repository does not have @Transactional annotation");
                return false;
            }
    }
    private JpaRepository<?, ?> findRepository(Object target) {
        // Assuming there is only one repository of type CrudRepository
        // You may need to adjust this based on your project's specific requirements
        return applicationContext.getBeansOfType(JpaRepository.class)
                .values()
                .stream()
                .filter(repo -> repo.getClass().isInstance(target))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Repository not found."));
    }
}
