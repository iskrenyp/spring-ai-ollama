package com.spring.ollama.service
import groovy.util.logging.Slf4j
import org.springframework.ai.chat.ChatResponse
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.ai.document.Document
import org.springframework.ai.ollama.OllamaChatClient
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Slf4j
@Service
class ChatService {

  @Value("classpath:/prompts/system-qa.st")
  Resource qaSystemPromptResource

  final OllamaChatClient ollamaChatClient

  final VectorStore vectorStore

  @Autowired
  ChatService(OllamaChatClient aiClient, VectorStore vectorStore) {
    this.ollamaChatClient = aiClient
    this.vectorStore = vectorStore
  }

  String question(String question) {
    Message systemMessage = getDefaultSystemMessage(question)
    log.info("Created system message: ${systemMessage.content}")
    UserMessage userMessage = new UserMessage(question)
    Prompt prompt = new Prompt(List.of(systemMessage, userMessage))
    log.info("Asking codellama to reply the question: [$question].")
    ChatResponse chatResponse = ollamaChatClient.call(prompt)
    log.info("Codellama responded with ${chatResponse?.result?.output?.content}")
    chatResponse?.result?.output?.content
  }

  private Message getDefaultSystemMessage(String message) {
    SearchRequest searchRequest = SearchRequest
      .defaults()
      .withTopK(13)
      .withQuery(message)
    List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest)

    String text = similarDocuments.content.join("\n")
    log.info(String.format("Found %s relevant documents.", similarDocuments.size()))
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.qaSystemPromptResource)
    systemPromptTemplate.createMessage(Map.of("code", text))
  }
}
