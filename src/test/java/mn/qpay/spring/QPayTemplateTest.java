package mn.qpay.spring;

import mn.qpay.sdk.QPayClient;
import mn.qpay.sdk.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QPayTemplateTest {

    @Mock
    private QPayClient mockClient;

    private QPayProperties properties;
    private QPayTemplate template;

    @BeforeEach
    void setUp() {
        properties = new QPayProperties();
        properties.setBaseUrl("https://merchant.qpay.mn");
        properties.setUsername("testuser");
        properties.setPassword("testpass");
        properties.setInvoiceCode("TEST_INVOICE");
        properties.setCallbackUrl("https://example.com/callback");
        template = new QPayTemplate(mockClient, properties);
    }

    @Test
    void createSimpleInvoiceSuccess() {
        InvoiceResponse expectedResponse = new InvoiceResponse();
        expectedResponse.setInvoiceId("inv_123");
        expectedResponse.setQrText("qr_data_here");

        when(mockClient.createSimpleInvoice(any(CreateSimpleInvoiceRequest.class)))
                .thenReturn(expectedResponse);

        InvoiceResponse result = template.createSimpleInvoice("ORDER-001", 50000.0);

        assertThat(result).isNotNull();
        assertThat(result.getInvoiceId()).isEqualTo("inv_123");
        assertThat(result.getQrText()).isEqualTo("qr_data_here");

        ArgumentCaptor<CreateSimpleInvoiceRequest> captor = ArgumentCaptor.forClass(CreateSimpleInvoiceRequest.class);
        verify(mockClient).createSimpleInvoice(captor.capture());

        CreateSimpleInvoiceRequest captured = captor.getValue();
        assertThat(captured.getInvoiceCode()).isEqualTo("TEST_INVOICE");
        assertThat(captured.getSenderInvoiceNo()).isEqualTo("ORDER-001");
        assertThat(captured.getAmount()).isEqualTo(50000.0);
    }

    @Test
    void createSimpleInvoiceThrowsOnClientError() {
        when(mockClient.createSimpleInvoice(any(CreateSimpleInvoiceRequest.class)))
                .thenThrow(new RuntimeException("API error: Unauthorized"));

        assertThatThrownBy(() -> template.createSimpleInvoice("ORDER-002", 10000.0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    void checkPaymentSuccess() {
        PaymentCheckResponse expectedResponse = new PaymentCheckResponse();
        PaymentCheckResponse.PaymentCheckRow row = new PaymentCheckResponse.PaymentCheckRow();
        row.setPaymentId("pay_456");
        row.setPaymentStatus("PAID");
        expectedResponse.setRows(List.of(row));

        when(mockClient.checkPayment(any(PaymentCheckRequest.class)))
                .thenReturn(expectedResponse);

        PaymentCheckResponse result = template.checkPayment("inv_123");

        assertThat(result).isNotNull();
        assertThat(result.getRows()).hasSize(1);
        assertThat(result.getRows().get(0).getPaymentId()).isEqualTo("pay_456");

        ArgumentCaptor<PaymentCheckRequest> captor = ArgumentCaptor.forClass(PaymentCheckRequest.class);
        verify(mockClient).checkPayment(captor.capture());

        PaymentCheckRequest captured = captor.getValue();
        assertThat(captured.getObjectType()).isEqualTo("INVOICE");
        assertThat(captured.getObjectId()).isEqualTo("inv_123");
    }

    @Test
    void checkPaymentReturnsEmptyForUnpaid() {
        PaymentCheckResponse expectedResponse = new PaymentCheckResponse();
        expectedResponse.setRows(List.of());

        when(mockClient.checkPayment(any(PaymentCheckRequest.class)))
                .thenReturn(expectedResponse);

        PaymentCheckResponse result = template.checkPayment("inv_unpaid");

        assertThat(result).isNotNull();
        assertThat(result.getRows()).isEmpty();
    }

    @Test
    void checkPaymentThrowsOnClientError() {
        when(mockClient.checkPayment(any(PaymentCheckRequest.class)))
                .thenThrow(new RuntimeException("Network error"));

        assertThatThrownBy(() -> template.checkPayment("inv_999"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Network error");
    }

    @Test
    void getClientReturnsInjectedClient() {
        assertThat(template.getClient()).isSameAs(mockClient);
    }

    @Test
    void createSimpleInvoiceUsesPropertiesDefaults() {
        properties.setInvoiceCode("CUSTOM_CODE");
        properties.setCallbackUrl("https://custom.com/cb");
        QPayTemplate customTemplate = new QPayTemplate(mockClient, properties);

        InvoiceResponse response = new InvoiceResponse();
        when(mockClient.createSimpleInvoice(any())).thenReturn(response);

        customTemplate.createSimpleInvoice("ORD-100", 25000.0);

        ArgumentCaptor<CreateSimpleInvoiceRequest> captor = ArgumentCaptor.forClass(CreateSimpleInvoiceRequest.class);
        verify(mockClient).createSimpleInvoice(captor.capture());
        assertThat(captor.getValue().getInvoiceCode()).isEqualTo("CUSTOM_CODE");
    }
}
