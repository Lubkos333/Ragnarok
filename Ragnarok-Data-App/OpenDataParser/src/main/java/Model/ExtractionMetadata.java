/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import Enum.ExtracterType;

/**
 *
 * @author brune
 */
public class ExtractionMetadata {
    
    private final String sourceURL;
    private final String collectionName;
    private final ExtracterType extracterType;
    
    public ExtractionMetadata(String sourceURL,String collectionName, ExtracterType extracterType){
        this.collectionName = collectionName;
        this.sourceURL = sourceURL;
        this.extracterType = extracterType;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public ExtracterType getExtracterType() {
        return extracterType;
    }
    

    
}
