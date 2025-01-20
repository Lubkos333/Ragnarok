/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.documentapi.Controller;

import com.documentapi.Configuration.MongoConfig;
import com.documentapi.Exception.DocumentNotFoundException;
import com.documentapi.Service.ChunkingService;
import com.documentapi.Service.MongoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author brune
 */
@RestController
@RequestMapping("/api/data")
@EnableAsync
@Api(produces = "application/json", value = "Operations for getting documents metadata")
public class DataController {
    private final MongoUtils mongoUtils;
    private final ControllerHelperService helperService;
    private final String collection;
    private final String sourceCollection;
    
    public DataController(MongoUtils mongoUtils,ControllerHelperService helperService,MongoConfig mongoConfig, ChunkingService chunkingService){
        this.mongoUtils = mongoUtils;
        this.helperService = helperService;
        collection = mongoConfig.MONGO_COLLECTION_AKTY_FINAL;
        sourceCollection = mongoConfig.MONGO_COLLECTION_AKTY_ZNENI;
    }
    
   @Operation(
        summary = "Marked for removal. Get all documents",
        description = "Gets all documents. Do not use. Requires a valid authorization token."
    )
    @GetMapping("/getAll")
    public ResponseEntity<JsonNode> getAllDocs(
        @RequestHeader("Authorization") String token,  
        @RequestParam(value = "from", required = false) Integer from, 
        @RequestParam(value = "to", required = false) Integer to) {
             if(mongoUtils.isProcessing()){
               return new ResponseEntity<>(HttpStatus.PROCESSING); 
            }

            if(helperService.isTokenValid(token)){
                try {
                  if (from == null) {
                   from = 1;
               }

               if (to == null) {
                   to = mongoUtils.getCollectionSize(collection);
               }
                    JsonNode docs = mongoUtils.getAllWithinRange(collection,from,to);
                    System.out.println(docs.size());
                    System.out.println("Source size" + mongoUtils.getCollectionSize(sourceCollection));
                     return new ResponseEntity<>(docs, HttpStatus.OK);
                } catch (JsonProcessingException ex) {
                    Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            else{
               return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
    }
    
    @Operation(
        summary = "Marked for removal. Get all links",
        description = "Gets all links. Do not use. Requires a valid authorization token."
    )
    @GetMapping("/getLinks")
    public ResponseEntity<JsonNode> getAllLinksOnly(@RequestHeader("Authorization") String token) {
             if(mongoUtils.isProcessing()){
               return new ResponseEntity<>(HttpStatus.PROCESSING); 
            }

            if(helperService.isTokenValid(token)){
                try {
                    JsonNode docs = mongoUtils.getLinksFromCollection(collection);
                     return new ResponseEntity<>(docs, HttpStatus.OK);
                } catch (JsonProcessingException ex) {
                    Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                } 
            }
            else{
               return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
    }
    
    
    @GetMapping("/getRelated")
    public ResponseEntity<JsonNode> getRelated(@RequestHeader("Authorization") String token, @RequestParam("id")String id) {
            if(mongoUtils.isProcessing()){
               return new ResponseEntity<>(HttpStatus.PROCESSING); 
            }
            if(helperService.isTokenValid(token)){
                try {
                    JsonNode doc = mongoUtils.getRelatedDocuments(sourceCollection,Integer.valueOf(id));
                     return new ResponseEntity<>(doc, HttpStatus.OK);
                } catch (JsonProcessingException ex) {
                    Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                } catch (DocumentNotFoundException ex) {
                    Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
            }
            else{
               return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
    }
    
    @Operation(
        summary = "Marked for removal. Get document by link",
        description = "Get document by link. Do not use. Requires a valid authorization token."
    )
    @GetMapping("/getByLink")
    public ResponseEntity<JsonNode> getOneByLink(@RequestHeader("Authorization") String token, @RequestParam("url")String link) {
            if(mongoUtils.isProcessing()){
               return new ResponseEntity<>(HttpStatus.PROCESSING); 
            }
            if(helperService.isTokenValid(token)){
                try {
                    JsonNode doc = mongoUtils.getMetadataByLink(collection,link);
                     return new ResponseEntity<>(doc, HttpStatus.OK);
                } catch (JsonProcessingException ex) {
                    Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                } catch (DocumentNotFoundException ex) {
                    Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
            }
            else{
               return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
    }
    
    
    @Operation(
        summary = "Get a document by ID",
        description = "Fetches a document from the MongoDB collection by its ID. Requires a valid authorization token."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document retrieved successfully",
                     content = @Content(mediaType = "application/json", 
                     schema = @Schema(implementation = JsonNode.class),
                     examples = @ExampleObject(value = """
    {
      "_id": {
        "$oid": "67617802c8fd6f7b907edffd"
      },
      "znění-dokument-id": 389695,
      "znění-base-id": 48945,
      "akt-název-vyhlášený": "Zákon občanský zákoník",
      "akt-typ-sbírky": "sb",
      "akt-označení": "2012/89",
      "akt-plné-označení": "89/2012 Sb.",
      "typ-aktu": "ZAKON",
      "cis-esb-typ-znění-po": "KONSOL",
      "znění-datum-účinnosti-od": "2024-04-01",
      "odkazován-v": [
        { "znění-dokument-id": 390579 },
        { "znění-dokument-id": 155831 },
        { "znění-dokument-id": 392049 },
        { "znění-dokument-id": 389687 },
        { "znění-dokument-id": 391253 },
        { "znění-dokument-id": 392373 },
        { "znění-dokument-id": 390011 },
        { "znění-dokument-id": 390555 },
        { "znění-dokument-id": 391099 },
        { "znění-dokument-id": 391387 },
        { "znění-dokument-id": 390237 },
        { "znění-dokument-id": 390721 },
        { "znění-dokument-id": 390117 },
        { "znění-dokument-id": 392485 },
        { "znění-dokument-id": 392069 },
        { "znění-dokument-id": 389353 },
        { "znění-dokument-id": 390761 },
        { "znění-dokument-id": 153163 }
      ],
      "odkazuje-na": [],
      "odkaz-stažení-pdf": "https://www.e-sbirka.cz/souborove-sluzby/soubory/1f83f64b-ba96-4aec-8d08-a94a9c51dd4d",
      "odkaz-stažení-docx": "https://www.e-sbirka.cz/souborove-sluzby/soubory/29eb26e7-8242-4c8e-8b4b-8c9edb1c8d7e"
    }
    """))),
        @ApiResponse(responseCode = "202", description = "Processing in progress"),
        @ApiResponse(responseCode = "204", description = "No document found for the given ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getByID")
    public ResponseEntity<JsonNode> getOneByID(
        @Parameter(description = "Authorization token", example = "testApiKey") 
        @RequestHeader("Authorization") String token,
        @Parameter(description = "ID of the document to fetch", example = "389695") 
        @RequestParam("id") String id) {
            if(mongoUtils.isProcessing()){
               return new ResponseEntity<>(HttpStatus.PROCESSING); 
            }
            if(helperService.isTokenValid(token)){
                try {
                    JsonNode doc = mongoUtils.getDocumentByID(collection,Integer.valueOf(id));
                     return new ResponseEntity<>(doc, HttpStatus.OK);
                } catch (JsonProcessingException ex) {
                    Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                } catch (DocumentNotFoundException ex) {
                    Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
            }
            else{
               return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
    }
    
    
    
    @Operation(
        summary = "Get a document by its designation",
        description = "Fetches a document from the MongoDB collection by its designation. Requires a valid authorization token."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document retrieved successfully",
                     content = @Content(mediaType = "application/json", 
                     schema = @Schema(implementation = JsonNode.class),
                     examples = @ExampleObject(value = """
    {
      "_id": {
        "$oid": "67617802c8fd6f7b907edffd"
      },
      "znění-dokument-id": 389695,
      "znění-base-id": 48945,
      "akt-název-vyhlášený": "Zákon občanský zákoník",
      "akt-typ-sbírky": "sb",
      "akt-označení": "2012/89",
      "akt-plné-označení": "89/2012 Sb.",
      "typ-aktu": "ZAKON",
      "cis-esb-typ-znění-po": "KONSOL",
      "znění-datum-účinnosti-od": "2024-04-01",
      "odkazován-v": [
        { "znění-dokument-id": 390579 },
        { "znění-dokument-id": 155831 },
        { "znění-dokument-id": 392049 },
        { "znění-dokument-id": 389687 },
        { "znění-dokument-id": 391253 },
        { "znění-dokument-id": 392373 },
        { "znění-dokument-id": 390011 },
        { "znění-dokument-id": 390555 },
        { "znění-dokument-id": 391099 },
        { "znění-dokument-id": 391387 },
        { "znění-dokument-id": 390237 },
        { "znění-dokument-id": 390721 },
        { "znění-dokument-id": 390117 },
        { "znění-dokument-id": 392485 },
        { "znění-dokument-id": 392069 },
        { "znění-dokument-id": 389353 },
        { "znění-dokument-id": 390761 },
        { "znění-dokument-id": 153163 }
      ],
      "odkazuje-na": [],
      "odkaz-stažení-pdf": "https://www.e-sbirka.cz/souborove-sluzby/soubory/1f83f64b-ba96-4aec-8d08-a94a9c51dd4d",
      "odkaz-stažení-docx": "https://www.e-sbirka.cz/souborove-sluzby/soubory/29eb26e7-8242-4c8e-8b4b-8c9edb1c8d7e"
    }
    """))),
        @ApiResponse(responseCode = "202", description = "Processing in progress"),
        @ApiResponse(responseCode = "204", description = "No document found for the given ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getByDesignation")
    public ResponseEntity<JsonNode> getOneByDesignation(
        @Parameter(description = "Authorization token", example = "testApiKey") 
        @RequestHeader("Authorization") String token,
        @Parameter(description = "Designation of the document to fetch", example = "89/2012 Sb.") 
        @RequestParam("designation") String designation) {
            if(mongoUtils.isProcessing()){
               return new ResponseEntity<>(HttpStatus.PROCESSING); 
            }
            if(helperService.isTokenValid(token)){
                try {
                    JsonNode doc = mongoUtils.getDocumentByDesignation(collection,designation);
                     return new ResponseEntity<>(doc, HttpStatus.OK);
                } catch (JsonProcessingException ex) {
                    Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                } catch (DocumentNotFoundException ex) {
                    Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
            }
            else{
               return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
    }   
}
