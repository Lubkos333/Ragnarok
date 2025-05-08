/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mongoiswriter.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

/**
 *
 * @author brune
 */
@Service
@EnableAsync
public class StringParser {

    public int extractIdAfterLastSlashAsInt(String input) throws IllegalArgumentException {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty.");
        }

        int lastSlashIndex = input.lastIndexOf('/');

        if (lastSlashIndex == -1 || lastSlashIndex == input.length() - 1) {
            throw new IllegalArgumentException("Input string must contain a '/' followed by an integer ID.");
        }

        String idStr = input.substring(lastSlashIndex + 1).trim();

        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The substring after the last '/' is not a valid integer: '" + idStr + "'");
        }
    }
    
    
      public String extractAfterLastSlashAsString(String input) {
        if (input == null || !input.contains("/")) {
            return input;
        }
        return input.substring(input.lastIndexOf('/') + 1);
    }
    
    public String extractFormattedText(String input) {
         if (input == null || input.isEmpty()) {
            return null; 
        }
         
        Pattern pattern = Pattern.compile(">([^<]+)</div>");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }
    

}
