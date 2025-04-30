package cz.ragnarok.ragnarok.rest;

import cz.ragnarok.ragnarok.rest.dto.FullTestDto;
import cz.ragnarok.ragnarok.rest.dto.FullTestLightDto;
import cz.ragnarok.ragnarok.rest.dto.TestDto;
import cz.ragnarok.ragnarok.rest.enums.FlowType;
import cz.ragnarok.ragnarok.service.DataService;
import cz.ragnarok.ragnarok.service.TestQuestionsService;
import cz.ragnarok.ragnarok.service.VectorDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/test/questions")
    public List<TestDto> testQuestions(@RequestParam(name = "flowType") FlowType flowType) {
        return testQuestionsService.doQuestionsTest(flowType);
    }

    @GetMapping("/test/questions/answersOnly")
    public List<Map<String, String>> testQuestionsAnswerOnly(@RequestParam(name = "flowType") FlowType flowType) {
        return testQuestionsService.doQuestionsTest(flowType).stream()
                .map(testDto -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("Question", testDto.getQuestion());
                    map.put("Answer", testDto.getAnswer());
                    map.put("ExpectedAnswer", testDto.getExpectedAnswer());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/test/questions/full")
    public FullTestDto testQuestionsFull() {
        return testQuestionsService.doQuestionsFullTest();
    }

    @GetMapping("/test/questions/full/light")
    public FullTestLightDto testQuestionsFullLight() {
        return testQuestionsService.doQuestionsFullTestLight();
    }

}
