/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.opendataparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
public class OpenDataParser {

    public static void main(String[] args){
        System.out.println("Starting");
        SpringApplication app = new SpringApplication(OpenDataParser.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}

