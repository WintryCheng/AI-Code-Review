package review.infrastructure.git;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class GitCommandTest {

    /**
     * 获取当前代码库的代码差异
     *
     * @throws Exception
     */
    @Test
    public void testDiff1() throws Exception {
        GitCommand gitCommand = new GitCommand();
        String diff = gitCommand.diff();
        log.info("当前代码块最近两次提交的代码差异为: {}", diff);

    }

    /**
     * 获取指定远程仓库最后一次git提交与前一次提交之间的差异
     *
     * @throws Exception
     */
    @Test
    public void testDiff2() throws Exception {
        GitCommand gitCommand = new GitCommand();
        String diff = gitCommand.diff("git@47.109.110.214:AICodeReview.git");
        log.info("当前代码块最近两次提交的代码差异为: {}", diff);

    }

    /**
     * 获取指定远程仓库某次git提交与前一次提交之间的差异
     *
     * @throws Exception
     */
    @Test
    public void testDiff3() throws Exception {
        GitCommand gitCommand = new GitCommand();
        String diff = gitCommand.diff("git@47.109.110.214:AICodeReview.git", "edb959b67ffd516646d577e2c42fbb2cc3499843");
        log.info("当前代码块最近两次提交的代码差异为: {}", diff);
    }
}