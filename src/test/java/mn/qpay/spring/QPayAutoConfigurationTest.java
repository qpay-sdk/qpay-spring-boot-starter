package mn.qpay.spring;

import mn.qpay.sdk.QPayClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class QPayAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(QPayAutoConfiguration.class));

    @Test
    void autoConfigurationCreatesQPayClientBean() {
        contextRunner
                .withPropertyValues(
                        "qpay.base-url=https://merchant.qpay.mn",
                        "qpay.username=testuser",
                        "qpay.password=testpass",
                        "qpay.invoice-code=INV001",
                        "qpay.callback-url=https://example.com/callback"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(QPayClient.class);
                    assertThat(context).hasSingleBean(QPayTemplate.class);
                });
    }

    @Test
    void autoConfigurationCreatesQPayTemplateBean() {
        contextRunner
                .withPropertyValues(
                        "qpay.username=testuser",
                        "qpay.password=testpass",
                        "qpay.invoice-code=INV001",
                        "qpay.callback-url=https://example.com/callback"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(QPayTemplate.class);
                    QPayTemplate template = context.getBean(QPayTemplate.class);
                    assertThat(template).isNotNull();
                    assertThat(template.getClient()).isNotNull();
                });
    }

    @Test
    void autoConfigurationBacksOffWhenCustomClientProvided() {
        contextRunner
                .withUserConfiguration(CustomClientConfiguration.class)
                .withPropertyValues(
                        "qpay.username=testuser",
                        "qpay.password=testpass"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(QPayClient.class);
                    QPayClient client = context.getBean(QPayClient.class);
                    // Should be our mock, not auto-configured one
                    assertThat(client).isNotNull();
                });
    }

    @Test
    void propertiesBindCorrectly() {
        contextRunner
                .withPropertyValues(
                        "qpay.base-url=https://custom.qpay.mn",
                        "qpay.username=myuser",
                        "qpay.password=mypass",
                        "qpay.invoice-code=INV999",
                        "qpay.callback-url=https://mysite.com/callback",
                        "qpay.webhook-path=/custom/webhook"
                )
                .run(context -> {
                    QPayProperties props = context.getBean(QPayProperties.class);
                    assertThat(props.getBaseUrl()).isEqualTo("https://custom.qpay.mn");
                    assertThat(props.getUsername()).isEqualTo("myuser");
                    assertThat(props.getPassword()).isEqualTo("mypass");
                    assertThat(props.getInvoiceCode()).isEqualTo("INV999");
                    assertThat(props.getCallbackUrl()).isEqualTo("https://mysite.com/callback");
                    assertThat(props.getWebhookPath()).isEqualTo("/custom/webhook");
                });
    }

    @Test
    void defaultPropertyValues() {
        contextRunner
                .withPropertyValues(
                        "qpay.username=user",
                        "qpay.password=pass"
                )
                .run(context -> {
                    QPayProperties props = context.getBean(QPayProperties.class);
                    assertThat(props.getBaseUrl()).isEqualTo("https://merchant.qpay.mn");
                    assertThat(props.getWebhookPath()).isEqualTo("/qpay/webhook");
                    assertThat(props.getInvoiceCode()).isNull();
                    assertThat(props.getCallbackUrl()).isNull();
                });
    }

    @Configuration
    static class CustomClientConfiguration {
        @Bean
        QPayClient qPayClient() {
            return mock(QPayClient.class);
        }
    }
}
