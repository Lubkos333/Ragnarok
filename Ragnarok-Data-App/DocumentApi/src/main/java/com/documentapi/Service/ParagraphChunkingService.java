/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.documentapi.Service;

import com.documentapi.Configuration.KeywordsConfig;
import com.documentapi.Configuration.StyleConfig;
import com.documentapi.Exception.UnsupportedFileTypeException;
import com.documentapi.Model.Chunk;
import com.documentapi.Model.CompleteDocument;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.springframework.stereotype.Service;

/**
 *
 * @author brune
 */
@Service
public class ParagraphChunkingService {
      private final StyleConfig styleConfig;
    private final KeywordsConfig keywordsConfig;
   
    public ParagraphChunkingService(StyleConfig styleConfig, KeywordsConfig keywordsConfig){
        this.styleConfig = styleConfig;
        this.keywordsConfig = keywordsConfig;
        System.out.println(styleConfig.newchunk().size());
        System.out.println(keywordsConfig.list().size());
    }
    
   
 
    public List<Chunk> getParagraphs(String url) throws UnsupportedFileTypeException, IOException, InterruptedException{
        List<Chunk> chunks = new ArrayList<>();
          HttpClient httpClient = HttpClient.newHttpClient();
          HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(url))
              .build();
          HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

           InputStream inputStream = response.body();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

              bufferedInputStream.mark(10);

              byte[] header = new byte[8];
              int bytesRead = bufferedInputStream.read(header, 0, 8);

              bufferedInputStream.reset();

              String fileType = determineFileType(header, bytesRead);

   
              
