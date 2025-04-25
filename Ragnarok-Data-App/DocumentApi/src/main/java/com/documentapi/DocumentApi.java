/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.documentapi;

import com.documentapi.Configuration.KeywordsConfig;
import com.documentapi.Configuration.StyleConfig;
import java.util.Collections;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 *
 * @author brune
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties({StyleConfig.class, KeywordsConfig.class})
@EnableAsync
public class DocumentApi {

    public static void main(String[] args){
        SpringApplication app = new SpringApplication(DocumentApi.class);
        app.setDefaultProperties(Collections
          .singletonMap("server.port", "9090"));
        app.run(args);
    }
}

