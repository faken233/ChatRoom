package org.example.onmessage.config.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.example.onmessage.constants.RabbitMQConstant;
import org.example.utils.NetworkUtils;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

/**
 * @author yinjunbiao
 * @version 1.0
 * @date 2024/1/27
 */
@Slf4j
@Configuration
public class RabbitMQConfig implements ApplicationContextAware {

    private String queueName;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() throws InterruptedException {
        String port = environment.getProperty("server.port", "8080");
        // TODO 自己电脑网卡环境有点复杂, 仅为测试, 生产环境获取IP地址等操作由实际情况决定
        String ip = NetworkUtils.getIpAddress();
        queueName = ip + ":" + port;
    }
    @Bean
    public String queueName() {
        return queueName;
    }

    @Bean
    public DirectExchange errorMessageExchange(){
        return new DirectExchange("error.order.direct");
    }
    @Bean
    public Queue errorQueue(){
        return new Queue("error.order.queue", true);
    }
    @Bean
    public Binding errorBinding(Queue errorQueue, DirectExchange errorMessageExchange){
        return BindingBuilder.bind(errorQueue).to(errorMessageExchange).with("error");
    }

    @Bean
    public MessageRecoverer republishMessageRecoverer(RabbitTemplate rabbitTemplate){
        return new RepublishMessageRecoverer(rabbitTemplate, "error.order.direct", "error");
    }

    @Bean(RabbitMQConstant.WS_EXCHANGE)
    public Exchange exchange(){
        return ExchangeBuilder.topicExchange(RabbitMQConstant.WS_EXCHANGE).durable(true).build();
    }

    @Bean(RabbitMQConstant.MQ_GROUP_EXCHANGE)
    public Exchange groupExchange(){
        return ExchangeBuilder.topicExchange(RabbitMQConstant.MQ_GROUP_EXCHANGE).durable(true).build();
    }

    @Bean(RabbitMQConstant.MQ_ACK_EXCHANGE)
    public Exchange ackExchange(){
        return ExchangeBuilder.topicExchange(RabbitMQConstant.MQ_ACK_EXCHANGE).durable(true).build();
    }

    @Bean(RabbitMQConstant.TEST_EXCHANGE)
    public Exchange testExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstant.TEST_EXCHANGE).durable(true).build();
    }


    @Bean(RabbitMQConstant.IP_QUEUE)
    public Queue queue(){
        return QueueBuilder.durable(queueName).build();
    }

    @Bean(RabbitMQConstant.MQ_GROUP_QUEUE)
    public Queue groupQueue(){
        return QueueBuilder.durable(RabbitMQConstant.MQ_GROUP_QUEUE + "." +queueName).build();
    }

    @Bean(RabbitMQConstant.MQ_ACK_QUEUE)
    public Queue ackQueue(){
        return QueueBuilder.durable(RabbitMQConstant.MQ_ACK_QUEUE + "." +queueName).build();
    }

    @Bean(RabbitMQConstant.TEST_QUEUE)
    public Queue testQueue(){
        return QueueBuilder.durable(RabbitMQConstant.TEST_QUEUE + "." +queueName).build();
    }




    @Bean
    public Binding bindingSingle(@Qualifier(RabbitMQConstant.IP_QUEUE) Queue queue,  @Qualifier(RabbitMQConstant.WS_EXCHANGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(queueName).noargs();
    }

    @Bean
    public Binding bindingGroup(@Qualifier(RabbitMQConstant.MQ_GROUP_QUEUE) Queue queue, @Qualifier(RabbitMQConstant.MQ_GROUP_EXCHANGE) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(queueName).noargs();
    }

    @Bean
    public Binding bindingAck(@Qualifier(RabbitMQConstant.MQ_ACK_QUEUE) Queue queue, @Qualifier(RabbitMQConstant.MQ_ACK_EXCHANGE) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(queueName).noargs();
    }

    @Bean
    public Binding bindingTest(@Qualifier(RabbitMQConstant.TEST_QUEUE) Queue queue, @Qualifier(RabbitMQConstant.TEST_EXCHANGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(queueName).noargs();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取RabbitTemplate
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        // 设置ReturnCallback
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            // 交换机投递到队列失败，记录日志
            log.error("消息路由到队列失败，应答码{}，原因{}，交换机{}，路由键{},消息{}",
                    replyCode, replyText, exchange, routingKey, message);
            // 重发消息
            if (replyCode == 312){
                log.info("消息发送失败，失败code为312，重新发送消息");
                CompletableFuture.runAsync(() -> rabbitTemplate.convertAndSend(exchange, routingKey, message));
            }else {
                rabbitTemplate.convertAndSend(exchange, routingKey, message);
            }
        });
//        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
//            if (ack) {
//                log.info("消息发送成功:correlationData({}),ack({}),cause({})", correlationData, ack, cause);
//            } else {
//                log.error("消息发送失败:correlationData({}),ack({}),cause({})", correlationData, ack, cause);
//            }
//        });
    }


}
