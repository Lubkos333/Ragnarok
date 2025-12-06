package cz.ragnarok.ragnarok.service.flow;

import cz.ragnarok.ragnarok.rest.dto.AnswerDto;
import cz.ragnarok.ragnarok.rest.dto.MessageDto;
import cz.ragnarok.ragnarok.rest.enums.FlowType;
import cz.ragnarok.ragnarok.service.SearchService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Service
public class ParaphraseFlow extends AFlow implements FlowInterface {

    private final SearchService searchService;
    private final ClassicFlow classicFlow;
    protected final static String PARAGRAPHSS = "paragraphs:";
    private final static String PARAPHRASE_PROMPT = "Přiloženou otázku uprav tak, aby byla více jako právnická řeč a obsahovala slova, "
            + "která by se mohla vyskytovat v právnickém textu, jako jsou zákoníky atd... "
            + "Ale hlavně zachovej význam otázky. Vrať mi jen a pouze tuto upravenou otázku.\n"
            + "dotaz: ";
    private final static String FAIL_PROCESSING_ANSWER = "Omlouvám se, ale na Vámi zadaný dotaz se nepodařilo zpracovat. Prosím, zkuste ho formulovat jinak.";
    private final static String ERROR_ANSWER = "Omlouváme se. Nastala nečekaná chyba. Zkuste váš dotaz znovu.";
    private final static String PARAPHRASE_VALIDATION_PROMPT = "Zkontroluj, jestli upravená otázky zachovává stoprocentně stejný význam jako původní otázka. Pokud ano, napiš mi jen a pouze ano. Pokud je upravená otázka váznamově odlišná, nebo nesmyslná, odepiš jen a pouze ne. Chci jenom tuhle jednoslovnou odpověď."+ "\n"+
            "Původní otázka: %s \n"+
            "Upravená otázka: %s";

    public ParaphraseFlow(ChatClient chatClient, InMemoryChatMemory chatMemory,  SearchService searchService, ClassicFlow classicFlow) {
        super(chatClient, chatMemory);
        this.searchService = searchService;
        this.classicFlow = classicFlow;
    }


    @Override
    public AnswerDto flow(MessageDto messageDto, WebSocketSession session) {
        return paraphraseFlow(messageDto,session, 0);
    }

    @Override
    public FlowType getType() {
        return FlowType.PARAPHRASE;
    }

    public AnswerDto paraphraseFlow(MessageDto messageDto, WebSocketSession session, Integer counter) {
        sandProgressReport(session, PROGRESS_REPORT_PROCESSING, getType());
        if (counter >= 3) {
            return classicFlow.flow(messageDto, session);
        }

        if (!questionValidation(messageDto)) {
            return badQuestionAnswer();
        }

        String paraphrase;
        int whileCounter = 0;
        do {
            if (whileCounter > 5) {
                return AnswerDto.builder()
                        .answer(FAIL_PROCESSING_ANSWER)
                        .paragraphs("")
                        .build();
            }

            paraphrase = ollamaUniqueQuestion(
                    PARAPHRASE_PROMPT + messageDto.getQuestion() + "\n"
            );
            ++whileCounter;

        } while (!paraphraseValidation(messageDto.getQuestion(), paraphrase));

        List<Document> docs;
        try {
            docs = searchService.search(messageDto.getQuestion(), paraphrase, messageDto.getNumberOfParagraphs());
        } catch (Exception e) {
            return AnswerDto.builder()
                    .answer(ERROR_ANSWER)
                    .build();
        }

        String documents = getDocumentsString(docs);
        sandProgressReport(session,String.format(PROGRESS_REPORT_DOCUMENTS, transform(docs)), getType());
        String paragraphs = "";

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
                return paraphraseFlow(messageDto, session, ++counter);
            }
        } else {
            paragraphs = transform(docs);
        }

        String prompt = buildRAGPrompt(messageDto.getQuestion(), documents, paragraphs);
        String answer = ollamaContextQuestion(prompt, messageDto.getConversationId(), paragraphs);

        return buildAnswer(answer, paragraphs, getType());
    }

    private Boolean paraphraseValidation(String originalQuestion, String paraphraseQuestion) {
        String validation = ollamaUniqueQuestion(String.format(PARAPHRASE_VALIDATION_PROMPT, originalQuestion, paraphraseQuestion)).toLowerCase();
        if(validation.contains(ANO)){
            return true;
        } else if (validation.contains(NE)) {
            return false;
        }
        return false;
    }
}
