package review.infrastructure.git;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 获取 git 提交差异
 */
@Slf4j
@Component
public class GitCommand {

    @Value("${config.ssh.key.url}")
    private String sshKeyUrl;

    public static String workSpacePrefix = "./repos/";

    // 构造函数
    public GitCommand() {
    }


    /**
     * 获取当前git仓库最新一次提交与前一次提交之间的差异
     *
     * @return git提交差异
     * @throws IOException
     * @throws InterruptedException
     */
    public String diff() throws IOException, InterruptedException {
        // 1. 使用 ProcessBuilder 操作进程（便于进行git操作）
        // 创建 ProcessBuilder 实例，用于执行 git log 命令获取最新的一条提交的 hash值
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:%H");
        logProcessBuilder.directory(new File(".")); // 设置工作目录为当前目录
        Process logProcess = logProcessBuilder.start(); // 启动 git log 进程

        // 2. 读取 git log 命令的输出，获取最新的一次提交的 hash值
        BufferedReader logReader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
        String lastestCommitHash = logReader.readLine();
        logReader.close();
        logProcess.waitFor();

        // 3. 使用 ProcessBuilder 操作进程（便于进行git操作）
        // 获取最新一次提交与过去1次提交之间的文件差异（多少个 ^ 就是多少次之前的提交）
        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", lastestCommitHash + "^", lastestCommitHash);
        diffProcessBuilder.directory(new File(".")); // 设置工作目录为当前目录
        Process diffProcess = diffProcessBuilder.start(); // 启动 git diff 进程

        // 4. 读取 git diff 命令的输出，按行获取文件差异
        StringBuilder diffCode = new StringBuilder();
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
        String line;
        while ((line = diffReader.readLine()) != null) {
            diffCode.append(line).append("\n");
        }
        diffReader.close();

        // 4. 获取 git diff 命令的返回码，0表示执行成功
        int exitCode = diffProcess.waitFor();
        if (exitCode != 0) {
            // 返回码不为0，抛出异常
            throw new RuntimeException("Failed to get git diff, exit code is: " + exitCode);
        }

        // 5. 返回最近两次提交文件的差异
        return diffCode.toString();
    }

