package cz.ragnarok.ragnarok.service;

import cz.ragnarok.ragnarok.rest.dto.DocumentsResponseDto;
import cz.ragnarok.ragnarok.rest.dto.TestDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FlowService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private VectorDBService vectorDBService;

    public String flow(String message) {
        String documents = getDocumentsString(message);
        String prompt = buildRAGPrompt(message, documents);

        //Response
        return ollamaUniqueQuestion(prompt);

    }

    public TestDto testFlow(String message) {
        List<Document> docs = vectorDBService.search(message);

        String documents = getDocumentsString(docs);

        String prompt = buildRAGPrompt(message, documents);

        return TestDto.builder()
                .question(message)
                .documents(
                        docs.stream().map(
                                document -> DocumentsResponseDto.builder()
                                        .text(document.getContent())
                                        .metadata(document.getMetadata())
                                        .build()
                        ).toList()
                )
                .answer(ollamaUniqueQuestion(prompt))
                .build();
    }

    private String getDocumentsString(String query) {
        List<Document> docs = vectorDBService.search(query);
        return getDocumentsString(docs);
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
        /*openAiApi.chatCompletionEntity(new OpenAiApi.ChatCompletionRequest(
                List.of(new OpenAiApi.ChatCompletionMessage(question, OpenAiApi.ChatCompletionMessage.Role.USER)),"Llama3.3",0.7
        ));*/

        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }



    public TestDto testFlow2(String message) {
        List<Document> docs = new ArrayList<>();
        String keywords = "";
        int i;
        for (i = 0; i<5 && docs.isEmpty(); i++) {
             keywords = ollamaUniqueQuestion("Vrať mi jen a pouze seznam alespoň deseti klíčových slov oddělaná čárkou, která souvisí s přiloženým dotazem a mohly by se vyskytovat v občanským zákoníku, nebo v jakémkoliv právnickém textu." + "\n" +
                    "dotaz: " + message + "\n"/* +
                     "Dále mi tuto přiloženou otázku uprav tak, aby byla více jako právnická řeč a přidej jí za seznam klíčových slov"*/);
            /*keywords = ollamaUniqueQuestion("Přiložený dotaz uprav tak, aby byla více jako právnická řeč a vrať mi jen a pouzde tuto upravenou otázku"+"\n"+
                    "dotaz: " + message + "\n");*/

            docs = vectorDBService.search(keywords);
        }
        if(docs.isEmpty()) {
            throw new RuntimeException("Zvláštní dotaz: " + message);
        }

        String documents = getDocumentsString(docs);
        System.out.println(documents);

        String prompt = buildRAGPrompt(message, documents);

        return TestDto.builder()
                .question(message)
                .documents(
                        docs.stream().map(
                                document -> DocumentsResponseDto.builder()
                                        .text(document.getContent())
                                        .metadata(document.getMetadata())
                                        .build()
                        ).toList()
                )
                .answer(ollamaUniqueQuestion(prompt))
                .keywords(keywords)
                .iterations(i)
                .build();
    }

}
