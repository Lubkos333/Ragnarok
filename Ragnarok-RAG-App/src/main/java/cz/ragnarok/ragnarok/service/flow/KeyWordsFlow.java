package cz.ragnarok.ragnarok.service.flow;

import cz.ragnarok.ragnarok.rest.dto.AnswerDto;
import cz.ragnarok.ragnarok.rest.dto.MessageDto;
import cz.ragnarok.ragnarok.rest.enums.FlowType;
import cz.ragnarok.ragnarok.service.VectorDBService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Service
public class KeyWordsFlow extends AFlow implements FlowInterface {

    private final VectorDBService vectorDBService;
    protected final static String PARAGRAPHSS = "paragraphs:";
    private final static String KEYWORDS_PROMPT = "Vrať mi jen a pouze seznam alespoň deseti až dvaceti klíčových slov oddělená čárkou, "
            + "která souvisí s přiloženým dotazem a mohla by se vyskytovat v právnickém textu, "
            + "jako jsou zákoníky atd...\n"
            + "dotaz: ";


    public KeyWordsFlow(ChatClient chatClient, InMemoryChatMemory chatMemory, VectorDBService vectorDBService) {
        super(chatClient, chatMemory);
        this.vectorDBService = vectorDBService;
    }

    @Override
    public AnswerDto flow(MessageDto messageDto, WebSocketSession session) {
        if (!questionValidation(messageDto)) {
            return badQuestionAnswer();
        }
        sandProgressReport(session, PROGRESS_REPORT_PROCESSING, getType());
        String keywords = ollamaUniqueQuestion(
                KEYWORDS_PROMPT + messageDto.getQuestion() + "\n"
        );

        List<Document> docs = vectorDBService.search(keywords, messageDto.getNumberOfParagraphs());
        String documents = getDocumentsString(docs);
        String paragraphs = "";

        sandProgressReport(session,String.format(PROGRESS_REPORT_DOCUMENTS, transform(docs)), getType());
        if (!answerValidation(messageDto.getQuestion(), documents)) {
            sandProgressReport(session,PROGRESS_REPORT_REVALIDATION, getType());
            List<Message> memory = chatMemory.get(messageDto.getConversationId(), 2);

            if (!memory.isEmpty()) {
                Message query = memory.getFirst();
                String queryText = extractText(query);

                int index = queryText.indexOf(FINDING_PHRASE);
                int paragraphsIndex = queryText.indexOf(PARAGRAPHSS);

                if (index != -1 && paragraphsIndex != -1) {
                    documents = queryText.substring(index + FINDING_PHRASE.length(), paragraphsIndex).trim();
                    paragraphs = queryText.substring(paragraphsIndex + PARAGRAPHSS.length()).trim();
                }
            } else {
                paragraphs = transform(docs);
            }
        } else {
            paragraphs = transform(docs);
        }

        String prompt = buildRAGPrompt(messageDto.getQuestion(), documents, paragraphs);
        String answer = ollamaContextQuestion(prompt, messageDto.getConversationId(), paragraphs);

        return buildAnswer(answer, paragraphs, getType());
    }

    @Override
    public FlowType getType() {
        return FlowType.KEYWORDS;
    }
}
