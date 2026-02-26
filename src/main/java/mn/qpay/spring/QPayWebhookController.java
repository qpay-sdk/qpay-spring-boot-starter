package mn.qpay.spring;

import mn.qpay.sdk.models.PaymentCheckResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class QPayWebhookController {

    private final QPayTemplate qPayTemplate;
    private final ApplicationEventPublisher publisher;

    public QPayWebhookController(QPayTemplate qPayTemplate, ApplicationEventPublisher publisher) {
        this.qPayTemplate = qPayTemplate;
        this.publisher = publisher;
    }

    @PostMapping("${qpay.webhook-path:/qpay/webhook}")
    public ResponseEntity<Map<String, String>> handleWebhook(@RequestBody Map<String, Object> body) {
        String invoiceId = (String) body.get("invoice_id");
        if (invoiceId == null || invoiceId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing invoice_id"));
        }

        try {
            PaymentCheckResponse result = qPayTemplate.checkPayment(invoiceId);
            if (result.getRows() != null && !result.getRows().isEmpty()) {
                publisher.publishEvent(new QPayWebhookEvent(this, invoiceId, result));
                return ResponseEntity.ok(Map.of("status", "paid"));
            }
            return ResponseEntity.ok(Map.of("status", "unpaid"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
