/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opendataparser.Task;

import com.opendataparser.Service.PostProcessingService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 *
 * @author brune
 */

@Component
public class SetupMongoTask {

    private final PostProcessingService mongoSetupService;

    public SetupMongoTask(PostProcessingService mongoSetupService) {
        this.mongoSetupService = mongoSetupService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void executeTaskAfterStartup() {
        System.out.println("Executing startup task...");
        try {
            mongoSetupService.setupMongo();
        } catch (MalformedURLException | SocketException ex) {
            System.out.println("Error when running mongo setup");
            Logger.getLogger(SetupMongoTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SetupMongoTask.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.out.println("Task done, exiting");
            System.exit(0);  
        }
    }
}
