/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.documentapi.Configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "style")
public record StyleConfig(
    List<String> head,
    List<String> main,
    List<String> subtitle,
    List<String> section,
    List<String> part,
    List<String> title,
    List<String> newchunk
) {}
