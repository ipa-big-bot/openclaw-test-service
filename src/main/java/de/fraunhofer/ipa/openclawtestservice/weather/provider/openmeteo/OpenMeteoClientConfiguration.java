package de.fraunhofer.ipa.openclawtestservice.weather.provider.openmeteo;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(OpenMeteoProperties.class)
class OpenMeteoClientConfiguration {

    @Bean
    @Qualifier("openMeteoGeocodingWebClient")
    WebClient openMeteoGeocodingWebClient(
            WebClient.Builder builder,
            OpenMeteoProperties properties) {
        return build(builder, properties.geocodingBaseUrl().toString(), properties);
    }

    @Bean
    @Qualifier("openMeteoForecastWebClient")
    WebClient openMeteoForecastWebClient(
            WebClient.Builder builder,
            OpenMeteoProperties properties) {
        return build(builder, properties.forecastBaseUrl().toString(), properties);
    }

    private WebClient build(
            WebClient.Builder builder,
            String baseUrl,
            OpenMeteoProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(
                        ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(properties.connectTimeout().toMillis()))
                .responseTimeout(properties.responseTimeout());

        return builder.clone()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
