package me.heesu.reactivespring.handler;

import me.heesu.reactivespring.constants.ItemConstants;
import me.heesu.reactivespring.document.Item;
import me.heesu.reactivespring.repository.ItemReactiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test") //특정 환경에서만 테스트가 돌도록 처리
public class ItemHandlerTest {

    @Autowired
    WebTestClient client;

    @Autowired
    ItemReactiveRepository itemReactiveRepository;


    public List<Item> data(){
        return Arrays.asList(
                new Item(null, "item1", 100.0),
                new Item(null, "item2", 200.0),
                new Item(null, "item3", 300.0),
                new Item("id4", "item4", 400.0)
        );
    }

    @BeforeEach
    public void setup(){
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(data()))
                .flatMap(itemReactiveRepository::save)
                .doOnNext(item -> System.out.println("[TEST] inserted item : " + item))
                .blockLast(); // 테스트를 진행하기 위해서 insert가 완료되어야함
    }

    @Test
    public void getAllItems(){
        client.get().uri(ItemConstants.ITEM_FUNCTIONAL_END_POINT_V1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Item.class)
                .hasSize(4);
    }

    @Test
    public void getOneItem(){
        client.get().uri(ItemConstants.ITEM_FUNCTIONAL_END_POINT_V1.concat("/{id}"), "id4")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price", 400.0);
    }

    @Test
    public void getOneItemFail(){
        client.get().uri(ItemConstants.ITEM_FUNCTIONAL_END_POINT_V1.concat("/{id}"), "id5")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void runtimeException(){
        client.get().uri("/func/exception")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message", "RuntimeException occured");
    }
}
