package com.dchealth.facade.security;

import com.dchealth.VO.MailProperties;
import com.dchealth.util.SmsSendUtil;
import com.dchealth.util.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Created by Administrator on 2017/8/25.
 */
@Component
public class MailSendFacade {

    private Session session;

    @PostConstruct
    public void init()
    {
        try{
            //从系统参数表获取相关参数
            MailProperties mailProperties = getMailProperties();
            if(checkMailProperties(mailProperties)){
                //初始化session
                initSession(mailProperties);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * 检查邮件参数是否为空
     * @param mailProperties
     * @return
     */
    private boolean checkMailProperties(MailProperties mailProperties ){
        if(mailProperties != null
                && !StringUtils.isEmpty(mailProperties.getLoginPassword())
                && !StringUtils.isEmpty(mailProperties.getUserName())
                && !StringUtils.isEmpty(mailProperties.getMailSmtpHost())
                && !StringUtils.isEmpty(mailProperties.getMailTransportProtocal())){
            return true;
        }
        return false;
    }
    /**设置发送邮件时的各种属性
     * @param mailProperties
     */
    private void initSession(MailProperties mailProperties ){
        if(mailProperties == null){
            return;
        }
        Properties pros=new Properties();
        pros.setProperty("mail.smtp.host", mailProperties.getMailSmtpHost());
        pros.setProperty("mail.transport.protocol", mailProperties.getMailTransportProtocal());
        pros.setProperty("mail.smtp.port", String.valueOf((mailProperties.getMailSmtpPort())));
        pros.setProperty("mail.smtp.auth","true");
        pros.setProperty("mail.debug", "false");
        pros.setProperty("loginUser", mailProperties.getUserName());
        pros.setProperty("loginPassWord", mailProperties.getLoginPassword());
        session= Session.getInstance(pros);//根据属性新建一个邮件会话
        session.setDebug(true);
    }
    /**
     * 获取发送邮件相关参数
     * @return
     */
    private MailProperties getMailProperties(){
        MailProperties mailProperties = new MailProperties();
        try {
            mailProperties.setMailSmtpHost(SmsSendUtil.getStringByKey("MAIL_SMTP_HOST"));
            String mailSmtpPort = SmsSendUtil.getStringByKey("MAIL_SMTP_PORT");
            if(!StringUtils.isEmpty(mailSmtpPort) && isInteger(mailSmtpPort)){
                mailProperties.setMailSmtpPort(Integer.valueOf(mailSmtpPort));
            }
            mailProperties.setMailTransportProtocal(SmsSendUtil.getStringByKey("MAIL_TRANSPORT_PROTOCOL"));
            mailProperties.setUserName(SmsSendUtil.getStringByKey("LOGIN_USER"));
            mailProperties.setLoginPassword(SmsSendUtil.getStringByKey("PASSWORD"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return mailProperties;
    }

    /**
     * 发送邮件
     * @param mailTo 收件人
     * @param info 发送内容
     * @throws Exception
     */
    public void sendMail(String mailTo,String info) throws Exception{
        String subject = "用户注册";
        if(null == session){
            MailProperties mailProperties=getMailProperties();
            initSession(mailProperties);
        }
        Transport transport = null;
        try{
            MimeMessage message=new MimeMessage(session);//由邮件会话新建一个消息对象
            String user=session.getProperties().getProperty("loginUser");
            String password=session.getProperties().getProperty("loginPassWord");
            InternetAddress fromAddress=new InternetAddress(user);
            message.setFrom(fromAddress);
            InternetAddress to=new InternetAddress(mailTo);
            message.setRecipient(Message.RecipientType.TO, to);
            message.setSubject(getTime()+subject);
            message.setContent(info,"text/html;charset=gbk");
            message.setSentDate(new Date());
            message.saveChanges();//存储邮件信息
            transport=session.getTransport();
            transport.connect(user,password);
            transport.sendMessage(message,message.getAllRecipients());
        }catch(Exception e){
            throw new Exception("发送邮件失败");
        }finally{
            if(transport != null){
                try {
                    transport.close();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 验证是否是数字
     * @param str
     * @return
     */
    public static boolean isInteger(String str)
    {
        try
        {
            Integer.parseInt(str);
            return true;
        }
        catch(NumberFormatException ex)
        {
            return false;
        }
    }

    /**
     * 获取系统时间
     * @return
     */
    private String getTime() {
        Calendar now = Calendar.getInstance();
        int year=now.get(Calendar.YEAR);
        int month=now.get(Calendar.MONTH);
        int day=now.get(Calendar.DAY_OF_MONTH);
        int  hour=now.get(Calendar.HOUR_OF_DAY);
        int minute=now.get(Calendar.MINUTE);
        int second=now.get(Calendar.SECOND);
        return year + "-" + (month + 1) + "-"+day+"  "+hour+":"+minute+":"+second+" ";
    }
}
