package mn.qpay.spring;

import mn.qpay.sdk.models.PaymentCheckResponse;
import org.springframework.context.ApplicationEvent;

public class QPayWebhookEvent extends ApplicationEvent {

    private final String invoiceId;
    private final PaymentCheckResponse result;

    public QPayWebhookEvent(Object source, String invoiceId, PaymentCheckResponse result) {
        super(source);
        this.invoiceId = invoiceId;
        this.result = result;
    }

    public String getInvoiceId() { return invoiceId; }
    public PaymentCheckResponse getResult() { return result; }
}
