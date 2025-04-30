package cz.ragnarok.ragnarok.service;

import cz.ragnarok.ragnarok.rest.dto.DocumentsResponseDto;
import cz.ragnarok.ragnarok.rest.dto.TestDto;
import cz.ragnarok.ragnarok.rest.enums.FlowType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestFlowService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private VectorDBService vectorDBService;


    public TestDto classicFlow(TestDto testDto) {
        List<Document> docs = vectorDBService.search(testDto.getQuestion());

        String documents = getDocumentsString(docs);
        String prompt = buildRAGPrompt(testDto.getQuestion(), documents);

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
        keywords = ollamaUniqueQuestion("Vrať mi jen a pouze seznam alespoň deseti klíčových slov oddělaná čárkou, která souvisí s přiloženým dotazem a mohly by se vyskytovat v občanským zákoníku, nebo v jakémkoliv právnickém textu." + "\n" +
                "dotaz: " + testDto.getQuestion() + "\n");

        docs = vectorDBService.search(keywords);

        String documents = getDocumentsString(docs);

        String prompt = buildRAGPrompt(testDto.getQuestion(), documents);

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
        paraphrase = ollamaUniqueQuestion("Přiloženou otázku uprav tak, aby byla více jako právnická řeč a obsahovala slova, která by se mohly vyskytovat v občanským zákoníku, nebo v jakémkoliv právnickém textu. Vrať mi jen a pouze tuto upravenou otázku." + "\n" +
                "dotaz: " + testDto.getQuestion() + "\n");

        docs = vectorDBService.search(paraphrase);

        String documents = getDocumentsString(docs);

        String prompt = buildRAGPrompt(testDto.getQuestion(), documents);

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

    private String buildRAGPrompt(String question, String documents) {
        return "Odpověz uživateli na tuto otázku: \n" +
                question + "\n" +
                "Ale Odpověď můžeš čerpat jen z těchto informací: \n" +
                documents;
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
}
