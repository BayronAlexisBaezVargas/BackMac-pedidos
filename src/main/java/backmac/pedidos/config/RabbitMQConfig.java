package backmac.pedidos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange:pedidos360.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.dlx:pedidos360.dlx}")
    private String dlxExchangeName;

    @Value("${app.rabbitmq.queues.pagos:pagos.queue}")
    private String pagosQueueName;

    @Value("${app.rabbitmq.queues.pagos-dlq:pagos.dlq}")
    private String pagosDlqQueueName;

    @Value("${app.rabbitmq.queues.pedidos-estado:pedidos.estado.queue}")
    private String pedidosEstadoQueueName;

    @Value("${app.rabbitmq.queues.pedidos-estado-dlq:pedidos.estado.dlq}")
    private String pedidosEstadoDlqQueueName;

    @Value("${app.rabbitmq.queues.notificaciones:notificaciones.queue}")
    private String notificacionesQueueName;

    @Value("${app.rabbitmq.queues.notificaciones-dlq:notificaciones.dlq}")
    private String notificacionesDlqQueueName;

    @Value("${app.rabbitmq.routing-keys.pagos:pagos.routing.key}")
    private String pagosRoutingKey;

    @Value("${app.rabbitmq.routing-keys.pagos-dlq:pagos.dlq.routing.key}")
    private String pagosDlqRoutingKey;

    @Value("${app.rabbitmq.routing-keys.pedidos-estado:pedidos.estado.routing.key}")
    private String pedidosEstadoRoutingKey;

    @Value("${app.rabbitmq.routing-keys.pedidos-estado-dlq:pedidos.estado.dlq.routing.key}")
    private String pedidosEstadoDlqRoutingKey;

    @Value("${app.rabbitmq.routing-keys.notificaciones:notificaciones.routing.key}")
    private String notificacionesRoutingKey;

    @Value("${app.rabbitmq.routing-keys.notificaciones-dlq:notificaciones.dlq.routing.key}")
    private String notificacionesDlqRoutingKey;

    // ==========================================
    // EXCHANGES
    // ==========================================
    @Bean
    public TopicExchange appExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange(dlxExchangeName, true, false);
    }

    // ==========================================
    // QUEUES: PAGOS (ms-pedidos -> ms-pagos)
    // ==========================================
    @Bean
    public Queue pagosQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", pagosDlqRoutingKey);
        return new Queue(pagosQueueName, true, false, false, args);
    }

    @Bean
    public Queue pagosDlqQueue() {
        return new Queue(pagosDlqQueueName, true, false, false);
    }

    @Bean
    public Binding pagosBinding() {
        return BindingBuilder.bind(pagosQueue()).to(appExchange()).with(pagosRoutingKey);
    }

    @Bean
    public Binding pagosDlqBinding() {
        return BindingBuilder.bind(pagosDlqQueue()).to(dlxExchange()).with(pagosDlqRoutingKey);
    }

    // ==========================================
    // QUEUES: ESTADO PEDIDOS (ms-pagos -> ms-pedidos)
    // ==========================================
    @Bean
    public Queue pedidosEstadoQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", pedidosEstadoDlqRoutingKey);
        return new Queue(pedidosEstadoQueueName, true, false, false, args);
    }

    @Bean
    public Queue pedidosEstadoDlqQueue() {
        return new Queue(pedidosEstadoDlqQueueName, true, false, false);
    }

    @Bean
    public Binding pedidosEstadoBinding() {
        return BindingBuilder.bind(pedidosEstadoQueue()).to(appExchange()).with(pedidosEstadoRoutingKey);
    }

    @Bean
    public Binding pedidosEstadoDlqBinding() {
        return BindingBuilder.bind(pedidosEstadoDlqQueue()).to(dlxExchange()).with(pedidosEstadoDlqRoutingKey);
    }

    // ==========================================
    // QUEUES: NOTIFICACIONES (ms-pedidos/ms-pagos -> ms-notificaciones)
    // ==========================================
    @Bean
    public Queue notificacionesQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", notificacionesDlqRoutingKey);
        return new Queue(notificacionesQueueName, true, false, false, args);
    }

    @Bean
    public Queue notificacionesDlqQueue() {
        return new Queue(notificacionesDlqQueueName, true, false, false);
    }

    @Bean
    public Binding notificacionesBinding() {
        return BindingBuilder.bind(notificacionesQueue()).to(appExchange()).with(notificacionesRoutingKey);
    }

    @Bean
    public Binding notificacionesDlqBinding() {
        return BindingBuilder.bind(notificacionesDlqQueue()).to(dlxExchange()).with(notificacionesDlqRoutingKey);
    }

    // ==========================================
    // SERIALIZADOR JSON & TEMPLATE
    // ==========================================
    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter());
        return template;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }
}
