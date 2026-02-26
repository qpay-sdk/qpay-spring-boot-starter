package mn.qpay.spring;

import mn.qpay.sdk.QPayClient;
import mn.qpay.sdk.models.*;

public class QPayTemplate {

    private final QPayClient client;
    private final QPayProperties properties;

    public QPayTemplate(QPayClient client, QPayProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    public InvoiceResponse createSimpleInvoice(String senderInvoiceNo, double amount) {
        CreateSimpleInvoiceRequest request = new CreateSimpleInvoiceRequest();
        request.setInvoiceCode(properties.getInvoiceCode());
        request.setSenderInvoiceNo(senderInvoiceNo);
        request.setAmount(amount);
        request.setCallbackURL(properties.getCallbackUrl());
        return client.createSimpleInvoice(request);
    }

    public PaymentCheckResponse checkPayment(String invoiceId) {
        PaymentCheckRequest request = new PaymentCheckRequest();
        request.setObjectType("INVOICE");
        request.setObjectId(invoiceId);
        return client.checkPayment(request);
    }

    public QPayClient getClient() {
        return client;
    }
}
