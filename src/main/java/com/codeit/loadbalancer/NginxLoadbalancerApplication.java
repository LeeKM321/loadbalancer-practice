package com.codeit.loadbalancer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NginxLoadbalancerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NginxLoadbalancerApplication.class, args);
    }

}
