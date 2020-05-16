package com.lemon;

import lombok.extern.slf4j.Slf4j;
import org.jasig.cas.client.boot.configuration.EnableCasClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * @author lemon
 * @version 1.0
 * @description: TODO
 * @date Create by lemon on 2020-05-09 07:48
 */
@Slf4j
@SpringBootApplication
@EnableCasClient
public class LemonCasClientApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(LemonCasClientApplication.class, args);
        Environment env = configurableApplicationContext.getEnvironment();

        log.info("\n----------------------------------------------------------\n\tCas client服务 '{}' 启动完成! \n\t端口号(s): \t{}\n\t环境(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"), env.getProperty("server.port"), env.getActiveProfiles());
    }
}
