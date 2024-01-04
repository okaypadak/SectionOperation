package dev.padak.backend;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(SectionAnnotationOperation.class)
public class SectionAutoConfiguration {

    @Bean
    public SectionAnnotationOperation sectionAnnotationOperation() {
        return new SectionAnnotationOperation();
    }
}