/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.documentapi.Controller;




import com.documentapi.Exception.DocumentNotFoundException;
import com.documentapi.Exception.UnsupportedFileTypeException;
import com.documentapi.Model.Chunk;
import com.documentapi.Service.ChunkingService;
import com.documentapi.Service.MongoUtils;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.util.List;
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
@RequestMapping("/api/processing")
@EnableAsync
@Api(produces = "application/json", value = "Operations for processing documents")
public class ProcessingController {
  private final ChunkingService chunkingService;   
  private final ControllerHelperService helperService;
  private final MongoUtils mongoUtils;
  
    public ProcessingController(ChunkingService chunkingService, ControllerHelperService helperService, MongoUtils mongoUtils) {
        this.chunkingService = chunkingService;
        this.helperService = helperService;
        this.mongoUtils = mongoUtils;
    }


    @Operation(
        summary = "Get processed chunks from a document",
        description = "Processes the document at the provided URL and extracts chunks by subtitles Works with DOCX and PDF links. Requires a valid authorization token."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chunks processed successfully",
                     content = @Content(mediaType = "application/json", 
                     schema = @Schema(implementation = Chunk.class),
                     examples = @ExampleObject(value = """
    [
      {
        "title": "82 VYHLÁŠKA ze dne 6. března 2012 o provádění kontrol technického stavu vozidel a jízdních souprav v provozu na pozemních komunikacích (vyhláška o technických silničních kontrolách) Ministerstvo dopravy stanoví podle § 137 odst. 2 zákona č. 361/2000 Sb., o provozu na pozemních komunikacích a o změnách některých zákonů (zákon o silničním provozu), ve znění zákona č. 478/2001 Sb., zákona č. 53/2004 Sb., zákona č. 411/2005 Sb., zákona č. 226/2006 Sb., zákona č. 274/2008 Sb., zákona č. 480/2008 Sb., zákona č. 133/2011 Sb. a zákona č. 297/2011 Sb., (dále jen „zákon“) k provedení § 6a odst. 4 zákona:",
        "subTitle": "Předmět úpravy",
        "content": "82 VYHLÁŠKA ze dne 6. března 2012 o provádění kontrol technického stavu vozidel a jízdních souprav..."
      },
      {
        "title": "82 VYHLÁŠKA ze dne 6. března 2012 o provádění kontrol technického stavu vozidel a jízdních souprav...",
        "subTitle": "Způsob provádění technické silniční kontroly",
        "content": "(1) Technická silniční kontrola se vykonává za přítomnosti řidiče po dobu nezbytně nutnou pro účely zjištění skutečného technického stavu vozidla nebo jízdní soupravy..."
      }
    ]
    """))),
        @ApiResponse(responseCode = "202", description = "Processing in progress"),
        @ApiResponse(responseCode = "204", description = "No content available in the document"),
        @ApiResponse(responseCode = "415", description = "Unsupported media type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getChunks")
    public ResponseEntity<List<Chunk>> getChunks(
        @Parameter(description = "Authorization token", example = "testApiKey") 
        @RequestHeader("Authorization") String token,
        @Parameter(description = "URL of the document to process and extract chunks", example = "https://www.e-sbirka.cz/souborove-sluzby/soubory/0492bcf7-9d99-4e86-be4b-f33c1616d373") 
        @RequestParam("url") String url) {
            if(mongoUtils.isProcessing()){
               return new ResponseEntity<>(HttpStatus.PROCESSING); 
            }
            if(helperService.isTokenValid(token)){
                List <Chunk> response;
                try {
                    response = chunkingService.processUrl(url);
                } catch (UnsupportedFileTypeException ex) {
                    Logger.getLogger(ProcessingController.class.getName()).log(Level.SEVERE, null, ex);
                     return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ProcessingController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                catch (IOException ex) {
                    Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            else{
               return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
    }
    
     @Operation(
        summary = "Get paragraphs from a document",
        description = "Extracts paragraphs (chunks) from a document at the given URL. Works with DOCX and PDF links. Requires a valid authorization token. If document do not contains paragraphs, the empty list is returned instead."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Paragraphs extracted successfully",
                     content = @Content(mediaType = "application/json", 
                     schema = @Schema(implementation = Chunk.class),
                     examples = @ExampleObject(value = """
   [
     {
       "title": "82 VYHLÁŠKA ze dne 6. března 2012 o provádění kontrol technického stavu vozidel a jízdních souprav v provozu na pozemních komunikacích (vyhláška o technických silničních kontrolách) Ministerstvo dopravy stanoví podle § 137 odst. 2 zákona č. 361/2000 Sb., o provozu na pozemních komunikacích a o změnách některých zákonů (zákon o silničním provozu), ve znění zákona č. 478/2001 Sb., zákona č. 53/2004 Sb., zákona č. 411/2005 Sb., zákona č. 226/2006 Sb., zákona č. 274/2008 Sb., zákona č. 480/2008 Sb., zákona č. 133/2011 Sb. a zákona č. 297/2011 Sb., (dále jen „zákon“) k provedení § 6a odst. 4 zákona:",
       "subTitle": "§ 1",
       "content": "82 VYHLÁŠKA ze dne 6. března 2012 o provádění kontrol technického stavu vozidel a jízdních souprav v provozu na pozemních komunikacích (vyhláška o technických silničních kontrolách)..."
     },
     {
       "title": "82 VYHLÁŠKA ze dne 6. března 2012 o provádění kontrol technického stavu vozidel a jízdních souprav v provozu na pozemních komunikacích (vyhláška o technických silničních kontrolách)...",
       "subTitle": "§ 2",
       "content": "Způsob provádění technické silniční kontroly..."
     }
   ]
   """))),
        @ApiResponse(responseCode = "202", description = "Processing in progress"),
        @ApiResponse(responseCode = "204", description = "No content available for given url"),
        @ApiResponse(responseCode = "415", description = "Unsupported media type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getParagraphs")
    public ResponseEntity<List<Chunk>> getParagraphs(
        @Parameter(description = "Authorization token", example = "testApiKey") 
        @RequestHeader("Authorization") String token,
        @Parameter(description = "URL of the document to extract paragraphs from", example = "https://www.e-sbirka.cz/souborove-sluzby/soubory/0492bcf7-9d99-4e86-be4b-f33c1616d373") 
        @RequestParam("url") String url) {
            if(mongoUtils.isProcessing()){
               return new ResponseEntity<>(HttpStatus.PROCESSING); 
            }
            if(helperService.isTokenValid(token)){
                List <Chunk> response;
                try {
                    response = chunkingService.getParagraphs(url);
                } catch (UnsupportedFileTypeException ex) {
                    Logger.getLogger(ProcessingController.class.getName()).log(Level.SEVERE, null, ex);
                     return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ProcessingController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                catch (IOException ex) {
                    Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            else{
               return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
    }
    
}
