package cz.ragnarok.ragnarok.service;

import cz.ragnarok.ragnarok.rest.dto.AnswerDto;
import cz.ragnarok.ragnarok.rest.dto.MessageDto;
import cz.ragnarok.ragnarok.rest.enums.FlowType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Service
public class FlowService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private VectorDBService vectorDBService;

    @Autowired
    private InMemoryChatMemory chatMemory;

    private Boolean questionValidation(String question) {
        String validation = ollamaUniqueQuestion("Zkontroluj, jestli přiložená otázka souvisí s právnickou tématikou. Pokud souvisí, napiš mi jen a pouze ano. Pokud nesouvisí, odepiš ne. Chci jenom tuhle jednoslovnou odpověď"+ "\n"+
                "Otázka: " + question).toLowerCase();

        if(validation.contains("ano")){
            return true;
        } else if (validation.contains("ne")) {
            return false;
        }
        return false;
    }

    private Boolean questionValidation(MessageDto messageDto) {
        List<Message> messages = chatMemory.get(messageDto.getConversationId(), 1);
        String validation = "";
        if(messages.isEmpty()) {
            validation = ollamaUniqueQuestion("Zkontroluj, jestli přiložená otázka souvisí s právnickou tématikou. Pokud souvisí, napiš mi jen a pouze ano. Pokud nesouvisí, odepiš ne. Chci jenom tuhle jednoslovnou odpověď."+ "\n"+
                    "Otázka: " + messageDto).toLowerCase();
        }
        else {
            validation = ollamaUniqueQuestion("Zkontroluj, jestli přiložená otázka souvisí s právnickou tématikou, nebo navazuje na předchozí zprávu, čímž může získat právnickou tématiku. Pokud souvisí, napiš mi jen a pouze ano. Pokud nesouvisí, odepiš ne. Chci jenom tuhle jednoslovnou odpověď." + "\n" +
                    "Otázka: " + messageDto.getQuestion()).toLowerCase() + "\n" +
                    "Předchozí zpráva: " + messages.getLast();
        }
        if(validation.contains("ano")){
            return true;
        } else if (validation.contains("ne")) {
            return false;
        }
        return false;
    }

    private Boolean answerValidation(String question, String docs) {
        String validation = ollamaUniqueQuestion("Zkontroluj, jestli přiložené informace souvisejí s položenou otázkou. Pokud ano, napiš mi jen a pouze ano. Pokud přiložené informace nesouvisí, odepiš jen a pouze ne. Chci jenom tuhle jednoslovnou odpověď."+ "\n"+
                "Otázka: " + question + "\n"+
                "Přiložené informace: " + "\n" + docs).toLowerCase();

        if(validation.contains("ano")){
            return true;
        } else if (validation.contains("ne")) {
            return false;
        }
        return false;
    }


    public AnswerDto classicFlow(MessageDto messageDto) {
        if(!questionValidation(messageDto)) {
            return AnswerDto.builder().answer("Omlouvám se, ale tato otázka nespadá do právního rámce, na který je systém RAGNAROK zaměřen. Pokud máte dotaz z oblasti práva, rád vám pomohu.").paragraphs("").build();
        }

        List<Document> docs = vectorDBService.search(messageDto.getQuestion());

        String documents = getDocumentsString(docs);

        String paragraphs = "";

        if(!answerValidation(messageDto.getQuestion(), documents)) {
            if(!chatMemory.get(messageDto.getConversationId(),2).isEmpty()) {
                Message query = chatMemory.get(messageDto.getConversationId(),2).getFirst();
                String findingPhrase = "Ale Odpověď můžeš čerpat jen z těchto informací: \n";

                int index = query.getContent().indexOf(findingPhrase);
                int paragraphsIndex = query.getContent().indexOf("paragraphs:");
                if (index != -1) {
                    documents = query.getContent().substring(index + findingPhrase.length(), paragraphsIndex).trim();
                    paragraphs = query.getContent().substring(paragraphsIndex + "paragraphs:".length()).trim();
                }
            }
        }
        else {
            paragraphs = docs.stream().map(
                            document -> document.getMetadata().get("paragraph").toString()
                    )
                    .distinct()
                    .collect(Collectors.joining(", "));
        }

        String prompt = buildRAGPrompt(messageDto.getQuestion(), documents, paragraphs);

        String answer = ollamaContextQuestion(prompt, messageDto.getConversationId(), paragraphs);

        return buildAnswer(answer, paragraphs, FlowType.CLASSIC);
    }

    public AnswerDto keyWordsFlow(MessageDto messageDto) {
        if(!questionValidation(messageDto.getQuestion())) {
            return AnswerDto.builder().answer("Omlouvám se, ale tato otázka nespadá do právního rámce, na který je systém RAGNAROK zaměřen. Pokud máte dotaz z oblasti práva, rád vám pomohu.").paragraphs("").build();        }

        List<Document> docs = new ArrayList<>();
        String keywords = "";

        keywords = ollamaUniqueQuestion("Vrať mi jen a pouze seznam alespoň deseti klíčových slov oddělaná čárkou, která souvisí s přiloženým dotazem a mohly by se vyskytovat v občanským zákoníku, nebo v jakémkoliv právnickém textu." + "\n" +
                "dotaz: " + messageDto.getQuestion() + "\n");

        docs = vectorDBService.search(keywords);

        String documents = getDocumentsString(docs);

        String paragraphs = "";

        if(!answerValidation(messageDto.getQuestion(), documents)) {
            if(!chatMemory.get(messageDto.getConversationId(),2).isEmpty()) {
                Message query = chatMemory.get(messageDto.getConversationId(),2).getFirst();
                String findingPhrase = "Ale Odpověď můžeš čerpat jen z těchto informací: \n";

                int index = query.getContent().indexOf(findingPhrase);
                int paragraphsIndex = query.getContent().indexOf("paragraphs:");
                if (index != -1) {
                    documents = query.getContent().substring(index + findingPhrase.length(), paragraphsIndex).trim();
                    paragraphs = query.getContent().substring(paragraphsIndex + "paragraphs:".length()).trim();
                }
            }
        }
        else {
            paragraphs = docs.stream().map(
                            document -> document.getMetadata().get("paragraph").toString()
                    )
                    .distinct()
                    .collect(Collectors.joining(", "));
        }

        String prompt = buildRAGPrompt(messageDto.getQuestion(), documents, paragraphs);

        String answer = ollamaContextQuestion(prompt, messageDto.getConversationId(), paragraphs);

        return buildAnswer(answer, paragraphs, FlowType.KEYWORDS);
    }

    public AnswerDto paraphraseFlow(MessageDto messageDto) {
        if (!questionValidation(messageDto.getQuestion())) {
            return AnswerDto.builder().answer("Omlouvám se, ale tato otázka nespadá do právního rámce, na který je systém RAGNAROK zaměřen. Pokud máte dotaz z oblasti práva, rád vám pomohu.").paragraphs("").build();
        }
        List<Document> docs = new ArrayList<>();
        String keywords = "";
        keywords = ollamaUniqueQuestion("Přiloženou otázku uprav tak, aby byla více jako právnická řeč a obsahovala slova, která by se mohly vyskytovat v občanským zákoníku, nebo v jakémkoliv právnickém textu. Vrať mi jen a pouze tuto upravenou otázku." + "\n" +
                "dotaz: " + messageDto.getQuestion() + "\n");

        docs = vectorDBService.search(keywords);

        String documents = getDocumentsString(docs);

        String paragraphs = "";

        if(!answerValidation(messageDto.getQuestion(), documents)) {
            if(!chatMemory.get(messageDto.getConversationId(),2).isEmpty()) {
                Message query = chatMemory.get(messageDto.getConversationId(),2).getFirst();
                String findingPhrase = "Ale Odpověď můžeš čerpat jen z těchto informací: \n";

                int index = query.getContent().indexOf(findingPhrase);
                int paragraphsIndex = query.getContent().indexOf("paragraphs:");
                if (index != -1) {
                    documents = query.getContent().substring(index + findingPhrase.length(), paragraphsIndex).trim();
                    paragraphs = query.getContent().substring(paragraphsIndex + "paragraphs:".length()).trim();
                }
            }
        }
        else {
            paragraphs = docs.stream().map(
                            document -> document.getMetadata().get("paragraph").toString()
                    )
                    .distinct()
                    .collect(Collectors.joining(", "));
        }

        String prompt = buildRAGPrompt(messageDto.getQuestion(), documents, paragraphs);

        String answer = ollamaContextQuestion(prompt, messageDto.getConversationId(), paragraphs);

        return buildAnswer(answer, paragraphs, FlowType.PARAPHRASE);
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

    private String buildRAGPrompt(String question, String documents, String paragraphs) {
        return "Odpověz uživateli na tuto otázku: \n" +
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

    private String ollamaContextQuestion(String question, String chatId) {

        return chatClient.prompt()
                .advisors(
                        a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                )
                .user(question)
                .call()
                .content();
    }

    private String ollamaContextQuestion(String question, String chatId, String paragraphs) {

        return chatClient.prompt()
                .advisors(
                        a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                .param("paragraphs", paragraphs)
                )
                .user(question)
                .call()
                .content();
    }


    private AnswerDto buildAnswer(String answer, List<Document> docs, FlowType flowType) {
        return AnswerDto.builder()
                .answer(answer)
                .paragraphs(
                        docs.stream().map(
                                document -> document.getMetadata().get("paragraph").toString()
                        )
                        .distinct()
                        .collect(Collectors.joining(", "))
                )
                .flow(flowType)
                .build();
    }

    private AnswerDto buildAnswer(String answer, String docs, FlowType flowType) {
        return AnswerDto.builder()
                .answer(answer)
                .paragraphs(docs)
                .flow(flowType)
                .build();
    }

}
