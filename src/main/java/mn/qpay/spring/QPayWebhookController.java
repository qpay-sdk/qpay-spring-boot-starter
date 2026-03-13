package mn.qpay.spring;

import mn.qpay.sdk.models.PaymentCheckResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class QPayWebhookController {

    private final QPayTemplate qPayTemplate;
    private final ApplicationEventPublisher publisher;

    public QPayWebhookController(QPayTemplate qPayTemplate, ApplicationEventPublisher publisher) {
        this.qPayTemplate = qPayTemplate;
        this.publisher = publisher;
    }

    @GetMapping(value = "${qpay.webhook-path:/qpay/webhook}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> handleWebhook(
            @RequestParam("qpay_payment_id") String paymentId) {
        try {
            PaymentCheckResponse result = qPayTemplate.checkPayment(paymentId);
            publisher.publishEvent(new QPayWebhookEvent(this, paymentId, result));
        } catch (Exception ignored) {
            // Verification failed but still return SUCCESS per QPay spec
        }
        return ResponseEntity.ok("SUCCESS");
    }
}
