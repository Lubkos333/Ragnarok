package cz.ragnarok.ragnarok.rest;

import cz.ragnarok.ragnarok.rest.dto.TestDto;
import cz.ragnarok.ragnarok.service.DataService;
import cz.ragnarok.ragnarok.service.TestQuestionsService;
import cz.ragnarok.ragnarok.service.VectorDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class TestRest {

    @Autowired
    private DataService dataService;

    @Autowired
    private VectorDBService vectorDBService;

    @Autowired
    private TestQuestionsService testQuestionsService;

    @GetMapping("/test")
    public String test() {
        try {
            /*List<Document> docs = vectorDBService.filteredSearch("");
            System.out.println(docs);*/
            //dataService.saveChunks();
            dataService.saveShortenChunks();
            return "ok";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @GetMapping("/test/questions")
    public List<TestDto> testQuestions() {
        return testQuestionsService.doQuestionsTest2();
        //return testQuestionsService.doQuestionsTest();
    }

    @GetMapping("/test/questions/answersOnly")
    public List<Map<String, String>> testQuestionsAnswerOnly() {
        return testQuestionsService.doQuestionsTest2().stream()
                .map(testDto -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("Question", testDto.getQuestion());
                    map.put("Answer", testDto.getAnswer());
                    return map;
                })
                .collect(Collectors.toList());
    }

}
