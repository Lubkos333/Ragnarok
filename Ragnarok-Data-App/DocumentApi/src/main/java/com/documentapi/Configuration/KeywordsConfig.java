/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.documentapi.Configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author brune
 */

@ConfigurationProperties(prefix = "keywords")
public record KeywordsConfig(
    List<String> list
) {}
