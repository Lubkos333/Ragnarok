package cz.ragnarok.ragnarok.service;

import cz.ragnarok.ragnarok.rest.dto.DocumentsResponseDto;
import cz.ragnarok.ragnarok.rest.dto.TestDto;
import cz.ragnarok.ragnarok.rest.enums.FlowType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestFlowService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private VectorDBService vectorDBService;

    @Autowired
    private SearchService searchService;


    public TestDto classicFlow(TestDto testDto) {
        List<Document> docs = vectorDBService.search(testDto.getQuestion(), 20);

        String documents = getDocumentsString(docs);

        String paragraphs = transform(docs);

        String prompt = buildRAGPrompt(testDto.getQuestion(), documents, paragraphs);

        String answer = ollamaUniqueQuestion(prompt);

        testDto.setAnswer(answer);
        testDto.setDocuments(
                docs.stream().map(
                document -> DocumentsResponseDto.builder()
                        .text(document.getContent())
                        .metadata(document.getMetadata())
                        .build()
                ).toList()
        );

        return testDto;
    }

    public TestDto keyWordsFlow(TestDto testDto) {
        List<Document> docs = new ArrayList<>();
        String keywords = "";
        keywords = ollamaUniqueQuestion("Vrať mi jen a pouze seznam alespoň deseti až dvaceti klíčových slov oddělaná čárkou, která souvisí s přiloženým dotazem a mohla by se vyskytovat v právnickém textu, jako jsou zákoníky atd..." + "\n" +
                "dotaz: " + testDto.getQuestion() + "\n");

        docs = vectorDBService.search(keywords, 20);

        String documents = getDocumentsString(docs);

        String paragraphs = transform(docs);

        String prompt = buildRAGPrompt(testDto.getQuestion(), documents, paragraphs);

        String answer = ollamaUniqueQuestion(prompt);

        testDto.setAnswer(answer);
        testDto.setDocuments(
                docs.stream().map(
                        document -> DocumentsResponseDto.builder()
                                .text(document.getContent())
                                .metadata(document.getMetadata())
                                .build()
                ).toList()
        );
        testDto.setMiddleStep(keywords);

        return testDto;
    }

    public TestDto paraphraseFlow(TestDto testDto) {
        List<Document> docs = new ArrayList<>();
        String paraphrase = "";
        do {
            paraphrase = ollamaUniqueQuestion("Přiloženou otázku uprav tak, aby byla více jako právnická řeč a obsahovala slova, která by se mohla vyskytovat v právnickém textu, jako jsou zákoníky atd... Ale hlavně zachovej význam otázky. Vrať mi jen a pouze tuto upravenou otázku." + "\n" +
                    "dotaz: " + testDto.getQuestion() + "\n");
        }while(!paraphraseValidation(testDto.getQuestion(), paraphrase));
        paraphrase = ollamaUniqueQuestion("Přiloženou otázku uprav tak, aby byla více jako právnická řeč a obsahovala slova, která by se mohla vyskytovat v právnickém textu, jako jsou zákoníky atd... Ale hlavně zachovej význam otázky. Vrať mi jen a pouze tuto upravenou otázku." + "\n" +
                "dotaz: " + testDto.getQuestion() + "\n");


        try {
            docs = searchService.search(paraphrase, testDto.getQuestion(), 20);
        } catch (Exception e) {
            docs = vectorDBService.search(paraphrase, 20);
        }

        String documents = getDocumentsString(docs);

        String paragraphs = transform(docs);

        String prompt = buildRAGPrompt(testDto.getQuestion(), documents, paragraphs);

        String answer = ollamaUniqueQuestion(prompt);

        testDto.setAnswer(answer);
        testDto.setDocuments(
                docs.stream().map(
                        document -> DocumentsResponseDto.builder()
                                .text(document.getContent())
                                .metadata(document.getMetadata())
                                .build()
                ).toList()
        );
        testDto.setMiddleStep(paraphrase);

        return testDto;
    }


    private String getDocumentsString(List<Document> docs) {
        List<String> contents = docs
                .stream()
                .map(
                        doc -> doc.getContent()
                ).toList();

        return String.join("\n",
                contents
        );
    }

    private String buildRAGPrompt(String question, String documents, String paragraphs) {
        return "Použij informace z přiložených dokumentů, abys odpověděl na následující otázku uživatele.  \n" +
                "Odpověď by měla být:  \n" +
                "- Přesná a konkrétní  \n" +
                "- Pokud možno formátovaná pomocí Markdown (např. nadpisy, seznamy, kód)  \n" +
                "- Vždy musí obsahovat jasnou odpověď na otázku uživatele  \n" +
                "- Pokud je otázka nejasná nebo chybí kontext, nabídni relevantní výklad a případně se ptej na upřesnění\n" +
                "- Použij výhradně český jazyk \n" +
                "- Už neopakuj uživatelovu otázku \n" +
                "\n" +
                "Otázka uživatele:   \n" +
                question + "\n" +
                "Ale Odpověď můžeš čerpat jen z těchto informací: \n" +
                documents + "\n"+
                "paragraphs:"+ paragraphs;
    }

    private String ollamaUniqueQuestion(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    public TestDto testFlow(TestDto testDto, FlowType flowType) {
        switch (flowType) {
            case CLASSIC -> testDto = classicFlow(testDto);
            case KEYWORDS -> testDto = keyWordsFlow(testDto);
            case PARAPHRASE -> testDto = paraphraseFlow(testDto);
            default -> testDto = classicFlow(testDto);
        }
        return  testDto;
    }

    public String transform(List<Document> docs) {
        Map<String, Set<String>> map = new LinkedHashMap<>();

        for (Document doc : docs) {
            Map<String, Object> metadata = doc.getMetadata();
            String designation = (String) metadata.get("designation");
            String paragraph = (String) metadata.get("paragraph");

            map.computeIfAbsent(designation, k -> new LinkedHashSet<>())
                    .add(paragraph);
        }

        return map.entrySet().stream()
                .map(entry -> "* **"+entry.getKey() +"**"+ ": " + String.join("; ", entry.getValue()))
                .collect(Collectors.joining("\n"));
    }

    private Boolean paraphraseValidation(String originalQuestion, String paraphraseQuestion) {
        String validation = ollamaUniqueQuestion("Zkontroluj, jestli upravená otázky zachovává stoprocentně stejný význam jako původní otázka. Pokud ano, napiš mi jen a pouze ano. Pokud je upravená otázka váznamově odlišná, nebo nesmyslná, odepiš jen a pouze ne. Chci jenom tuhle jednoslovnou odpověď."+ "\n"+
                "Původní otázka: " + originalQuestion + "\n"+
                "Upravená otázka: " + paraphraseQuestion).toLowerCase();
        if(validation.contains("ano")){
            return true;
        } else if (validation.contains("ne")) {
            return false;
        }
        return false;
    }
}
