package org.hapifhir.formtofhirservice.route;

import org.apache.camel.builder.RouteBuilder;
import org.hapifhir.formtofhirservice.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailRoute extends RouteBuilder {

    AppProperties appProperties;

    @Autowired
    EmailRoute(AppProperties appProperties){
        this.appProperties = appProperties;
    }

    public void configure() throws Exception {

        // Poll email address for new emails.
        from(appProperties.getEmailEndpoint())
            .id("email")
            .to("direct:log");

        // Placeholder for initial commit, additionally functionality will ultimately replace
        from("direct:log")
            .id("log")
            .to("log:EmailLogData");

        // TODO (1) - Split attachments into individual messages
        // TODO (2) - Write each attachment to S3
        // TODO (3) - For each S3 document asynchronously kickoff Textract, see https://docs.aws.amazon.com/textract/latest/dg/api-async.html
        // TODO (4) - Configure another Consumer endpoint for SNS/SQS (this is where Textract completion notification are posted)
        // TODO (5) - Create Camel Processor for transforming Textract data to FHIR Questionaire/QuestionaireResponse
        // TODO (6) - Use Camel FHIR Component to persist, see https://camel.apache.org/components/latest/fhir-component.html

    }
}
