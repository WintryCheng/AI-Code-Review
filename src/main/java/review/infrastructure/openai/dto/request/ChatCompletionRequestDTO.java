package review.infrastructure.openai.dto.request;


import java.util.List;

/**
 * ChatGLM请求参数实体类
 */
public class ChatCompletionRequestDTO {

    private String model;
    private List<Prompt> messages;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Prompt> getMessages() {
        return messages;
    }

    public void setMessages(List<Prompt> messages) {
        this.messages = messages;
    }

    public String toString() {
        return "ChatCompletionRequestDTO{" +
                "model='" + model + '\'' +
                ", messages=" + messages +
                '}';
    }
}
