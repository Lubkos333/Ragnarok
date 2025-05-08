/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mongoiswriter.Service;

import com.mongoiswriter.Model.Segments;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

/**
 *
 * @author brune
 */
@Service
@EnableAsync
public class SegmentsExtractionUtil {

    public Segments extractSegments(String input) throws IllegalArgumentException {
       
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input string must not be null or empty.");
        }

        String trimmedInput = input.trim();
        String[] parts = trimmedInput.split("/");

        if (parts.length < 3) {
            throw new IllegalArgumentException("Input string must contain at least three '/' separated segments.");
        }

        String typSbirky = parts[parts.length - 3];
        String rokAktu = parts[parts.length - 2];
        String cisloAktu = parts[parts.length - 1];

        String combinedCisloAktu = rokAktu + "/" + cisloAktu;

        if (typSbirky.isEmpty() || rokAktu.isEmpty() || cisloAktu.isEmpty()) {
            throw new IllegalArgumentException("Extracted segments must not be empty.");
        }

        return new Segments(typSbirky, combinedCisloAktu);
        }
    
        
}
