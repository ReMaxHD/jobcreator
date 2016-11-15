/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/main/java/com/swisslog/jenkins/jobcreator/polarion/mail/MailServiceImpl.java $
 * 
 * -----------------------------------------------------------------------------
 * Copyright
 * This software module including the design and software principals used
 * is and remains the property of Swisslog and is submitted with the
 * understanding that it is not to be reproduced nor copied in whole or in
 * part, nor licensed or otherwise provided or communicated to any third
 * party without Swisslog's prior written consent.
 * It must not be used in any way detrimental to the interests of Swisslog.
 * Acceptance of this module will be construed as an agreement to the above.
 *
 * All rights of Swisslog remain reserved. Swisslog and WarehouseManager
 * are trademarks or registered trademarks of Swisslog. Other products
 * and company names mentioned herein may be trademarks or trade names of
 * their respective owners. Specifications are subject to change without
 * notice.
 * -----------------------------------------------------------------------------
 */
package com.swisslog.jenkins.jobcreator.polarion.mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

public class MailServiceImpl implements MailService {

    private static final Logger LOGGER = Logger.getLogger(MailServiceImpl.class);

    private static final String PORT = "25";
    private static final String STARTTLS = "true";
    private static final String HOST = "172.31.49.80";
    private static final String PROTOCOL = "smtp";
    private static final String FROM = "test@swisslog.com";
    private static final String AUTH = "";
    private static final String USER = "";
    private static final String PASSWORD = "";

    @Override
    public void sendMail(String[] recipients, String subject, String content) {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.port", PORT);
        properties.setProperty("mail.smtp.starttls.enable", STARTTLS);
        properties.setProperty("mail.smtp.host", HOST);
        properties.setProperty("mail.transport.protocol", PROTOCOL);
        properties.setProperty("mail.smtp.from", FROM);
        properties.setProperty("mail.smtp.auth", AUTH);
        properties.setProperty("mail.smtp.user", USER);
        properties.setProperty("mail.smtp.password", PASSWORD);
        Session session = Session.getDefaultInstance(properties);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM));
            for (String recipient : recipients) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            }
            message.setSubject(subject);
            message.setContent(content, "text/html");

            Transport.send(message);
            LOGGER.info("Sent mail to " + recipients + "\n subject: " + subject + "\n content:" + content);
        } catch (MessagingException e) {
            LOGGER.error("Couldn't send mail due to MessagingException", e);
        }
    }
}