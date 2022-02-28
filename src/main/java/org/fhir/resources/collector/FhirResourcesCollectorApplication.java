package org.fhir.resources.collector;

import org.fhir.resources.collector.service.FhirResourcesCollectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.fhir.resources.collector.model.Constants;
import org.fhir.resources.collector.model.Response;

import com.dsl.wrapper.utilities.AppDSLWrapperProcessor;

@EnableBinding(Processor.class)
@EnableConfigurationProperties(FhirResourcesCollectorProperties.class)
@EnableAsync
@EnableRetry
@SpringBootApplication
public class FhirResourcesCollectorApplication extends AppDSLWrapperProcessor {

	@Autowired
	public FhirResourcesCollectorApplication(FhirResourcesCollectorService fhirResourceCollectorService) {
		this.fhirResourceCollectorService = fhirResourceCollectorService;
	}

	private FhirResourcesCollectorService fhirResourceCollectorService;

	public static void main(String[] args) {
		SpringApplication.run(FhirResourcesCollectorApplication.class, args);
	}

	@Override
	public Message<String> mainTransformer(Message<?> message) {

		String inputJson = (String) message.getPayload();
		Response response = null;
		try {
			response = fhirResourceCollectorService.getEnrichedJson(inputJson);
		}
		/**
		 * if our application throws any exception like null pointer or timeout
		 * exception , send it to exception channel and return null
		 */
		catch (Exception e) {

			errorsGateway.sendToExceptionChannel(Constants.INTERNAL_SERVER_ERROR, e, Constants.TECHNICAL_ERROR_MSG);
			e.printStackTrace();
		}

		/**
		 * if any of remote service gave exception, send response to error channel and
		 * return null. if none of remote services gave exception return response which
		 * will go to output channel.
		 */
		if (response != null) {
			Message<String> finalResponse = null;
			finalResponse = MessageBuilder.withPayload(response.getJsonWithRefrenceData())
					.copyHeaders(message.getHeaders()).build();
			if (!(response.getIsExceptionOccured() == true)) {
				return finalResponse;
			} else {
				errorsGateway.sendToErrorsChannel(finalResponse);
			}
		}

		return null;
	}

}
