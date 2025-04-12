package cz.ragnarok.ragnarok.service;

import cz.ragnarok.ragnarok.rest.dto.TestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestQuestionsService {

    @Autowired
    private FlowService flowService;

    public List<TestDto> doQuestionsTest() {
        List<TestDto> results = new ArrayList<>();
        for (String question: getQuestionsList()) {
            results.add(flowService.testFlow(question));
        }
        return results;
    }

    public List<TestDto> doQuestionsTest2() {
        List<TestDto> results = new ArrayList<>();
        for (String question: getQuestionsList()) {
            results.add(flowService.testFlow2(question));
        }
        return results;
    }

    private List<String> getQuestionsList() {
        return List.of(
                "Může si jeden z manželů vzít úvěr, aniž by s tím ten druhý souhlasil?",
                "Proč uzavřít předmanželskou smlouvu?",
                "Mají manželé nárok na stejný životní standard? Jak ho případně zajistit?",
                "Podle čeho se určuje výše výživného na dítě?",
                "Co je tzv. smluvený rozvod?",
                "Je střídavá péče po rozvodu pravidlem, nebo výjimkou?",
                "Co mám dělat, když ten druhý nesouhlasí? Jak nahradit souhlas druhého rodiče?",
                "Dostanu po rozvodu zpět finanční vnos poskytnutý na naše společné bydlení?",
                "Kdy mohu žádat o zvýšení výživného?"
        );
    }

}
