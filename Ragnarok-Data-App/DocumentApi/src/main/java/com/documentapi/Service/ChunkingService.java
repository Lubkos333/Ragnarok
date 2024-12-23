/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.documentapi.Service;

import com.documentapi.Exception.UnsupportedFileTypeException;
import com.documentapi.Model.Chunk;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.ooxml.POIXMLProperties.CoreProperties;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
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

              if ("DOCX".equals(fileType)) {
                 return  getParagraphsFromPDF(bufferedInputStream, chunks);
              } else if ("PDF".equals(fileType)) {
                 return  getParagraphsFromPDF(bufferedInputStream, chunks);
              }
              else{
                  throw new UnsupportedFileTypeException("file is unsupported");
              }
    }
    
    public List<Chunk> getParagraphsFromPDF(InputStream inputStream, List<Chunk> chunks) throws IOException{

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
                        chunks.add(new Chunk(documentTitle, currentSubtitle, currentContent.toString().trim()));
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
                chunks.add(new Chunk(documentTitle, currentSubtitle, currentContent.toString().trim()));
            }
        }

        return chunks;
    }

 
    
    public List<Chunk> getParagraphsFromDOCX(InputStream inputStream, List<Chunk> chunks)throws IOException{
        
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            String documentTitle = extractDocxTitle(document);

            String currentSubtitle = null;
            StringBuilder currentContent = new StringBuilder();

            for (XWPFParagraph paragraph : paragraphs) {
                String line = paragraph.getText().trim();
                
                if (line.matches("^§\\s*\\d+[a-zA-Z]?$")) {
                    if (currentSubtitle != null && currentContent.length() > 0) {
                        chunks.add(new Chunk(documentTitle, currentSubtitle, currentContent.toString().trim()));
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
                chunks.add(new Chunk(documentTitle, currentSubtitle, currentContent.toString().trim()));
            }
        }

        return chunks;
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
  
private static boolean isSubtitle(String text) {
    if (text.equals(text.toUpperCase()) && text.length() > 3 && text.length() < 100) {
        return true;
    }

    String[] subtitleKeywords = { "ČÁST", "HLAVA", "DÍL", "ODDÍL", "PŘECHODNÁ USTANOVENÍ", "ZÁVĚREČNÁ USTANOVENÍ",
                                  "Předmět zákona", "Základní pojmy", "Veřejná hydrometeorologická služba" };
    for (String keyword : subtitleKeywords) {
        if (text.equalsIgnoreCase(keyword)) {
            return true;
        }
    }

    return false;
}

private static boolean isSubtitle(XWPFParagraph para) {
    String text = para.getText().trim();

    String style = para.getStyle();
    if (style != null) {
        System.out.println("Paragraph style: " + style);
        if (style.matches("Heading[1-6]") || style.equalsIgnoreCase("Subtitle") || style.equalsIgnoreCase("Nadpis")) {
            return true;
        }
    }

    if (text.equals(text.toUpperCase()) && text.length() > 3 && text.length() < 100) {
        return true;
    }

    String[] subtitleKeywords = { "ČÁST", "HLAVA", "DÍL", "ODDÍL",
        "PŘECHODNÁ USTANOVENÍ", "ZÁVĚREČNÁ USTANOVENÍ",
        "Předmět úpravy", "Základní pojmy", "Veřejná hydrometeorologická služba" };
    for (String keyword : subtitleKeywords) {
        if (text.equalsIgnoreCase(keyword)) {
            return true;
        }
    }

    return false;
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
