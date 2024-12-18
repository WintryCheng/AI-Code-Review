package review.infrastructure.openai.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import review.infrastructure.openai.IOPenAI;
import review.infrastructure.openai.dto.request.ChatCompletionRequestDTO;
import review.infrastructure.openai.dto.request.Prompt;

import java.util.Arrays;


/**
 * 调用通义千问模型并获取响应结果
 */
@Slf4j
@Component
public class TongYi implements IOPenAI {


    // 模型的apikey
    @Value("${config.ai.tongyi.api-key}")
    private String apiKey;


    /**
     * 根据请求数据，调用Qwen模型。以JSON格式返回Qwen的响应数据。
     *
     * @param requestDTO
     * @return
     * @throws Exception
     */
    @Override
    public String completions(ChatCompletionRequestDTO requestDTO) throws Exception {
        GenerationResult generationResult = null;
        try {
            generationResult = this.callWithMessage(requestDTO);
            log.info("模型请求为 {}，模型响应为 {}", requestDTO.toString(), generationResult.toString());
        } catch (Exception e) {
            // 使用日志框架记录异常信息
            log.error("AI服务调用异常，异常信息为: ", e);
            throw new RuntimeException("AI服务调用异常，异常信息为: ", e);
        }
        return generationResult.getOutput().getChoices().get(0).getMessage().getContent();
    }

    /**
     * 传入提示次，并调用模型，返回模型响应结果。
     *
     * @param requestDTO
     * @return
     * @throws Exception
     */
    @Override
    public GenerationResult callWithMessage(ChatCompletionRequestDTO requestDTO) throws Exception {
        Prompt systemPrompt = requestDTO.getMessages().get(0);
        Prompt userPrompt = requestDTO.getMessages().get(1);
        Generation gen = new Generation();
        Message systemMsg = Message
                .builder()
                .role(systemPrompt.getRole())
                .content(systemPrompt.getContent())
                .build();
        Message userMsg = Message
                .builder()
                .role(userPrompt.getRole())
                .content(userPrompt.getContent())
                .build();
        GenerationParam param = GenerationParam
                .builder()
                .apiKey(apiKey)
                .model(requestDTO.getModel())
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        return gen.call(param);
    }


}
