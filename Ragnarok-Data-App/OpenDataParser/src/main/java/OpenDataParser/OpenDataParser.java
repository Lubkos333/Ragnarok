/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package OpenDataParser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

/**
 *
 * @author brune
 */
public class OpenDataParser {

    public static void main(String[] args){
        SpringApplication app = new SpringApplication(OpenDataParser.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
        
    }
}
