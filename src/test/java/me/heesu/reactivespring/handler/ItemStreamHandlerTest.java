package me.heesu.reactivespring.handler;

import me.heesu.reactivespring.constants.ItemConstants;
import me.heesu.reactivespring.document.ItemCapped;
import me.heesu.reactivespring.repository.ItemReactiveCappedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test") //특정 환경에서만 테스트가 돌도록 처리
public class ItemStreamHandlerTest {

    @Autowired
    WebTestClient client;

    @Autowired
    ItemReactiveCappedRepository itemReactiveCappedRepository;

    @Autowired
    MongoOperations mongoOperations;

    @BeforeEach
    public void setup(){
        mongoOperations.dropCollection(ItemCapped.class);
        mongoOperations.createCollection(ItemCapped.class, CollectionOptions.empty().maxDocuments(20).size(50000).capped());


        // 1초마다 ItemCapped 생성
        Flux<ItemCapped> itemCappedFlux = Flux.interval(Duration.ofMillis(100))
                .map(i -> new ItemCapped(null, "Random Item" + i, (100.00+i)))
                .take(5);

        // 여기서의 insert는 위의 flux를 subscribe함으로써 1초마다 db에 데이터를 insert처리
        itemReactiveCappedRepository
                .insert(itemCappedFlux)
                .doOnNext(itemCapped -> {
                    System.out.println("Inserted Item : " + itemCapped);
                })
                .blockLast(); // 테스트 이전에 셋팅이 완료되어야하므로 block 호출
    }

    @Test
    public void testStreamAllItems(){
        Flux<ItemCapped> itemCappedFlux = client.get().uri(ItemConstants.ITEM_STREAM_FUNCTIONAL_END_POINT_V1)
                .exchange()
                .expectStatus().isOk()
                .returnResult(ItemCapped.class)
                .getResponseBody()
                .take(5);

        StepVerifier.create(itemCappedFlux)
                .expectNextCount(5)
                .thenCancel()
                .verify();
    }
}
