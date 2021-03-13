package me.heesu.reactivespring.controller.v1;

import me.heesu.reactivespring.constants.ItemConstants;
import me.heesu.reactivespring.document.Item;
import me.heesu.reactivespring.repository.ItemReactiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test") //특정 환경에서만 테스트가 돌도록 처리
public class ItemControllerTest {

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
        client.get().uri(ItemConstants.ITEM_END_POINT_V1)
                .exchange() // 실제로 endpoint에 connect
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Item.class)
                .hasSize(4); // setup에서 인서트된 아이템 갯수
    }

    @Test
    public void getAllItems2 (){
        client.get().uri(ItemConstants.ITEM_END_POINT_V1)
                .exchange() // 실제로 endpoint에 connect
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Item.class)
                .hasSize(4)  // setup에서 인서트된 아이템 갯수
        .consumeWith((response) -> { // response에 접근 가능
            List<Item> itemList = response.getResponseBody();
            itemList.forEach((item) -> {
                assertTrue(item.getId() != null); // insert가 완료되었으면 id가 null이 아님
            });
        });
    }

    @Test
    public void getAllItems3(){
        Flux<Item> itemFlux = client.get().uri(ItemConstants.ITEM_END_POINT_V1)
                .exchange() // 실제로 endpoint에 connect
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(Item.class)
                .getResponseBody();

        StepVerifier.create(itemFlux.log("Value from network : "))
                .expectSubscription()
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void getOneItem(){
        client.get().uri(ItemConstants.ITEM_END_POINT_V1.concat("/{id}"), "id4")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price", 400.0);
    }

    @Test
    public void getOneItemFail(){
        client.get().uri(ItemConstants.ITEM_END_POINT_V1.concat("/{id}"), "id5")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void createItem(){
        Item item = new Item(null, "item5", 500.0);

        client.post().uri(ItemConstants.ITEM_END_POINT_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(item), Item.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.description").isEqualTo("item5")
                .jsonPath("$.price").isEqualTo(500.0);
    }

    @Test
    public void deleteItem(){
        client.delete().uri(ItemConstants.ITEM_END_POINT_V1.concat("/{id}"), "id4")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);
    }

    @Test
    public void updateItem(){
        double newPrice = 4000.0;
        Item tobeItem = new Item(null, "id4", newPrice);

        client.put().uri(ItemConstants.ITEM_END_POINT_V1.concat("/{id}"), "id4")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(tobeItem), Item.class) // 요청body
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price").isEqualTo(newPrice);
    }

    @Test
    public void updateItemFail(){
        double newPrice = 4000.0;
        Item tobeItem = new Item(null, "id4", newPrice);

        client.put().uri(ItemConstants.ITEM_END_POINT_V1.concat("/{id}"), "id44") // 없는 id로 돌릴 경우
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(tobeItem), Item.class) // 요청body
                .exchange()
                .expectStatus().isNotFound();
    }


}
