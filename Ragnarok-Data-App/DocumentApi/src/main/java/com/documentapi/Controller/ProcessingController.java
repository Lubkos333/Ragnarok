/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.documentapi.Controller;




import com.documentapi.Exception.DocumentNotFoundException;
import com.documentapi.Exception.UnsupportedFileTypeException;
import com.documentapi.Model.Chunk;
import com.documentapi.Model.CompleteChunk;
import com.documentapi.Model.CompleteDocument;
import com.documentapi.Service.CompleteChunkingService;
import com.documentapi.Service.MongoUtils;
import com.documentapi.Service.ParagraphChunkingService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
  private final CompleteChunkingService completeChunkingService;   
  private final ControllerHelperService helperService;
  private final MongoUtils mongoUtils;
  private final ParagraphChunkingService paragraphChunkingService;
  
    public ProcessingController(CompleteChunkingService completeChunkingService, ControllerHelperService helperService, MongoUtils mongoUtils, ParagraphChunkingService paragraphChunkingService) {
        this.completeChunkingService = completeChunkingService;
        this.helperService = helperService;
        this.mongoUtils = mongoUtils;
        this.paragraphChunkingService = paragraphChunkingService;
    }

        @Operation(
        summary = "Get paragraphs with metadata from a document",
        description = "Extracts paragraphs with all metadata present (chunks) from a document at the given designation. If document do not contains paragraphs, the empty list is returned instead."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Paragraphs extracted successfully",
                     content = @Content(mediaType = "application/json", 
                     schema = @Schema(implementation = Chunk.class),
                     examples = @ExampleObject(value = """
   [
{
         "main": null,
         "head": null,
         "part": null,
         "section": null,
         "title": null,
         "paragraph": null,
         "paragraphSubtitle": null,
         "content": "89 ZÁKON ze dne 3. února 2012 občanský zákoník Parlament se usnesl na tomto zákoně České republiky:"
       },
       {
         "main": "OBECNÁ ČÁST",
         "head": "PŘEDMĚT ÚPRAVY A JEJÍ ZÁKLADNÍ ZÁSADY",
         "part": "Soukromé právo",
         "section": null,
         "title": null,
         "paragraph": "§ 1",
         "paragraphSubtitle": null,
         "content": "(1)    Ustanovení právního řádu upravující vzájemná práva a povinnosti osob vytvářejí ve svém souhrnu soukromé právo. Uplatňování soukromého práva je nezávislé na uplatňování práva veřejného. (2)    Nezakazuje-li to zákon výslovně, mohou si osoby ujednat práva a povinnosti odchylně od zákona; zakázána jsou ujednání porušující dobré mravy, veřejný pořádek nebo právo týkající se postavení osob, včetně práva na ochranu osobnosti."
       },
       {
         "main": "OBECNÁ ČÁST",
         "head": "PŘEDMĚT ÚPRAVY A JEJÍ ZÁKLADNÍ ZÁSADY",
         "part": "Soukromé právo",
         "section": null,
         "title": null,
         "paragraph": "§ 2",
         "paragraphSubtitle": null,
         "content": "(1)    Každé ustanovení soukromého práva lze vykládat jenom ve shodě s Listinou základních práv a svobod a ústavním pořádkem vůbec, se zásadami, na nichž spočívá tento zákon, jakož i s trvalým zřetelem k hodnotám, které se tím chrání. Rozejde-li se výklad jednotlivého ustanovení pouze podle jeho slov s tímto příkazem, musí mu ustoupit. (2)    Zákonnému ustanovení nelze přikládat jiný význam, než jaký plyne z vlastního smyslu slov v jejich vzájemné souvislosti a z jasného úmyslu zákonodárce; nikdo se však nesmí dovolávat slov právního předpisu proti jeho smyslu. (3)    Výklad a použití právního předpisu nesmí být v rozporu s dobrými mravy a nesmí vést ke krutosti nebo bezohlednosti urážející obyčejné lidské cítění."
       },
   ]
   """))),
        @ApiResponse(responseCode = "202", description = "Processing in progress"),
        @ApiResponse(responseCode = "204", description = "No content available for given url"),
        @ApiResponse(responseCode = "415", description = "Unsupported media type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getParagraphsByDesignation")
    public ResponseEntity<List<CompleteChunk>> getParagraphsByDesignation(
        @Parameter(description = "Authorization token", example = "testApiKey") 
        @RequestHeader("Authorization") String token,
        @Parameter(description = "Designation of document", example = "89/2012 Sb.") 
        @RequestParam("designation") String designation) {
            if(mongoUtils.isProcessing()){
               return new ResponseEntity<>(HttpStatus.PROCESSING); 
            }
            if(helperService.isTokenValid(token)){
                List <CompleteChunk> response;
                try {
                    response = completeChunkingService.getParagraphs(mongoUtils.getDocxUrl(designation));
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
                } catch (DocumentNotFoundException ex) {
                    Logger.getLogger(ProcessingController.class.getName()).log(Level.SEVERE, null, ex);
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
        description = "Extracts paragraphs (chunks) from a document at the given designation. If document do not contains paragraphs, the empty list is returned instead."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Paragraphs extracted successfully",
                     content = @Content(mediaType = "application/json", 
                     schema = @Schema(implementation = Chunk.class),
                     examples = @ExampleObject(value = """
   [
{
         "title": null,
         "paragraph": null,
         "content": "89 ZÁKON ze dne 3. února 2012 občanský zákoník Parlament se usnesl na tomto zákoně České republiky:"
       },
       {
         "title": null,
         "paragraph": "§ 1",
         "content": "(1)    Ustanovení právního řádu upravující vzájemná práva a povinnosti osob vytvářejí ve svém souhrnu soukromé právo. Uplatňování soukromého práva je nezávislé na uplatňování práva veřejného. (2)    Nezakazuje-li to zákon výslovně, mohou si osoby ujednat práva a povinnosti odchylně od zákona; zakázána jsou ujednání porušující dobré mravy, veřejný pořádek nebo právo týkající se postavení osob, včetně práva na ochranu osobnosti."
       },
       {
         "title": Soukromé právo,
         "paragraph": "§ 2",
         "content": "(1)    Každé ustanovení soukromého práva lze vykládat jenom ve shodě s Listinou základních práv a svobod a ústavním pořádkem vůbec, se zásadami, na nichž spočívá tento zákon, jakož i s trvalým zřetelem k hodnotám, které se tím chrání. Rozejde-li se výklad jednotlivého ustanovení pouze podle jeho slov s tímto příkazem, musí mu ustoupit. (2)    Zákonnému ustanovení nelze přikládat jiný význam, než jaký plyne z vlastního smyslu slov v jejich vzájemné souvislosti a z jasného úmyslu zákonodárce; nikdo se však nesmí dovolávat slov právního předpisu proti jeho smyslu. (3)    Výklad a použití právního předpisu nesmí být v rozporu s dobrými mravy a nesmí vést ke krutosti nebo bezohlednosti urážející obyčejné lidské cítění."
       },
   ]
   """))),
        @ApiResponse(responseCode = "202", description = "Processing in progress"),
        @ApiResponse(responseCode = "204", description = "No content available for given url"),
        @ApiResponse(responseCode = "415", description = "Unsupported media type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
        
    @GetMapping("/getParagraphsOnlyByDesignation")
    public ResponseEntity<List<Chunk>> getParagraphsOnlyByDesignation(
        @Parameter(description = "Authorization token", example = "testApiKey") 
        @RequestHeader("Authorization") String token,
        @Parameter(description = "Designation of document", example = "89/2012 Sb.") 
        @RequestParam("designation") String designation) {
            if(mongoUtils.isProcessing()){
               return new ResponseEntity<>(HttpStatus.PROCESSING); 
            }
            if(helperService.isTokenValid(token)){
                List <Chunk> response;
                try {
                    response = paragraphChunkingService.getParagraphs(mongoUtils.getPdfUrl(designation));
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
                } catch (DocumentNotFoundException ex) {
                    Logger.getLogger(ProcessingController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            else{
               return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
    }
    
    
    
  
    @Operation(
        summary = "Get document content",
        description = "Extracts complete content from document at the given designation. Requires a valid authorization token."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Content extracted successfully",
                     content = @Content(mediaType = "application/json", 
                     schema = @Schema(implementation = Chunk.class),
                     examples = @ExampleObject(value = """
    {
         "title": "81 SDĚLENÍ Ministerstva zdravotnictví ze dne 5. ledna 2012 o vydání cenového předpisu 1/2012/DZP o regulaci cen zdravotní péče, stanovení maximálních cen zdravotní péče zubních lékařů hrazené z veřejného zdravotního pojištění, stomatologických výrobků plně hrazených z veřejného zdravotního pojištění a specifických zdravotních výkonů Ministerstvo zdravotnictví podle ustanovení § 10 zákona č. 526/1990 Sb., o cenách, ve znění pozdějších předpisů, sděluje, že dne 14. prosince 2011 vydalo cenový předpis 1/2012/DZP o regulaci cen zdravotní péče, stanovení maximálních cen zdravotní péče zubních lékařů hrazené z veřejného zdravotního pojištění, stomatologických výrobků plně hrazených z veřejného zdravotního pojištění a specifických zdravotních výkonů. Cenový předpis nabyl účinnosti dne 1. ledna 2012 a je publikován ve Věstníku Ministerstva zdravotnictví částka 11 ze dne 29. prosince 2011. Ministr: doc. MUDr. Heger, CSc., v. r. strana 1",
         "content": "81 SDĚLENÍ Ministerstva zdravotnictví ze dne 5. ledna 2012 o vydání cenového předpisu 1/2012/DZP  o regulaci cen zdravotní péče, stanovení  maximálních cen zdravotní péče zubních  lékařů hrazené z veřejného zdravotního  pojištění, stomatologických výrobků plně  hrazených z veřejného zdravotního  pojištění a specifických zdravotních výkonů Ministerstvo zdravotnictví podle ustanovení § 10 zákona č. 526/1990 Sb., o cenách, ve znění pozdějších  předpisů, sděluje, že dne 14. prosince 2011 vydalo cenový předpis 1/2012/DZP o regulaci cen zdravotní  péče, stanovení maximálních cen zdravotní péče zubních lékařů hrazené z veřejného zdravotního  pojištění, stomatologických výrobků plně hrazených z veřejného zdravotního pojištění a specifických  zdravotních výkonů. Cenový předpis nabyl účinnosti dne 1. ledna 2012 a je publikován ve Věstníku  Ministerstva zdravotnictví částka 11 ze dne 29. prosince 2011. Ministr: doc. MUDr. Heger, CSc., v. r. strana 1 "
    }
   """))),
        @ApiResponse(responseCode = "202", description = "Processing in progress"),
        @ApiResponse(responseCode = "204", description = "No content available for given url"),
        @ApiResponse(responseCode = "415", description = "Unsupported media type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getCompleteDocument")
    public ResponseEntity<CompleteDocument> getCompleteDocument(
        @Parameter(description = "Authorization token", example = "testApiKey") 
        @RequestHeader("Authorization") String token,
        @Parameter(description = "Designation of document", example = "89/2012 Sb.") 
        @RequestParam("designation") String designation) {
            if(mongoUtils.isProcessing()){
               return new ResponseEntity<>(HttpStatus.PROCESSING); 
            }
            if(helperService.isTokenValid(token)){
                CompleteDocument response;
                try {
                    response = completeChunkingService.getContent(mongoUtils.getPdfUrl(designation));
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
                } catch (DocumentNotFoundException ex) {
                    Logger.getLogger(ProcessingController.class.getName()).log(Level.SEVERE, null, ex);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            else{
               return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
    }
    
}
