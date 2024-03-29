package com.spring.ollama.resource
import com.spring.ollama.service.ChatService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
class ChatController {

  final ChatService chatService

  final static Map<String, String> localHistory = new LinkedHashMap<>()

  static String scrollIntoView(String elementId) {
    """
    <script>
    function scrollToElement(elementId) {
    let foundElement = document.getElementById(elementId)
    console.log("Found element")
    console.log(foundElement)
    const y = foundElement.getBoundingClientRect().top + window.scrollY;
    window.scroll({
            top: y,
            behavior: 'smooth'
            });
    }
    scrollToElement("${elementId}")
    </script>
    """
  }

  static void addHistory(String question, String answer) {
    localHistory.put(question, answer)
  }

  static String renderResponse(String question) {
    StringBuilder result = new StringBuilder()
    localHistory.each {
      String currentQuestion =  it.key
      String currentAnswer =  it.value
      currentAnswer = currentAnswer.replaceAll("\n", "<br>")
      String style = 'style="border:4px solid orange; border-radius:30px; padding: 20px;background-color: #141a3b; color: whitesmoke"'
      result.append(
        """
        <div ${style}" id="${currentQuestion}">
        <h1>Question: ${currentQuestion}</h1>
        </div>
        <br>
        <div ${style}>
        <h1>Answer:</h1>
        <br>
        ${currentAnswer}
        </div>
        <br>
      """)
    }
    result.append(scrollIntoView(question))
    result
  }

  @Autowired
  ChatController(ChatService chatService) {
    this.chatService = chatService
  }

  @GetMapping
  String completion(@RequestParam(value = "question") String question) {
    String answer = this.chatService.question(question)
    addHistory(question, answer)
    renderResponse(question)
  }
}
