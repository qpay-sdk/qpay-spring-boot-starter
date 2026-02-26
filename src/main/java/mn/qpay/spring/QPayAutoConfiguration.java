package mn.qpay.spring;

import mn.qpay.sdk.QPayClient;
import mn.qpay.sdk.QPayConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(QPayProperties.class)
public class QPayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public QPayClient qPayClient(QPayProperties props) {
        QPayConfig config = QPayConfig.builder()
                .baseUrl(props.getBaseUrl())
                .username(props.getUsername())
                .password(props.getPassword())
                .invoiceCode(props.getInvoiceCode())
                .callbackUrl(props.getCallbackUrl())
                .build();
        return new QPayClient(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public QPayTemplate qPayTemplate(QPayClient client, QPayProperties props) {
        return new QPayTemplate(client, props);
    }
}
