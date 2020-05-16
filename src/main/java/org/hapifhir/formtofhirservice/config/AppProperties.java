package org.hapifhir.formtofhirservice.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {

    @Value("${email.hostname}")
    private String hostname;

    @Value("${email.address}")
    private String address;

    @Value("${email.password}")
    private String password;


    public String getEmailEndpoint() {
        return "imaps://"
                + this.hostname
                + "?username=" + this.address
                + "&password=" + this.password
                + "&delete=false&unseen=true&delay=60000";
    }

}
