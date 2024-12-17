/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.documentapi;

import java.util.Collections;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 *
 * @author brune
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
public class DocumentApi {

    public static void main(String[] args){
        SpringApplication app = new SpringApplication(DocumentApi.class);
        app.setDefaultProperties(Collections
          .singletonMap("server.port", "9090"));
        app.run(args);
    }
}

