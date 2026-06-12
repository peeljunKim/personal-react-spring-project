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
     * 여기서 선언한 리소스 목록
     * <p>
     * - Exchange </br>
     * - Queue </br>
     * - 실패 메시지 처리를 위한 Dead Letter Exchange </br>
     * - 실패 메시지 저장용 Dead Letter Queue </br>
     * - Exchange와 Queue를 연결하는 Binding </br>
     *
     * @return 쿠폰 발급에 필요한 RabbitMQ 리소스를 선언
     */
    @Bean
    public Declarables couponIssueRabbitDeclarables() {
        /*
        Exchange는 routing key가 정확히 일치하는 Queue로만 이동
        durable(true)는 RabbitMQ 서버가 재시작되어도 Exchange가 유지되도록 설정하는 옵션
         */
        DirectExchange issueExchange = ExchangeBuilder.directExchange(couponIssueProperties.getExchange())
                .durable(true)
                .build();

        /*
        실패한 메시지를 전달받을 Dead Letter Exchange 관련 설정

        Consumer 처리 중 예외가 발생하거나 메시지가 reject/nack 처리되는 경우
        설정된 DLX 정책에 따라 이 Exchange로 메시지가 이동
         */
        DirectExchange deadLetterExchange = ExchangeBuilder.directExchange(couponIssueProperties.getDeadLetterExchange())
                .durable(true)
                .build();

        /*
        메시지를 소비할 Queue를 생성 및 실패 시 설정

        x-dead-letter-exchange: Queue에서 처리에 실패한 메시지를 보낼 Dead Letter Exchange를 지정
        x-dead-letter-routing-key: 실패 메시지가 Dead Letter Exchange로 이동할 때 사용할 routing key를 지정
         */
        Queue issueQueue = QueueBuilder.durable(couponIssueProperties.getQueue())
                // Consumer 실패 메시지는 DLX로 이동시키되, DLQ 적재만으로 비즈니스 실패를 확정하지 않습니다.
                .withArgument("x-dead-letter-exchange", couponIssueProperties.getDeadLetterExchange())
                .withArgument("x-dead-letter-routing-key", couponIssueProperties.getDeadLetterRoutingKey())
                .build();

        /*
        처리 실패한 메시지를 저장할 Dead Letter Queue를 생성
         */
        Queue deadLetterQueue = QueueBuilder.durable(couponIssueProperties.getDeadLetterQueue())
                .build();

        /*

        issueExchange와 issueQueue를 routing key로 연결

        Publisher가 issueExchange로 메시지를 발행할 때 routingKey가 couponIssueProperties.getRoutingKey()와 일치하면
        해당 메시지는 issueQueue로 전달
         */
        Binding issueBinding = BindingBuilder.bind(issueQueue)
                .to(issueExchange)
                .with(couponIssueProperties.getRoutingKey());

        /*
        Dead Letter Exchange와 Dead Letter Queue를 routing key로 연결 (issueBinding이랑 비슷)

        issueQueue에서 실패한 메시지가 DLX로 이동할 때 deadLetterRoutingKey가 일치하면 deadLetterQueue에 저장
         */
        Binding deadLetterBinding = BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(couponIssueProperties.getDeadLetterRoutingKey());

        /*
        Spring RabbitMQ 브로커에 해당 리소스들을 자동 전달
         */
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
