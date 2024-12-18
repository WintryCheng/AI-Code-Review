package review.infrastructure.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import review.domain.entity.CommitInfo;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * 发送邮件
 */
@Slf4j
@Component
public class MailService {

    @Value("${config.mail.authorization.code}")
    private String authorizationCode;

    /**
     * 发送邮件到指定联系人
     *
     * @param content  邮件内容
     * @param fromUser 发件用户
     * @param toUsers  收件用户
     */
    public void sendMail(String content, String fromUser, String toUsers) {
        // 设置系统属性
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.qq.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        // 获取默认会话对象
        Session session = Session.getDefaultInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromUser, authorizationCode);
            }
        });

        String[] splitToUsers = toUsers.split(",");
        for (String toUser : splitToUsers) {
            try {
                // 创建默认的 MimeMessage 对象
                Message message = new MimeMessage(session);

                // 设置 FROM 地址
                message.setFrom(new InternetAddress(fromUser));

                // 设置收件人地址 (可以增加多个收件人)
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(toUser));

                // 设置主题
                message.setSubject("AI代码评审结果");

                // 设置消息体
                message.setText(content);

                // 发送消息
                Transport.send(message);

                log.info("邮件发送成功，发送用户为{}，接受用户为{}，邮件内容为{}：", fromUser, toUser, content);

            } catch (Exception e) {
                log.error("邮件发送失败，发送用户为{}，接受用户为{}，邮件内容为{}，报错信息：", fromUser, toUser, content, e);
            }
        }
    }

    /**
     * 发送邮件到指定联系人
     *
     * @param content  邮件内容
     * @param fromUser 发件用户
     * @param toUsers  收件用户
     */
    public void sendMail(String content, String fromUser, String toUsers, CommitInfo commitInfo) {
        // 设置系统属性
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.qq.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        // 获取默认会话对象
        Session session = Session.getDefaultInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromUser, authorizationCode);
            }
        });

        String[] splitToUsers = toUsers.split(",");
        for (String toUser : splitToUsers) {
            try {
                // 创建默认的 MimeMessage 对象
                Message message = new MimeMessage(session);

                // 设置 FROM 地址
                message.setFrom(new InternetAddress(fromUser));

                // 设置收件人地址 (可以增加多个收件人)
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(toUser));

                // 设置主题
                message.setSubject("AI代码评审结果");

                // 设置消息体
                content = String.format("提交信息：%s\n\n%s", commitInfo, content);
                message.setText(content);

                // 发送消息
                Transport.send(message);

                log.info("邮件发送成功，发送用户为{}，接受用户为{}，邮件内容为{}：", fromUser, toUser, content);

            } catch (Exception e) {
                log.error("邮件发送失败，发送用户为{}，接受用户为{}，邮件内容为{}，报错信息：", fromUser, toUser, content, e);
            }
        }
    }
}
