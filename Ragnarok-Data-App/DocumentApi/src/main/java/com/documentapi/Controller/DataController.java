/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.documentapi.Controller;

import com.documentapi.Exception.DocumentNotFoundException;
import com.documentapi.Service.CompleteChunkingService;
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
    
    public DataController(MongoUtils mongoUtils,ControllerHelperService helperService){
        this.mongoUtils = mongoUtils;
        this.helperService = helperService;
    }
      
    @Operation(
        summary = "Get a related documents designations for given document ID",
        description = "Fetches a all documents designations from the MongoDB collection by ID of document for which relations are set. Requires a valid authorization token."
    )
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document retrieved successfully",
                     content = @Content(mediaType = "application/json", 
                     schema = @Schema(implementation = JsonNode.class),
                     examples = @ExampleObject(value = """
["13/1993 Sb.","770/2019 Sb.","31/2000 Sb.","181/2011 Sb.","2394/2017 Sb.","1107/2006 Sb.","65/2002 Sb.","44/1999 Sb.","7/2011 Sb.","29/2001 Sb.","22/2009 Sb.","771/2019 Sb.","114/2006 Sb.","2366/2015 Sb.",
                                                       "83/2011 Sb.","314/1990 Sb.","122/2008 Sb.","392/2009 Sb.","58/2013 Sb.","1177/2010 Sb.","34/1999 Sb.","261/2004 Sb.","2120/2015 Sb.","374/1985 Sb.",
                                                       "7/1997 Sb.","2341/2016 Sb.","2161/2019 Sb.","9/1996 Sb.","790/2019 Sb.","29/2005 Sb.","653/1986 Sb.","64/2007 Sb.","577/1985 Sb.","89/2011 Sb.",
                                                       "51/2014 Sb.","2302/2015 Sb.","1371/2007 Sb.","295/1991 Sb.","27/1998 Sb.","2006/2004 Sb.","6/1998 Sb.","843/2018 Sb.","138/2009 Sb.","23/2012 Sb.",
                                                       "403/1990 Sb.","87/1991 Sb.","229/1991 Sb.","264/1992 Sb.","68/1993 Sb.","286/1993 Sb.","331/1993 Sb.","156/1994 Sb.","223/1994 Sb.","259/1994 Sb.",
                                                       "84/1995 Sb.","104/1995 Sb.","118/1995 Sb.","89/1996 Sb.","94/1996 Sb.","142/1996 Sb.","227/1997 Sb.","15/1998 Sb.","91/1998 Sb.","165/1998 Sb.",
                                                       "159/1999 Sb.","356/1999 Sb.","360/1999 Sb.","363/1999 Sb.","27/2000 Sb.","29/2000 Sb.","30/2000 Sb.","70/2000 Sb.","103/2000 Sb.","105/2000 Sb.",
                                                       "227/2000 Sb.","301/2000 Sb.","307/2000 Sb.","362/2000 Sb.","367/2000 Sb.","370/2000 Sb.","120/2001 Sb.","229/2001 Sb.","259/2001 Sb.","317/2001 Sb.",
                                                       "353/2001 Sb.","451/2001 Sb.","15/2002 Sb.","109/2002 Sb.","125/2002 Sb.","126/2002 Sb.","151/2002 Sb.","210/2002 Sb.","308/2002 Sb.","309/2002 Sb.",
                                                       "312/2002 Sb.","320/2002 Sb.","88/2003 Sb.","437/2003 Sb.","37/2004 Sb.","47/2004 Sb.","85/2004 Sb.","257/2004 Sb.","315/2004 Sb.","360/2004 Sb.","480/2004 Sb.",
                                                       "484/2004 Sb.","499/2004 Sb.","554/2004 Sb.","179/2005 Sb.","216/2005 Sb.","359/2005 Sb.","377/2005 Sb.","383/2005 Sb.","413/2005 Sb.","56/2006 Sb.","57/2006 Sb.",
                                                       "79/2006 Sb.","81/2006 Sb.","107/2006 Sb.","112/2006 Sb.","115/2006 Sb.","134/2006 Sb.","160/2006 Sb.","227/2006 Sb.","230/2006 Sb.","264/2006 Sb.","308/2006 Sb.",
                                                       "315/2006 Sb.","342/2006 Sb.","443/2006 Sb.","269/2007 Sb.","296/2007 Sb.","344/2007 Sb.","36/2008 Sb.","104/2008 Sb.","126/2008 Sb.","130/2008 Sb.","230/2008 Sb.",
                                                       "306/2008 Sb.","384/2008 Sb.","259/2008 Sb.","198/2009 Sb.","215/2009 Sb.","217/2009 Sb.","227/2009 Sb.","230/2009 Sb.","278/2009 Sb.","285/2009 Sb.","345/2009 Sb.",
                                                       "420/2009 Sb.","155/2010 Sb.","160/2010 Sb.","409/2010 Sb.","424/2010 Sb.","427/2010 Sb.","28/2011 Sb.","139/2011 Sb.","188/2011 Sb.","113/1990 Sb.","102/1992 Sb.",
                                                       "69/1982 Sb.","33/2008 Sb.","87/1990 Sb.","208/2002 Sb.","47/1964 Sb.","126/1998 Sb.","209/2000 Sb.","522/2002 Sb.","122/1980 Sb.","132/1982 Sb.","72/1994 Sb.",
                                                       "398/1992 Sb.","121/1980 Sb.","89/1993 Sb.","152/1996 Sb.","136/2002 Sb.","360/2005 Sb.","74/1989 Sb.","171/2005 Sb.","135/2002 Sb.","136/1969 Sb.","231/2010 Sb.",
                                                       "509/1991 Sb.","591/1992 Sb.","42/1980 Sb.","50/2003 Sb.","174/2009 Sb.","248/1995 Sb.","40/1964 Sb.","18/1965 Sb.","142/1994 Sb.","440/2001 Sb.","371/2004 Sb.",
                                                       "74/1981 Sb.","158/2010 Sb.","529/1990 Sb.","73/1991 Sb.","513/1991 Sb.","132/2011 Sb.","163/2005 Sb.","83/1990 Sb.","136/1985 Sb.","146/1990 Sb.","267/1994 Sb.",
                                                       "89/1998 Sb.","385/2000 Sb.","321/2002 Sb.","15/1971 Sb.","131/1982 Sb.","27/1982 Sb.","33/2010 Sb.","302/1999 Sb.","106/1984 Sb.","116/1990 Sb.","102/1988 Sb.",
                                                       "188/1988 Sb.","540/1991 Sb.","97/1999 Sb.","59/1998 Sb.","17/1966 Sb.","234/1992 Sb.","151/2006 Sb.","152/2010 Sb.","300/1990 Sb.","258/1995 Sb.","94/1963 Sb.",
                                                       "133/1964 Sb.","99/1963 Sb.","101/1963 Sb.","45/1965 Sb.","65/1965 Sb.","124/1990 Sb.","200/1990 Sb.","526/1990 Sb.","530/1990 Sb.","63/1991 Sb.","283/1991 Sb.",
                                                       "328/1991 Sb.","455/1991 Sb.","468/1991 Sb.","549/1991 Sb.","563/1991 Sb.","582/1991 Sb.","21/1992 Sb.","214/1992 Sb.","229/1992 Sb.","238/1992 Sb.","301/1992 Sb.",
                                                       "337/1992 Sb.","344/1992 Sb.","357/1992 Sb.","358/1992 Sb.","495/1992 Sb.","511/1992 Sb.","586/1992 Sb.","593/1992 Sb.","600/1992 Sb.","634/1992 Sb.","2/1993 Sb.",
                                                       "121/1993 Sb.","217/1993 Sb.","189/1994 Sb.","40/1995 Sb.","89/1995 Sb.","117/1995 Sb.","155/1995 Sb.","219/1995 Sb.","85/1996 Sb.","252/1997 Sb.","63/1998 Sb.",
                                                       "82/1998 Sb.","155/1998 Sb.","168/1999 Sb.","325/1999 Sb.","326/1999 Sb.","359/1999 Sb.","26/2000 Sb.","219/2000 Sb.","365/2000 Sb.","492/2000 Sb.","143/2001 Sb.",
                                                       "239/2001 Sb.","319/2001 Sb.","501/2001 Sb.","201/2002 Sb.","218/2002 Sb.","476/2002 Sb.","189/2004 Sb.","190/2004 Sb.","256/2004 Sb.","340/2004 Sb.","627/2004 Sb.",
                                                       "178/2005 Sb.","412/2005 Sb.","110/2006 Sb.","111/2006 Sb.","182/2006 Sb.","183/2006 Sb.","262/2006 Sb.","307/2006 Sb.","121/2008 Sb.","125/2008 Sb.","273/2008 Sb.",
                                                       "111/2009 Sb.","277/2009 Sb.","284/2009 Sb.","100/2010 Sb.","156/2010 Sb.","134/2016 Sb.","450/1984 Sb.","101/2009 Sb."]
    """))),
        @ApiResponse(responseCode = "202", description = "Processing in progress"),
        @ApiResponse(responseCode = "204", description = "No document found for the given ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getRelated")
    public ResponseEntity<JsonNode> getRelated(
        @Parameter(description = "Authorization token", example = "testApiKey")
        @RequestHeader("Authorization")  String token, 
        @Parameter(description = "ID of the document to get relations for", example = "389695")    
        @RequestParam("id") String id) {
            if(mongoUtils.isProcessing()){
               return new ResponseEntity<>(HttpStatus.PROCESSING); 
            }
            if(helperService.isTokenValid(token)){
                try {
                    JsonNode doc = mongoUtils.getRelatedDocuments(Integer.valueOf(id));
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
                    JsonNode doc = mongoUtils.getDocumentByID(Integer.valueOf(id));
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
                    JsonNode doc = mongoUtils.getDocumentByDesignation(designation);
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
