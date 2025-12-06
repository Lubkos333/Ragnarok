package cz.ragnarok.ragnarok.service.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.ragnarok.ragnarok.rest.dto.AnswerDto;
import cz.ragnarok.ragnarok.rest.dto.MessageDto;
import cz.ragnarok.ragnarok.rest.enums.FlowType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Slf4j
public class AFlow {

    private final ObjectMapper objectMapper;
    protected final ChatClient chatClient;
    protected final InMemoryChatMemory chatMemory;
    protected final static String FINDING_PHRASE = "Ale Odpověď můžeš čerpat jen z těchto informací: \n";
    protected final static String BAD_QUESTION_ANSWER = "Omlouvám se, ale tato otázka nespadá do právního rámce, na který je systém RAGNAROK zaměřen. Pokud máte dotaz z oblasti práva, rád vám pomohu.";
    protected final static String ANO = "ano";
    protected final static String NE = "ne";
    protected final static String PARAGRAPHS = "paragraphs";
    protected final static String PARAGRAPH = "paragraph";
    protected final static String DESIGNATION = "designation";
    protected final static String PROGRESS_REPORT_PROCESSING = "Zpracovávám dotaz";
    protected final static String PROGRESS_REPORT_DOCUMENTS = "Zkoumám dokumenty: %s";
    protected final static String PROGRESS_REPORT_REVALIDATION = "Přehodnocuji správnost odpovědi";
    private final static String QUESTION_VALIDATION = "Zkontroluj, jestli přiložená otázka souvisí s právnickou tématikou. Pokud souvisí, napiš mi jen a pouze ano. Pokud nesouvisí, odepiš ne. Chci jenom tuhle jednoslovnou odpověď. \nOtázka: %s";
    private final static String QUESTION_VALIDATION_CONTINUES = "Zkontroluj, jestli přiložená otázka souvisí s právnickou tématikou, nebo navazuje na předchozí zprávu, čímž může získat právnickou tématiku. Pokud souvisí, napiš mi jen a pouze ano. Pokud nesouvisí, odepiš ne. Chci jenom tuhle jednoslovnou odpověď. \nOtázka: %s \n" + "Předchozí zpráva: %s";
    private final static String ADDITIONAL_INFO_VALIDATION_CONTINUES = "Zkontroluj, jestli přiložené informace souvisejí s položenou otázkou. Pokud ano, napiš mi jen a pouze ano. Pokud přiložené informace nesouvisí, odepiš jen a pouze ne. Chci jenom tuhle jednoslovnou odpověď.\nOtázka: %s\nPřiložené informace: \n%s";
    private final static String RAG_MAIN_PROMPT = """
            Použij informace z přiložených dokumentů, abys odpověděl na následující otázku uživatele.
            Odpověď by měla být:
            Přesná a konkrétní
            Pokud možno formátovaná pomocí Markdown (např. nadpisy, seznamy, kód)
            Vždy musí obsahovat jasnou odpověď na otázku uživatele
            Pokud je otázka nejasná nebo chybí kontext, nabídni relevantní výklad a případně se ptej na upřesnění
            Použij výhradně český jazyk
            Už neopakuj uživatelovu otázku
            
            Otázka uživatele:
            %s
            "Ale Odpověď můžeš čerpat jen z těchto informací:
            %s
            paragraphs: %s
            """;

    public AFlow(ChatClient chatClient, InMemoryChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.objectMapper = new ObjectMapper();
    }

    protected Boolean questionValidation(MessageDto messageDto) {
        List<Message> messages = chatMemory.get(messageDto.getConversationId(), 1);
        String validation = "";
        if (messages.isEmpty()) {
            validation = ollamaUniqueQuestion(String.format(QUESTION_VALIDATION, messageDto.getQuestion())).toLowerCase();
        } else {
            validation = ollamaUniqueQuestion(String.format(QUESTION_VALIDATION_CONTINUES, messageDto.getQuestion(), messages.getLast())).toLowerCase();
        }
        if (validation.contains(ANO)) {
            return true;
        } else if (validation.contains(NE)) {
            return false;
        }
        return false;
    }

    protected Boolean answerValidation(String question, String docs) {
        String validation = ollamaUniqueQuestion(String.format(ADDITIONAL_INFO_VALIDATION_CONTINUES, question, docs)).toLowerCase();

        if (validation.contains(ANO)) {
            return true;
        } else if (validation.contains(NE)) {
            return false;
        }
        return false;
    }

    protected String extractText(Message message) {
        if (message == null) return "";

        if (message instanceof UserMessage userMessage) {
            return userMessage.getText();
        }
        if (message instanceof AssistantMessage assistantMessage) {
            return assistantMessage.getText();
        }

        String text = message.getText();
        return text != null ? text : "";
    }

    protected String getDocumentsString(List<Document> docs) {
        List<String> contents = docs
                .stream()
                .map(
                        doc -> doc.getFormattedContent()
                ).toList();

        return String.join("\n",
                contents
        );
    }

    protected String buildRAGPrompt(String question, String documents, String paragraphs) {
        return String.format(RAG_MAIN_PROMPT, question, documents, paragraphs);
    }

    protected String ollamaUniqueQuestion(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    protected String ollamaContextQuestion(String question, String chatId, String paragraphs) {

        return chatClient.prompt()
                .advisors(
                        a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                .param(PARAGRAPHS, paragraphs)
                )
                .user(question)
                .call()
                .content();
    }

    protected AnswerDto buildAnswer(String answer, String docs, FlowType flowType) {
        return AnswerDto.builder()
                .answer(answer)
                .paragraphs(docs)
                .flow(flowType)
                .answerReady(true)
                .progressReport("")
                .build();
    }

    private AnswerDto buildProgressReport(String progressReport, FlowType flowType) {
        return AnswerDto.builder()
                .answer("")
                .paragraphs("")
                .flow(flowType)
                .answerReady(false)
                .progressReport(progressReport)
                .build();
    }

    protected String transform(List<Document> docs) {
        Map<String, Set<String>> map = new LinkedHashMap<>();

        for (Document doc : docs) {
            Map<String, Object> metadata = doc.getMetadata();
            String designation = (String) metadata.get(DESIGNATION);
            String paragraph = (String) metadata.get(PARAGRAPH);

            map.computeIfAbsent(designation, k -> new LinkedHashSet<>())
                    .add(paragraph);
        }

        return map.entrySet().stream()
                .map(entry -> "* **" + entry.getKey() + "**" + ": " +
                        entry.getValue().stream()
                                .map(this::fixParagraph)
                                .collect(Collectors.joining("; ")))
                .collect(Collectors.joining("\n"));
    }

    private String fixParagraph(String paragraph) {
        return paragraph.replaceAll("§\\s+(\\d+)", "§\u00A0$1");
    }

    protected AnswerDto badQuestionAnswer() {
        return AnswerDto.builder()
                .answer(BAD_QUESTION_ANSWER)
                .paragraphs("")
                .build();
    }

    protected void sandProgressReport(WebSocketSession session, String report, FlowType flowType) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(buildProgressReport(report, flowType))));
        }
        catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
