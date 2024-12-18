package review.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import review.domain.entity.GitDiff;
import review.infrastructure.git.GitCommand;
import review.infrastructure.mail.MailService;
import review.infrastructure.openai.IOPenAI;
import review.infrastructure.openai.dto.request.ChatCompletionRequestDTO;
import review.infrastructure.openai.dto.request.Prompt;
import review.domain.model.Model;
import review.service.ReviewService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {
    @Resource
    private GitCommand gitCommand;
    @Resource
    private IOPenAI openAi;
    @Resource
    private MailService mailService;

    @Value("${config.mail.user.from}")
    private String fromUser;
    @Value("${config.mail.user.to}")
    private String toUser;

    /**
     * 对于当前git仓库，对其最新一次git提交进行代码评审
     *
     * @return
     * @throws Exception
     */
    @Override
    public String review() throws Exception {
        // 1. 获取git提交差异
        log.info("获取git提交差异");
        String commitDiff = gitCommand.diff();
        log.info("获取git提交差异成功，git提交差异为：{}", commitDiff);

        // 2. 进行AI代码评审
        log.info("进行AI代码评审");
        String response = openAi.completions(this.getRequest(commitDiff));
        log.info("进行AI代码评审成功，AI代码评审结果为：{}", response);

        // 3. 发送代码评审邮件
        log.info("发送代码评审邮件, 发送用户为{}，接受用户为{}", fromUser, toUser);
        mailService.sendMail(response, fromUser, toUser);
        return response;
    }


    /**
     * 对于指定URL的git仓库，对其最新一次git提交进行代码评审
     *
     * @param repoUrl git仓库地址
     * @return
     * @throws Exception
     */
    @Override
    public String reviewByRepoUrl(String repoUrl) throws Exception {
        // 1. 获取git提交差异
        log.info("获取git提交差异");
        String commitDiff = gitCommand.diff(repoUrl);
        log.info("获取git提交差异成功，git提交差异为：{}", commitDiff);

        // 2. 进行AI代码评审
        log.info("进行AI代码评审");
        String response = openAi.completions(this.getRequest(commitDiff));
        log.info("进行AI代码评审成功，AI代码评审结果为：{}", response);

        // 3. 发送代码评审邮件
        log.info("发送代码评审邮件, 发送用户为{}，接受用户为{}", fromUser, toUser);
        mailService.sendMail(response, fromUser, toUser);
        return response;
    }

    /**
     * 对git提交的代码差异进行评审
     *
     * @param diffString git提交的代码差异
     * @return
     * @throws Exception
     */
    @Override
    public String reviewByDiffString(String diffString) throws Exception {
        // 1. 进行AI代码评审
        log.info("进行AI代码评审");
        String response = openAi.completions(this.getRequest(diffString));
        log.info("进行AI代码评审成功，AI代码评审结果为：{}", response);

        // 2. 发送代码评审邮件
        log.info("发送代码评审邮件, 发送用户为{}，接受用户为{}", fromUser, toUser);
        mailService.sendMail(response, fromUser, toUser);
        return response;
    }

    /**
     * 对git提交的代码差异进行评审，并将提交信息写到邮件中
     *
     * @param diffJson git提交的代码差异和提交信息
     * @return
     * @throws Exception
     */
    @Override
    public String reviewByDiffJson(GitDiff diffJson) throws Exception {
        // 1. 进行AI代码评审
        log.info("进行AI代码评审");
        String response = openAi.completions(this.getRequest(diffJson.getDiffInfo()));
        log.info("进行AI代码评审成功，AI代码评审结果为：{}", response);

        // 2. 发送代码评审邮件
        log.info("发送代码评审邮件, 发送用户为{}，接受用户为{}", fromUser, toUser);
        mailService.sendMail(response, fromUser, toUser, diffJson.getCommitInfo());
        return response;
    }

    /**
     * 获取模型请求参数
     *
     * @param content
     * @return
     */
    public ChatCompletionRequestDTO getRequest(String content) {
        ChatCompletionRequestDTO requestDTO = new ChatCompletionRequestDTO();
        requestDTO.setModel(Model.TURBO.getCode());
        List<Prompt> prompts = new ArrayList<>();
        prompts.add(new Prompt("system", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审，代码评审结果以中文的markdown的格式返回。"));
        prompts.add(new Prompt("user", content));
        requestDTO.setMessages(prompts);
        return requestDTO;
    }

}
