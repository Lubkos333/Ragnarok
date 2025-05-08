package cz.ragnarok.ragnarok.rest;

import cz.ragnarok.ragnarok.rest.dto.DataPostByDesignation;
import cz.ragnarok.ragnarok.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataController {

    @Autowired
    private DataService dataService;

    @PostMapping("api/data/byDesignation")
    public String uploadDocument(@RequestBody DataPostByDesignation designation) {
        return dataService.uploadDocumentByDesignation(designation.getDesignation());
    }

    @DeleteMapping("api/data/byDesignation")
    public String deleteDocument(@RequestBody DataPostByDesignation designation) {
        return dataService.deleteByDesignation(designation.getDesignation());
    }

}
