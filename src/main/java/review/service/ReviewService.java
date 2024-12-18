package review.service;

import org.springframework.stereotype.Service;
import review.domain.entity.GitDiff;

@Service
public interface ReviewService {
    /**
     * 对于当前git仓库，对其最新一次git提交进行代码评审
     *
     * @return
     * @throws Exception
     */
    String review() throws Exception;

    /**
     * 对于指定URL的git仓库，对其最新一次git提交进行代码评审
     *
     * @param repoUrl git仓库地址
     * @return
     * @throws Exception
     */
    String reviewByRepoUrl(String repoUrl) throws Exception;


    /**
     * 对于git提交差异，该git提交内容进行代码评审
     *
     * @param diffString git提交的代码差异
     * @return
     * @throws Exception
     */
    String reviewByDiffString(String diffString) throws Exception;


    /**
     * 对于git提交差异，该git提交内容进行代码评审
     *
     * @param diffJson git提交的代码差异和提交信息
     * @return
     * @throws Exception
     */
    String reviewByDiffJson(GitDiff diffJson) throws Exception;
}
