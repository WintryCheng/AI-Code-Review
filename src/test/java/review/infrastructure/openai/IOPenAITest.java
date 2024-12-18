package review.infrastructure.openai;

import lombok.extern.slf4j.Slf4j;
import mockit.Deencapsulation;
import mockit.Tested;
import org.junit.Before;
import org.junit.Test;
import review.infrastructure.openai.dto.request.ChatCompletionRequestDTO;
import review.infrastructure.openai.dto.request.Prompt;
import review.infrastructure.openai.impl.TongYi;
import review.domain.model.Model;

import java.util.ArrayList;
import java.util.List;


@Slf4j
public class IOPenAITest {

    @Tested
    private IOPenAI openAI;

    @Before
    public void setUp() {
        // 创建一个OpenAI对象，传入API密钥和模型名称
        openAI = new TongYi();
        Deencapsulation.setField(openAI, "apiKey", "sk-0cec355fa19243a092753a2c6937e325");
    }

    @Test
    public void completions() throws Exception {
        ChatCompletionRequestDTO requestDTO = new ChatCompletionRequestDTO();
        requestDTO.setModel(Model.TURBO.getCode());
        List<Prompt> prompts = new ArrayList<>();
        prompts.add(new Prompt("system", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审，代码评审结果以中文的markdown的格式返回。"));
        prompts.add(new Prompt("user", "代码提交内容为：1+1=2"));
        requestDTO.setMessages(prompts);
        try {
            String generationResult = openAI.completions(requestDTO);
            log.info("模型响应输出为：{}", generationResult);
        } catch (Exception e) {
            // 使用日志框架记录异常信息
            log.error("AI服务调用异常，异常信息为: ", e);
        }


    }
}



