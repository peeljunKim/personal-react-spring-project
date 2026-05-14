package org.personal.project.service.orderarchive;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.personal.project.entity.Member;
import org.personal.project.entity.Order;
import org.personal.project.entity.OrderItem;
import org.personal.project.entity.Product;
import org.personal.project.repository.MemberRepository;
import org.personal.project.repository.OrderItemRepository;
import org.personal.project.repository.OrderRepository;
import org.personal.project.repository.ProductRepository;
import org.personal.project.service.payment.PaymentLockExecutor;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ActiveProfiles("dev")
@SpringBootTest(properties = {
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:order_archive_dev_it;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6380",
        "portone.pay-method=EASY_PAY",
        "order.archive.enabled=false",
        "order.archive.storage-path=build/order-archive-dev-it",
        "order.archive.immediate-retention=3m",
        "order.archive.delayed-retention=5m",
        "order.archive.unknown-retention=5m",
        "order.archive.immediate-pay-methods=EASY_PAY,CARD",
        "order.archive.delayed-pay-methods=VBANK,VIRTUAL_ACCOUNT",
        "order.archive.page-size=50",
        "order.archive.max-pages-per-run=10"
})
class OrderArchiveDevIntegrationTest {

    private static final Path ARCHIVE_PATH = Path.of("build/order-archive-dev-it");

    @Autowired
    private OrderArchiveService orderArchiveService;

    @Autowired
    private OrderArchiveExtractor extractor;

    @Autowired
    private OrderArchivePolicy policy;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private PaymentLockExecutor lockExecutor;

    @MockitoBean
    private RedissonClient redissonClient;

    @BeforeEach
    void setUp() throws IOException {
        when(lockExecutor.execute(anyList(), anyLong(), anyLong(), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(3);
                    return supplier.get();
                });
        deleteRecursively(ARCHIVE_PATH);
    }

    @Test
    @DisplayName("dev 설정으로 이탈 주문을 CSV로 아카이빙하고 메인 DB에서 purge한다")
    void archivesAbandonedOrdersAndPurgesMainTablesInDevProfile() throws Exception {
        Order order = createAbandonedEasyPayOrder();
        Long orderId = order.getOno();
        Long orderItemId = order.getItems().get(0).getOino();

        LocalDateTime now = LocalDateTime.now();
        OrderArchiveBatch preview = extractor.extract(policy.criteria(now, null));
        System.out.println("[ARCHIVE-IT] 5-1 아카이빙 대상 DB 조회 결과: found="
                + !preview.candidates().isEmpty()
                + ", candidateOrderIds="
                + preview.candidates().stream().map(OrderArchiveCandidate::orderId).toList());

        OrderArchiveRunResult result = orderArchiveService.archiveEligibleOrders();
        Path archiveFile = ARCHIVE_PATH.resolve(LocalDate.now() + "_orders.csv").toAbsolutePath();
        List<String> sampleLines = Files.readAllLines(archiveFile, StandardCharsets.UTF_8)
                .stream()
                .limit(5)
                .toList();

        System.out.println("[ARCHIVE-IT] 5-2 생성된 CSV 파일 절대 경로: " + archiveFile);
        System.out.println("[ARCHIVE-IT] 5-3 생성된 CSV 파일 샘플(첫 5줄):");
        sampleLines.forEach(line -> System.out.println("[ARCHIVE-IT]      " + line));

        boolean orderExists = orderRepository.existsById(orderId);
        boolean orderItemExists = orderItemRepository.existsById(orderItemId);
        System.out.println("[ARCHIVE-IT] 5-4 Purge 확인: orderExists="
                + orderExists
                + ", orderItemExists="
                + orderItemExists
                + ", result="
                + result);

        assertFalse(preview.candidates().isEmpty(), "아카이빙 대상 주문이 조회되어야 합니다.");
        assertTrue(Files.exists(archiveFile), "CSV 파일이 생성되어야 합니다.");
        assertTrue(sampleLines.stream().anyMatch(line -> line.contains(order.getPaymentId())), "CSV에 paymentId가 기록되어야 합니다.");
        assertFalse(orderExists, "아카이빙 완료 후 주문은 purge되어야 합니다.");
        assertFalse(orderItemExists, "아카이빙 완료 후 주문 아이템은 purge되어야 합니다.");
    }

    private Order createAbandonedEasyPayOrder() {
        String unique = UUID.randomUUID().toString();
        Member member = memberRepository.save(Member.builder()
                .email("archive-it-" + unique + "@example.com")
                .pw("test-password")
                .nickname("archive-it")
                .build());
        Product product = productRepository.save(Product.builder()
                .pname("Archive Integration Product")
                .pdesc("archive integration test")
                .price(1000)
                .stock(10)
                .build());

        Order order = Order.ready(member, 2000, "EASY_PAY");
        order.assignPaymentId("archive-it-" + unique);
        order.addItem(OrderItem.snapshot(product, 2));
        Order savedOrder = orderRepository.saveAndFlush(order);

        jdbcTemplate.update(
                "update tbl_order set created_at = ? where ono = ?",
                Timestamp.valueOf(LocalDateTime.now().minusMinutes(4)),
                savedOrder.getOno()
        );
        entityManager.clear();
        return savedOrder;
    }

    private void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (var paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(candidate -> {
                        try {
                            Files.deleteIfExists(candidate);
                        } catch (IOException e) {
                            throw new IllegalStateException("테스트 아카이브 디렉토리 정리에 실패했습니다. path=" + candidate, e);
                        }
                    });
        }
    }
}
