# QPay Spring Boot Starter

[![CI](https://github.com/qpay-sdk/qpay-spring-boot-starter/actions/workflows/ci.yml/badge.svg)](https://github.com/qpay-sdk/qpay-spring-boot-starter/actions)

QPay V2 payment integration for Spring Boot.

## Install

Maven:
```xml
<dependency>
    <groupId>mn.qpay</groupId>
    <artifactId>qpay-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Configuration

```properties
qpay.base-url=https://merchant.qpay.mn
qpay.username=your_username
qpay.password=your_password
qpay.invoice-code=your_invoice_code
qpay.callback-url=https://yoursite.com/qpay/webhook
```

## Usage

```java
@Autowired
private QPayTemplate qpay;

InvoiceResponse invoice = qpay.createSimpleInvoice("ORDER-001", 10000);
// invoice.getQrImage(), invoice.getUrls()
```

## Webhook Events

```java
@EventListener
public void onPayment(QPayWebhookEvent event) {
    String invoiceId = event.getInvoiceId();
    // handle payment
}
```

## Thymeleaf

```html
<div th:replace="~{fragments/qpay :: qr-code(${invoice.qrImage}, 256)}"></div>
<div th:replace="~{fragments/qpay :: payment-buttons(${invoice.urls})}"></div>
```

## License

MIT
