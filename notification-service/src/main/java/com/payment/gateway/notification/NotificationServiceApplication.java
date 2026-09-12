package com.payment.gateway.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.notification.application.impl.WebhookServiceImpl;
import com.payment.gateway.notification.application.service.WebhookService;
import com.payment.gateway.notification.domain.repository.WebhookDeliveryRepository;
import com.payment.gateway.notification.dto.request.WebhookPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootApplication
public class NotificationServiceApplication {
//    @Autowired
//    public static WebhookServiceImpl webhookService;
//    @Autowired
//    public static WebhookDeliveryRepository webhookDeliveryRepository;
    public static void main(String[] args) {

        SpringApplication.run(NotificationServiceApplication.class, args);

//        WebhookPayload payload = WebhookPayload.builder()
//                .transactionId("55cc655f-cc7f-4877-b9f9-5cd4a91f2b30")
//                .merchantId("6d0043fe-09f3-48d9-b99a-99e91702c7bc")
//                .status("SUCCESS")
//                .amount(BigDecimal.valueOf(1514.14))
//                .currency("INR")
//                .processorRef("STUB-76E9282A")
//                .timestamp(LocalDateTime.now())
//                .build();
//        if (webhookService == null)
//            webhookService = new WebhookServiceImpl(webhookDeliveryRepository,new RestTemplate(), new ObjectMapper());
//        webhookService.deliver("6d0043fe-09f3-48d9-b99a-99e91702c7bc","http://localhosthost:9090/webhook",payload);

        /*
        * Webhook payload JSON: {"transactionId":"55cc655f-cc7f-4877-b9f9-5cd4a91f2b30","merchantId":"6d0043fe-09f3-48d9-b99a-99e91702c7bc","status":"SUCCESS","amount":1514.1400,"currency":"INR","processorRef":"STUB-76E9282A","failureReason":null,"timestamp":[2026,6,13,1,55,22,833269641]}
        *
        * */
    }
}
