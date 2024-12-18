package review.infrastructure.openai.dto.request;

/**
 * 调用语言模型时，当前对话消息列表作为模型的提示输入，以JSON数组形式提供
 * 例如{"role": "user", "content": "Hello"}
 * 可能的消息类型包括：系统消息-system、用户消息-user、助手消息-assistant、工具消息-tool
 */
public class Prompt {
    private String role;
    private String content;

    public Prompt() {
    }

    public Prompt(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