    /**
     * 获取指定远程仓库最后一次git提交与前一次提交之间的差异
     *
     * @param repoUrl 远程仓库地址
     * @return git提交差异
     * @throws IOException
     * @throws InterruptedException
     */
    public String diff(String repoUrl) throws IOException, GitAPIException, InterruptedException {
        // 保存git提交差异
        StringBuilder diffCode = new StringBuilder();
        // 设置远程仓库的本地工作地址
        String workUrl = workSpacePrefix + UUID.randomUUID().toString();

        // 1. 设置密钥位置，防止克隆远程仓库失败
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                // 可以在这里进行额外的配置
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch defaultJSch = super.createDefaultJSch(fs);
                // 添加SSH密钥
                defaultJSch.addIdentity(sshKeyUrl);
                return defaultJSch;
            }
        };

        // 2. 克隆远程的git仓库
        Git git = null;
        try {
            git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(new File(workUrl))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("root", "hhxxttxs"))
                    .call();
        } catch (Exception e) {
            log.error("clone remote git repo failed: ", e);
            throw new RuntimeException("clone remote git repo failed");
        }

        // 3. 使用 ProcessBuilder 操作进程（便于进行git操作）
        // 获取最新一次提交与过去1次提交之间的文件差异（多少个 ^ 就是多少次之前的提交）
        String commitHash = "";
        if (null != git) {
            commitHash = git.log().setMaxCount(1).call().iterator().next().getName();
        }
        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", commitHash + "^", commitHash);
        diffProcessBuilder.directory(new File(workUrl)); // 设置工作目录
        Process diffProcess = diffProcessBuilder.start(); // 启动 git diff 进程

        // 4. 读取 git diff 命令的输出，按行获取文件差异
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
        String line;
        while ((line = diffReader.readLine()) != null) {
            diffCode.append(line).append("\n");
        }
        diffReader.close();

        // 5. 获取 git diff 命令的返回码，0表示执行成功
        int exitCode = diffProcess.waitFor();
        if (exitCode != 0) {
            // 返回码不为0，抛出异常
            throw new RuntimeException("Failed to get git diff, exit code is: " + exitCode);
        }


        // 6. 删除目录
        Path repo = Paths.get(workUrl);
        try {
            if (Files.exists(repo)) {
                Files.walkFileTree(repo, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (exc == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        } else {
                            throw exc;
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.error("删除文件夹异常，文件夹路径为：{}，异常信息为", workUrl, e);
        }


        // 5. 返回最近两次提交文件的差异
        return diffCode.toString();
    }

    /**
     * 获取指定远程仓库某次git提交与前一次提交之间的差异
     *
     * @param repoUrl    远程仓库地址
     * @param commitHash 某一次git提交的hash
     * @return git提交差异
     * @throws IOException
     * @throws InterruptedException
     */
    public String diff(String repoUrl, String commitHash) throws IOException, GitAPIException, InterruptedException {
        // 保存git提交差异
        StringBuilder diffCode = new StringBuilder();
        // 设置远程仓库的本地工作地址
        String workUrl = workSpacePrefix + UUID.randomUUID().toString();

        // 1. 设置密钥位置，防止克隆远程仓库失败
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                // 可以在这里进行额外的配置
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch defaultJSch = super.createDefaultJSch(fs);
                // 添加SSH密钥
                defaultJSch.addIdentity(sshKeyUrl);
                return defaultJSch;
            }
        };

        // 2. 克隆远程的git仓库
        try {
            Git git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(new File(workUrl))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("root", "hhxxttxs"))
                    .call();
        } catch (Exception e) {
            log.error("clone remote git repo failed: ", e);
        }

        // 3. 使用 ProcessBuilder 操作进程（便于进行git操作）
        // 获取最新一次提交与过去1次提交之间的文件差异（多少个 ^ 就是多少次之前的提交）
        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", commitHash + "^", commitHash);
        diffProcessBuilder.directory(new File(workUrl)); // 设置工作目录
        Process diffProcess = diffProcessBuilder.start(); // 启动 git diff 进程

        // 4. 读取 git diff 命令的输出，按行获取文件差异
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
        String line;
        while ((line = diffReader.readLine()) != null) {
            diffCode.append(line).append("\n");
        }
        diffReader.close();

        // 5. 获取 git diff 命令的返回码，0表示执行成功
        int exitCode = diffProcess.waitFor();
        if (exitCode != 0) {
            // 返回码不为0，抛出异常
            throw new RuntimeException("Failed to get git diff, exit code is: " + exitCode);
        }


        // 6. 删除目录
        Path repo = Paths.get(workUrl);
        try {
            if (Files.exists(repo)) {
                Files.walkFileTree(repo, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (exc == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        } else {
                            throw exc;
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.error("删除文件夹异常，文件夹路径为：{}，异常信息为", workUrl, e);
        }


        // 5. 返回最近两次提交文件的差异
        return diffCode.toString();
    }

    /**
     * git拉取远程仓库，将AI生成的代码评审结果推送到该仓库。并返回日志文件路径
     *
     * @param comment AI生成的代码评审结果
     * @return
     * @throws Exception
     */
    public String commitAndPush(String repoUrl, String comment) throws Exception {

        // 设置远程仓库的本地工作地址
        String workUrl = workSpacePrefix + UUID.randomUUID().toString();

        // 1. 设置密钥位置，防止克隆远程仓库失败
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                // 可以在这里进行额外的配置
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch defaultJSch = super.createDefaultJSch(fs);
                // 添加SSH密钥
                defaultJSch.addIdentity(sshKeyUrl);
                return defaultJSch;
            }
        };

        // 2. 克隆远程的git仓库
        Git git = null;
        try {
            git = Git.cloneRepository()
                    .setURI(repoUrl + ".git")
                    .setDirectory(new File(workUrl))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("root", "hhxxttxs"))
                    .call();
        } catch (Exception e) {
            log.error("clone remote git repo failed: ", e);
            throw new RuntimeException("clone remote git repo failed: ", e);
        }

        // 2. 创建文件夹，用于存放本次AI生成的代码评审结果
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File(workUrl + File.separator + dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }

        // 3. 生成文件，将AI生成的代码评审结果写入该文件中
        String fileName = String.format("%s.md", System.currentTimeMillis());
        File newFile = new File(dateFolder, fileName);
        // 使用FileWriter将代码评审结果写入文件中
        try (FileWriter fileWriter = new FileWriter(newFile)) {
            fileWriter.write(comment);
        }

        // 4. 在日志仓库上执行git操作：add + commit + push
        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("add new code review file " + fileName).call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider("root", "hhxxttxs")).call();
        log.info("openai-code-review git commit and push done! {}", fileName);

        // 5. 返回日志文件地址
        return repoUrl + File.separator + dateFolderName + File.separator + fileName;
    }

}
