package com.pulkitsood.risk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TxnRiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(TxnRiskApplication.class, args);
    }
}
