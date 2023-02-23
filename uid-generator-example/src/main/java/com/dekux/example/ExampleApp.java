package com.dekux.example;

import com.dekux.uid.UidGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 示例
 *
 * @author yuan
 * @since 1.0
 */
@SpringBootApplication
public class ExampleApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ExampleApp.class, args);
        UidGenerator uidGenerator = context.getBean(UidGenerator.class);
        System.out.println(uidGenerator.getUID());
    }
}
