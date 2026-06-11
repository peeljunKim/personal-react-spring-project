package org.personal.project.config;

import lombok.RequiredArgsConstructor;
import org.personal.project.properties.CouponIssueRabbitMqProperties;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 메시지 발행용 RabbitTemplate과 메시지 소비용 ListenerContainerFactory를 설정
 */
@Configuration
@RequiredArgsConstructor
public class RabbitMqConfig {

    private final CouponIssueRabbitMqProperties couponIssueProperties;

    /**
     * RabbitMQ 메시지 변환기를 등록
     * <p>
     * Spring AMQP 4.0부터 Jackson2JsonMessageConverter이 제거 예정
     * 이유는 JacksonJsonMessageConverter이 Jackson 3 기반으로 변경
     * <p>
     * 현재 spring-amqp 3.2.6
     * <p>
     * <a href="https://docs.spring.io/spring-amqp/api/org/springframework/amqp/support/converter/Jackson2JsonMessageConverter.html">공식주소</a>
     *
     * @return 예를 들어 DTO를 RabbitMQ로 보낼 때 직접 JSON 문자열로 변환하지 않아도
     * RabbitTemplate이 자동으로 JSON 변환을 수행
     *
     */
    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     *
     * RabbitMQ로 메시지를 발행할 때 사용하는 RabbitTemplate을 설정
     * <p>
     * yml파일에 설정값을 자동으로 ConnectionFactory에 주입됨 (Bean 상태)
     *
     * @param connectionFactory      RabbitMQ 브로커와의 연결을 생성하고 관리
     * @param rabbitMessageConverter RabbitMQ 메시지 송수신 시 Java 객체와 JSON 메시지 간 변환
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter rabbitMessageConverter) {

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        rabbitTemplate.setMessageConverter(rabbitMessageConverter);
        rabbitTemplate.setMandatory(true);  // mandatory=true이면 라우팅 실패 메시지를 publisher return callback에서 감지할 수 있음

        return rabbitTemplate;
    }

    /**
     *
     *
     * @param connectionFactory
     * @param rabbitMessageConverter
     * @return @RabbitListener를 실행할 때 필요한 Listener Container를 만들어주는 설정 객체
     * @RabbitListener(containerFactory = "couponIssueRabbitListenerContainerFactory") 형태로 지정해서 사용 가능
     */
    @Bean
    public SimpleRabbitListenerContainerFactory couponIssueRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);

        /*
          수동 ACK 모드 설정

          이유: 쿠폰 발급은 DB 커밋 이후에 ack해야 하므로 수동 ack를 사용

          예를 들어, 메시지를 받았는데 모종의 이유로 DB 저장 중 실패하면
          이미 ACK된 메시지는 RabbitMQ 입장에서 정상 처리된 것으로 보기 때문에
          다시 처리하기 어려움

          중요
          그래서 DB 커밋이 성공한 뒤 직접 basicAck()를 호출하고
          실패 시 basicNack() 또는 basicReject()로 처리하기 위해 MANUAL 모드를 사용
         */
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(couponIssueProperties.getPrefetch()); // prefetch 개수 설정
        // listener에서 예외 발생 시 무한 재큐잉하지 않고 retry/DLQ 정책으로 넘기기 위해 false 설정
        factory.setDefaultRequeueRejected(false);

        return factory;
    }

    /**
     *
     * @return 쿠폰 발급에 필요한 RabbitMQ 리소스를 한 번에 선언
     */
    @Bean
    public Declarables couponIssueRabbitDeclarables() {
        DirectExchange issueExchange = ExchangeBuilder.directExchange(couponIssueProperties.getExchange())
                .durable(true)
                .build();

        DirectExchange deadLetterExchange = ExchangeBuilder.directExchange(couponIssueProperties.getDeadLetterExchange())
                .durable(true)
                .build();

        Queue issueQueue = QueueBuilder.durable(couponIssueProperties.getQueue())
                // Consumer 실패 메시지는 DLX로 이동시키되, DLQ 적재만으로 비즈니스 실패를 확정하지 않습니다.
                .withArgument("x-dead-letter-exchange", couponIssueProperties.getDeadLetterExchange())
                .withArgument("x-dead-letter-routing-key", couponIssueProperties.getDeadLetterRoutingKey())
                .build();

        Queue deadLetterQueue = QueueBuilder.durable(couponIssueProperties.getDeadLetterQueue())
                .build();

        Binding issueBinding = BindingBuilder.bind(issueQueue)
                .to(issueExchange)
                .with(couponIssueProperties.getRoutingKey());

        Binding deadLetterBinding = BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(couponIssueProperties.getDeadLetterRoutingKey());

        return new Declarables(
                issueExchange,
                deadLetterExchange,
                issueQueue,
                deadLetterQueue,
                issueBinding,
                deadLetterBinding
        );
    }
}
