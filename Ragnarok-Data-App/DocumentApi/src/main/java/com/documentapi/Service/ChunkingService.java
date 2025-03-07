/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.documentapi.Service;

import com.documentapi.Exception.UnsupportedFileTypeException;
import com.documentapi.Model.Chunk;
import com.documentapi.Model.CompleteChunk;
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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.ooxml.POIXMLProperties.CoreProperties;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.springframework.stereotype.Service;

/**
 *
 * @author brune
 */

@Service
public class ChunkingService {
   
 public ChunkingService(){
     
 }
    public List<Chunk> processUrl(String url) throws UnsupportedFileTypeException, IOException, InterruptedException{
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

              if ("DOCX".equals(fileType)) {
                 return  processDocx(bufferedInputStream, chunks);
              } else if ("PDF".equals(fileType)) {
                 return  processPdf(bufferedInputStream, chunks);
              }
              else{
                  throw new UnsupportedFileTypeException("file is unsupported");
              }
        }
    
   
    
    public List<CompleteChunk> getParagraphs(String url) throws UnsupportedFileTypeException, IOException, InterruptedException{
        List<CompleteChunk> chunks = new ArrayList<>();
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

              if ("DOCX".equals(fileType)) {
                 return  getParagraphsFromDOCX(bufferedInputStream, chunks);
              } else if ("PDF".equals(fileType)) {
                 return  getParagraphsFromPDF(bufferedInputStream, chunks);
              }
              else{
                  throw new UnsupportedFileTypeException("file is unsupported");
              }
    }
    
    
    public CompleteDocument getContent(String url) throws UnsupportedFileTypeException, IOException, InterruptedException{
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

              if ("DOCX".equals(fileType)) {
                 return  getCompleteDocumentFromDOCX(bufferedInputStream);
              } else if ("PDF".equals(fileType)) {
                 return  getCompleteDocumentFromPDF(bufferedInputStream);
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
    
    
    public List<CompleteChunk> getParagraphsFromPDF(InputStream inputStream, List<CompleteChunk> chunks) throws IOException{

    try (PDDocument document = PDDocument.load(inputStream)) {
          String documentTitle = extractPdfTitleFromContent(document);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            String[] lines = text.split("\\r?\\n");

            String currentSubtitle = null;
            StringBuilder currentContent = new StringBuilder();

            for (String line : lines) {
                line = line.trim(); 

                if (line.matches("^§\\s*\\d+[a-zA-Z]?$")) {
                    if (currentSubtitle != null && currentContent.length() > 0) {
                        chunks.add(new CompleteChunk(documentTitle, currentSubtitle, currentContent.toString().trim(), null));
                        currentContent.setLength(0);
                    }
                    currentSubtitle = line;
                } else {
                    if (currentContent.length() > 0) {
                        currentContent.append(" ");
                    }
                    currentContent.append(line);
                }
            }
            if (currentSubtitle != null && currentContent.length() > 0) {
                chunks.add(new CompleteChunk(documentTitle, currentSubtitle, currentContent.toString().trim(), null));
            }
        }

        return chunks;
    }

 
    
    public List<CompleteChunk> getParagraphsFromDOCX(InputStream inputStream,
                                                     List<CompleteChunk> chunks) throws IOException {
        final String STYLE_HEAD = "9";
        final String STYLE_PART1 = "11";
        final String STYLE_PART2 = "22";
        final String STYLE_SECTION = "25";
        final String STYLE_NEWCHUNK1 = "15";
        final String STYLE_NEWCHUNK2 = "12";
        final String STYLE_TITLE1 = "10";
        final String STYLE_TITLE2 = "26";
        final String STYLE_SUBTITLE = "23";
        final String STYLE_MAIN = "7";

        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            String lastHead    = null;
            String lastPart    = null;
            String lastSection = null;
            String lastTitle   = null;
            String lastMain = null;

            boolean nextParagraphIsHead = false;
            boolean nextParagraphIsPart = false;
            boolean nextParagraphIsMain = false;

            CompleteChunk currentChunk = new CompleteChunk();
            StringBuilder currentContent = new StringBuilder();
            
                for (XWPFParagraph paragraph : paragraphs) {
        String line = paragraph.getText().trim();
        String styleId = paragraph.getStyle(); // or paragraph.getStyleID();

        System.out.println("Paragraph text: " + line);
        System.out.println("Style ID: " + styleId);

        if (styleId != null && document.getStyles() != null) {
            XWPFStyle styleObj = document.getStyles().getStyle(styleId);
            if (styleObj != null) {
                System.out.println("Style name: " + styleObj.getName());
            }
            
        }
        System.out.println("----");
    }

            for (XWPFParagraph paragraph : paragraphs) {
                String styleId = paragraph.getStyle();
                String text = paragraph.getText().trim();


                if (text.isEmpty()) {
                    continue;
                }

                if (STYLE_HEAD.equals(styleId) && text.contains("HLAVA")) {
                    nextParagraphIsHead = true;
                    continue; 
                }

                if ((STYLE_PART1.equals(styleId) || STYLE_PART2.equals(styleId)) && text.contains("Díl")) {
                    nextParagraphIsPart = true;
                    continue; 
                }
                if(STYLE_MAIN.equals(styleId) && text.contains("ČÁST")){
                    nextParagraphIsMain = true;
                    continue;
                }
                if(nextParagraphIsMain){
                    lastMain = text;
                    lastHead = null; 
                    lastPart = null;
                    lastSection = null;
                    lastTitle = null;
                    nextParagraphIsMain = false;
                    continue;
                }

                if (nextParagraphIsHead) {
                    lastHead = text;
                    lastPart = null;
                    lastSection = null;
                    lastTitle = null;
                    nextParagraphIsHead = false;
                    continue;
                }
                if (nextParagraphIsPart) {
                    lastPart = text;
                    lastSection = null;
                    lastTitle = null;
                    nextParagraphIsPart = false;
                    continue;
                }
                if (STYLE_SECTION.equals(styleId)) {
                    lastSection = text;
                    lastTitle = null;
                    continue;
                }

                if (STYLE_NEWCHUNK1.equals(styleId) || STYLE_NEWCHUNK2.equalsIgnoreCase(styleId)) {
                    finalizeChunk(currentChunk, currentContent, chunks);

                    currentChunk = openNewChunk(lastMain, lastHead, lastPart, lastSection, lastTitle, text);
                    currentContent.setLength(0);
                    continue;
                }

                if (STYLE_TITLE1.equals(styleId) || STYLE_TITLE2.equals(styleId)) {
                    lastTitle = text;
                    continue;
                }

                if (STYLE_SUBTITLE.equals(styleId)) {
                    currentChunk.setParagraphSubtitle(text);
                    continue;
                }

                if (currentContent.length() > 0) {
                    currentContent.append(" ");
                }
                currentContent.append(text);
            }

            finalizeChunk(currentChunk, currentContent, chunks);
        }
        return chunks;
    }

    private void finalizeChunk(CompleteChunk chunk,
                               StringBuilder contentBuffer,
                               List<CompleteChunk> chunks) {
        if (chunk == null) {
            return; 
        }
        chunk.setContent(contentBuffer.toString().trim());

        if (Objects.equals(chunk.getPart(), chunk.getTitle())) {
            chunk.setTitle(null);
        }

        chunks.add(chunk);
    }


    private CompleteChunk openNewChunk(String main,
                                       String head,
                                       String part,
                                       String section,
                                       String title,
                                       String paragraphText) {
        CompleteChunk newChunk = new CompleteChunk();
        newChunk.setMain(main);
        newChunk.setHead(head);
        newChunk.setPart(part);
        newChunk.setSection(section);
        newChunk.setTitle(title);
        newChunk.setParagraph(paragraphText);
        // paragraphSubtitle is null by default, content is filled later
        return newChunk;
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

 private List<Chunk> processDocx(InputStream inputStream, List<Chunk> chunks) throws IOException {
        XWPFDocument document = new XWPFDocument(inputStream);
  System.out.println("DOCX document loaded.");

        String documentTitle = extractDocxTitle(document);
        System.out.println("Extracted document title: " + documentTitle);

        List<XWPFParagraph> paragraphs = document.getParagraphs();
        System.out.println("Total paragraphs: " + paragraphs.size());

        StringBuilder contentBuilder = new StringBuilder();
        String currentSubtitle = null;
        Pattern sectionPattern = Pattern.compile("^\\s*§\\s*(\\d+[a-zA-Z]*)(?:\\s+(.*))?$");

        for (int i = 0; i < paragraphs.size(); i++) {
            XWPFParagraph para = paragraphs.get(i);
            String text = para.getText().trim();
            System.out.println("Paragraph " + i + ": " + text);

            if (text.isEmpty()) {
                continue; 
            }

            Matcher matcher = sectionPattern.matcher(text);
            if (matcher.matches()) {
                System.out.println("Found section: " + text);
                if (contentBuilder.length() > 0 && currentSubtitle != null) {
                    String content = contentBuilder.toString().trim();
                    Chunk chunk = new Chunk(documentTitle, currentSubtitle, content);
                    chunks.add(chunk);
                    System.out.println("Added chunk with subtitle: " + currentSubtitle);
                    contentBuilder.setLength(0);
                }

                String subtitle = matcher.group(2);
                if (subtitle != null && !subtitle.isEmpty()) {
                    currentSubtitle = subtitle.trim();
                    System.out.println("Detected subtitle on the same line: " + currentSubtitle);
                } else {
                    while (i + 1 < paragraphs.size()) {
                        XWPFParagraph nextPara = paragraphs.get(i + 1);
                        String nextText = nextPara.getText().trim();
                        if (!nextText.isEmpty()) {
                            if (isLikelySubtitle(nextPara)) {
                                currentSubtitle = nextText;
                                System.out.println("Detected subtitle on the next line: " + currentSubtitle);
                                i++;
                            } else {
                                System.out.println("Next line is not a subtitle: " + nextText);
                            }
                            break;
                        }
                        i++;
                    }
                    if (currentSubtitle == null) {
                        currentSubtitle = "No Subtitle";
                    }
                }
            } else {
                contentBuilder.append(text).append(" ");
            }
        }

        if (contentBuilder.length() > 0 && currentSubtitle != null) {
            String content = contentBuilder.toString().trim();
            Chunk chunk = new Chunk(documentTitle, currentSubtitle, content);
            chunks.add(chunk);
            System.out.println("Added final chunk with subtitle: " + currentSubtitle);
        }
        System.out.println("Finished processing DOCX.");
        return chunks;
    }

  private List<Chunk> processPdf(InputStream inputStream, List<Chunk> chunks) throws IOException {
        PDDocument document = PDDocument.load(inputStream);
     System.out.println("PDF document loaded.");

        String documentTitle = extractPdfTitleFromContent(document);
        System.out.println("Extracted document title: " + documentTitle);

        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        System.out.println("Extracted text from PDF.");

        String[] lines = text.split("\\r?\\n");
        System.out.println("Total lines: " + lines.length);

        StringBuilder contentBuilder = new StringBuilder();
        String currentSubtitle = null;
        Pattern sectionPattern = Pattern.compile("^\\s*§\\s*(\\d+[a-zA-Z]*)(?:\\s+(.*))?$");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            System.out.println("Line " + i + ": " + line);

            if (line.isEmpty()) {
                continue; 
            }

            Matcher matcher = sectionPattern.matcher(line);
            if (matcher.matches()) {
                System.out.println("Found section: " + line);
                if (contentBuilder.length() > 0 && currentSubtitle != null) {
                    String content = contentBuilder.toString().trim();
                    Chunk chunk = new Chunk(documentTitle, currentSubtitle, content);
                    chunks.add(chunk);
                    System.out.println("Added chunk with subtitle: " + currentSubtitle);
                    contentBuilder.setLength(0);
                }

                String subtitle = matcher.group(2);
                if (subtitle != null && !subtitle.isEmpty()) {
                    currentSubtitle = subtitle.trim();
                    System.out.println("Detected subtitle on the same line: " + currentSubtitle);
                } else {
                    while (i + 1 < lines.length) {
                        String nextLine = lines[i + 1].trim();
                        if (!nextLine.isEmpty()) {
                             if(isLikelySubtitle(nextLine)){
                                  currentSubtitle = nextLine;
                             } 
      
                            System.out.println("Detected subtitle on the next line: " + currentSubtitle);
                            i++; 
                            break;
                        }
                        i++;
                    }
                    if (currentSubtitle == null) {
                        currentSubtitle = "No Subtitle";
                    }
                }
            } else {
                contentBuilder.append(line).append(" ");
            }
        }

        if (contentBuilder.length() > 0 && currentSubtitle != null) {
            String content = contentBuilder.toString().trim();
            Chunk chunk = new Chunk(documentTitle, currentSubtitle, content);
            chunks.add(chunk);
            System.out.println("Added final chunk with subtitle: " + currentSubtitle);
        }
        System.out.println("Finished processing PDF.");
        return chunks;
  }
  

    private static boolean isLikelySubtitle(String text) {
        text = text.trim();
        if (text.isEmpty()) {
            return false;
        }

        if (text.matches("^\\(?\\d+[\\).]?\\s+.*") || text.matches("^[a-zA-Z][\\).]\\s+.*")) {
            return false;
        }

        if (text.equals(text.toUpperCase()) && text.length() > 2 && text.length() < 100) {
            return true;
        }

        if (!text.endsWith(".") && text.length() > 2 && text.length() < 100) {
            return true;
        }

        return false;
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


    private static boolean isLikelySubtitle(XWPFParagraph para) {
        String text = para.getText().trim();
        if (text.isEmpty()) {
            return false;
        }

        if (text.matches("^\\(?\\d+[\\).]?\\s+.*") || text.matches("^[a-zA-Z][\\).]\\s+.*")) {
            return false;
        }

        // Check paragraph style
        String style = para.getStyle();
        if (style != null) {
            if (style.matches("Heading[1-6]") || style.equalsIgnoreCase("Subtitle") || style.equalsIgnoreCase("Nadpis")) {
                return true;
            }
        }

        if (text.equals(text.toUpperCase()) && text.length() > 2 && text.length() < 100) {
            return true;
        }

        if (!text.endsWith(".") && text.length() > 2 && text.length() < 100) {
            return true;
        }

        return false;
    }


  
  
  
    private static String extractDocxTitle(XWPFDocument document) {
        String documentTitle = null;
        POIXMLProperties props = document.getProperties();
        CoreProperties coreProps = props.getCoreProperties();
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
