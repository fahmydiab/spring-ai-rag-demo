package org.spring.springaicourse;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.chat.memory.InMemoryChatMemory;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RestController
public class MyController {

    private final ChatClient chatClient;

    public MyController(ChatClient.Builder chatClientBuilder, PgVectorStore vectorStore) {
        ChatMemory chatMemory = new InMemoryChatMemory();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new QuestionAnswerAdvisor(vectorStore),
                        new SimpleLoggerAdvisor())
                .build();
    }


    @GetMapping("/")
    public String chat() {
        return chatClient.prompt()
                .user("What is the summery of the report")
                .call()
                .content();
    }

    @PostMapping("/ask")
    public Answer ask(
            @RequestBody Question question,
            @RequestHeader(name="X_CONV_ID", defaultValue="defaultConversation") String conversationId) {

        return chatClient.prompt()
                .user(question.question())
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .call()
                .entity(Answer.class);

    }
}
