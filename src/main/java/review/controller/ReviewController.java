package review.controller;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import review.domain.entity.GitDiff;
import review.service.ReviewService;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/ai/review")
public class ReviewController {

    @Resource
    private ReviewService reviewService;

    /**
     * 对当前仓库最新的一次git提交进行代码评审
     *
     * @return
     * @throws Exception
     */
    @GetMapping
    public String reviewLocalRepoLatest() throws Exception {
        log.info("对当前仓库最新的一次git提交进行代码评审");
        return reviewService.review();
    }

    /**
     * 根据传入的仓库地址，对该仓库最新的一次git提交进行代码评审
     *
     * @param repoUrl 待评审仓库地址
     * @return
     * @throws Exception
     */
    @GetMapping("/{repoUrl}")
    public String reviewRemoteRepoLatest(@PathVariable("repoUrl") String repoUrl) throws Exception {
        log.info("根据传入的仓库地址，对该仓库最新的一次git提交进行代码评审，仓库地址为：{}", repoUrl);
        return reviewService.reviewByRepoUrl(repoUrl);
    }

    /**
     * 根据传入的git提交差异，对收到的git提交差异进行代码评审。仅包括git差异
     *
     * @param diff git提交差异
     * @return
     * @throws Exception
     */
    @PostMapping("/diffString")
    public String reviewAccordingDiffString(@RequestBody String diff) throws Exception {
        log.info("根据传入的git提交差异，对收到的git提交差异进行代码评审，git提交差异为：{}", diff);
        return reviewService.reviewByDiffString(diff);
    }

    /**
     * 根据传入的git提交差异，对收到的git提交差异进行代码评审。包括git差异和提交信息
     *
     * @param diff git提交差异
     * @return
     * @throws Exception
     */
    @PostMapping("/diffJson")
    public void reviewAccordingDiffJson(@RequestBody String diff) throws Exception {
        log.info("根据传入的git提交差异，对收到的git提交差异进行代码评审，git提交差异为：{}", diff);
        GitDiff gitDiff = JSON.parseObject(diff, GitDiff.class);
        reviewService.reviewByDiffJson(gitDiff);
    }

}
