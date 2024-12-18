package review.infrastructure.openai;


import com.alibaba.dashscope.aigc.generation.GenerationResult;
import review.infrastructure.openai.dto.request.ChatCompletionRequestDTO;

/**
 * AI模型通用接口，实现该接口进行AI模型调用
 */
public interface IOPenAI {
    String completions(ChatCompletionRequestDTO requestDTO) throws Exception;

    GenerationResult callWithMessage(ChatCompletionRequestDTO requestDTO) throws Exception;
}
