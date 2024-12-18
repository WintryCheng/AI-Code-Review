package review.infrastructure.mail;

import mockit.Deencapsulation;
import mockit.Tested;
import org.junit.Before;
import org.junit.Test;

public class MailServiceTest {
    @Tested
    private MailService mailService;

    @Before
    public void init() {
        mailService = new MailService();
        Deencapsulation.setField(mailService, "authorizationCode", "gbaqmbwbhwneddjg");
    }

    @Test
    public void testSendMail() {
        mailService.sendMail("测试邮件，测试java代码能够成功发送邮件", "3154905097@qq.com", "3154905097@qq.com,2324923349@qq.com");
    }
}