/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mongoiswriter.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

/**
 *
 * @author brune
 */
@Component
@ConfigurationPropertiesScan
@EnableAsync
public class DataSourceConfig {
    @Value("${sbirka.url.akty.zneni}")
    public String URL_AKTY_ZNENI; 
    @Value("${sbirka.documents.number}")
    public int DOCUMENTS_NUMBER;
}

