package org.hapifhir.formtofhirservice.route;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.hapifhir.formtofhirservice.config.AppProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.security.Security;

public class EmailRouteTest extends CamelTestSupport {

    AppProperties appProperties = new AppProperties();

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new EmailRoute(appProperties);
    }


    private GreenMail mailServer;
    private static final String USER_PASSWORD = "hapifhir";
    private static final String USER_NAME = "hapifhir";
    private static final String EMAIL_USER_ADDRESS = "hapifhir@localhost";
    private static final String EMAIL_TO = "someone@localhost.com";
    private static final String EMAIL_SUBJECT = "Test E-Mail";
    private static final String EMAIL_TEXT = "This is a test e-mail.";

    @BeforeEach
    void beforeEach() {
        Security.setProperty("ssl.SocketFactory.provider", DummySSLSocketFactory.class.getName());
        mailServer = new GreenMail(ServerSetupTest.IMAPS);
        mailServer.start();
    }

    @AfterEach
    void afterEach() {
        mailServer.stop();
    }

    @EndpointInject("mock:endpoint")
    protected MockEndpoint mockEndpoint;

    @Test
    public void testRoute() throws Exception {

        String emailEndpoint = "imaps://localhost:" + ServerSetupTest.IMAPS.getPort()
                + "?username=" + USER_NAME + "&password=" + USER_PASSWORD
                + "&delete=true&closeFolder=false&searchTerm.unseen=true";

        AdviceWithRouteBuilder.adviceWith(context, "email", a -> {
            a.replaceFromWith(emailEndpoint);
            a.interceptSendToEndpoint("direct:log").to(mockEndpoint);
        });

        mockEndpoint.expectedMessageCount(1);

        GreenMailUser user = mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD);
        user.deliver(createMessage());
        assertMockEndpointsSatisfied();

    }

    private MimeMessage createMessage() throws Exception {
        MimeMessage message = new MimeMessage((Session) null);
        message.setFrom(new InternetAddress(EMAIL_TO));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(EMAIL_USER_ADDRESS));
        message.setSubject(EMAIL_SUBJECT);
        message.setText(EMAIL_TEXT);
        message.setFlag(Flags.Flag.SEEN, false);
        message.setContent(getMultipartAttachment("src/test/resources/pui-form.pdf"));
        return message;
    }

    private Multipart getMultipartAttachment(String path) throws Exception {
        Multipart multipart = new MimeMultipart();
        MimeBodyPart attachPart = new MimeBodyPart();
        String attachFile = path;
        attachPart.attachFile(attachFile);
        multipart.addBodyPart(attachPart);
        return multipart;
    }

}