              if ("PDF".equals(fileType)) {
                 return  getParagraphsFromPDF(bufferedInputStream, chunks);
              } else if ("DOCX".equals(fileType)) {
                 return  getParagraphsFromDOCX(bufferedInputStream, chunks);
              }
              else{
                  throw new UnsupportedFileTypeException("file is unsupported");
              }
    }
    
    
    
   
    public CompleteDocument getCompleteDocumentFromPDF(InputStream inputStream) throws IOException{
        try (PDDocument document = PDDocument.load(inputStream)) {
            String documentTitle = extractPdfTitleFromContent(document);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            text = text.replace("\r\n", " ").replace("\r", " ").replace("\n", " ");
            return new CompleteDocument(documentTitle, text);
        }
        
    }
    
    
    public CompleteDocument getCompleteDocumentFromDOCX(InputStream inputStream) throws IOException{
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            String documentTitle = extractDocxTitle(document);
            StringBuilder contentBuilder = new StringBuilder();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText().trim();
                if (!text.isEmpty()) {
                    contentBuilder.append(text).append(" ");
                }
            }
            return new CompleteDocument(documentTitle, contentBuilder.toString().trim());
        }
    }
    
    
    public List<Chunk> getParagraphsFromPDF(InputStream pdfInputStream, List<Chunk>  chunks) throws IOException {      
        String pdfText;
        try (PDDocument pdDoc = PDDocument.load(pdfInputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();

            pdfText = stripper.getText(pdDoc);
        }

        String[] lines = pdfText.split("\\r?\\n");

        String lastTitle = null;    


        Chunk currentChunk = new Chunk();
        StringBuilder currentContent = new StringBuilder();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue; 
            }
             
               if(isTitle(line)){

                    lastTitle = line;
                }

            if (line.matches("^§\\s\\d+[A-Za-z]?$")){
                finalizeChunk(currentChunk, currentContent, chunks);
                currentChunk = openNewChunk(lastTitle, line);
                currentContent.setLength(0);
                continue;
            }
            

            currentContent.append(line);
        }

        finalizeChunk(currentChunk, currentContent, chunks);
                       
        return chunks;
    }
    
   
    
    public List<Chunk> getParagraphsFromDOCX(InputStream inputStream,
                                                     List<Chunk> chunks) throws IOException {

        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            debugPrintParagraphStyles(document);
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            String lastTitle   = null;

            Chunk currentChunk = new Chunk();
            StringBuilder currentContent = new StringBuilder();

            for (XWPFParagraph paragraph : paragraphs) {
                String text  = paragraph.getText().trim();

                if (text.isEmpty()) {
                        continue; 
                }

                if(isTitle(text)){

                    lastTitle = text;
                }


                if (text.matches("^§\\s\\d+[A-Za-z]?$")) {
                    finalizeChunk(currentChunk, currentContent, chunks);
                    currentChunk = openNewChunk(lastTitle, text);
                    currentContent.setLength(0);
                    continue;
                }

                currentContent.append(text);
            }

            finalizeChunk(currentChunk, currentContent, chunks);
        }

        return chunks;
    }
    
    
    private boolean isTitle(String line){
            return !line.contains(".") && 
                    !line.startsWith("§") && 
                    Character.isUpperCase(line.charAt(0)) && 
                    !line.contains(",") && 
                    line.length() < 20 && 
                    !isKeywordMatch(keywordsConfig.list(),0,line) && 
                    !isKeywordMatch(keywordsConfig.list(),1,line) && 
                    !isKeywordMatch(keywordsConfig.list(),2,line) &&
                    !isKeywordMatch(keywordsConfig.list(),3,line) &&
                    !isKeywordMatch(keywordsConfig.list(),4,line);
    }


    private void finalizeChunk(Chunk chunk,
                               StringBuilder contentBuffer,
                               List<Chunk> chunks) {
        if (chunk == null) {
            return;
        }
        chunk.setContent(contentBuffer.toString().trim());

        chunks.add(chunk);
    }


    private Chunk openNewChunk(String title, String paragraphText) {
        Chunk newChunk = new Chunk();
        newChunk.setTitle(title);
        newChunk.setParagraph(paragraphText);

        return newChunk;
    }
    
    
    private boolean isStyleMatch(List<String> styleList, String styleId) {
        return styleList.stream().anyMatch(id -> id.trim().equalsIgnoreCase(styleId.trim()));
    }
    
    private boolean isKeywordMatch(List<String> keywords, int index, String text) {
        if (index >= keywords.size()) return false;
        return text.contains(keywords.get(index));
    }


    private void debugPrintParagraphStyles(XWPFDocument document) {
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        for (XWPFParagraph paragraph : paragraphs) {
            String text = paragraph.getText().trim();
            String styleId = paragraph.getStyle();
            System.out.println("Paragraph text: " + text);
            System.out.println("Style ID: " + styleId);
            if (styleId != null && document.getStyles() != null) {
                XWPFStyle styleObj = document.getStyles().getStyle(styleId);
                if (styleObj != null) {
                    System.out.println("is paragraph: " + isStyleMatch(styleConfig.newchunk(), styleId));
                    System.out.println("Style name: " + styleObj.getName());
                }
            }
            System.out.println("----");
        }
    }

    
    
    
       private  String determineFileType(byte[] header, int bytesRead) {
        if (bytesRead >= 4) {
            String pdfSignature = new String(header, 0, 5, StandardCharsets.US_ASCII);
            if ("%PDF-".equals(pdfSignature)) {
                return "PDF";
            }
            else if (header[0] == (byte) 0x50 && header[1] == (byte) 0x4B && header[2] == (byte) 0x03
                    && header[3] == (byte) 0x04) {
                return "DOCX";
            }
        }
        return null;
    }
       
    private static String extractPdfTitleFromContent(PDDocument document) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(1);
        stripper.setEndPage(1);
        String text = stripper.getText(document);

        String[] lines = text.split("\\r?\\n");

        StringBuilder titleBuilder = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("§")) {
                break;
            }
            if (titleBuilder.length() > 0) {
                titleBuilder.append(" ");
            }
            titleBuilder.append(line);
        }

        String documentTitle = titleBuilder.toString().trim();

        if (documentTitle.isEmpty()) {
            documentTitle = "Untitled Document";
        }

        return documentTitle;
    }


    private static String extractDocxTitle(XWPFDocument document) {
        String documentTitle = null;
        POIXMLProperties props = document.getProperties();
        POIXMLProperties.CoreProperties coreProps = props.getCoreProperties();
        documentTitle = coreProps.getTitle();

        if (documentTitle == null || documentTitle.isEmpty()) {
            StringBuilder titleBuilder = new StringBuilder();
            for (XWPFParagraph para : document.getParagraphs()) {
                String text = para.getText().trim();
                if (text.startsWith("§")) {
                    break;
                }
                if (titleBuilder.length() > 0) {
                    titleBuilder.append(" ");
                }
                titleBuilder.append(text);
            }
            documentTitle = titleBuilder.toString().trim();
        }

        if (documentTitle == null || documentTitle.isEmpty()) {
            documentTitle = "Untitled Document";
        }
        return documentTitle;
    }


  
  
}
