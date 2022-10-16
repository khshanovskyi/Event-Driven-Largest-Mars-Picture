package ua.khshanovskyi;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;

import feign.codec.Decoder;

@EnableCaching
@EnableFeignClients
@SpringBootApplication
public class EventDrivenLargestMarsPictureApplication {

    @Value("${nasa.exchange}")
    private String nasaExchangeName;
    @Value("${nasa.queue}")
    private String nasaQueueName;

    public static void main(String[] args) {
        SpringApplication.run(EventDrivenLargestMarsPictureApplication.class, args);
    }

    @Bean
    public Decoder feignDecoder() {
        ObjectFactory<HttpMessageConverters> messageConverters = HttpMessageConverters::new;
        return new SpringDecoder(messageConverters);
    }

    @Bean
    public Declarables bindings() {
        var exchange = new DirectExchange(nasaExchangeName);
        var queue = new Queue(nasaQueueName);
        return new Declarables(queue, exchange, BindingBuilder.bind(queue).to(exchange).with(""));
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
