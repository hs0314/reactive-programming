package me.heesu.reactivespring.repository;

import me.heesu.reactivespring.document.Item;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@DataMongoTest
@RunWith(SpringRunner.class)
@DirtiesContext // test용, application context state를 변경하는 부분에서 선언해줘야함
public class ItemReactiveRepositoryTest {

    @Autowired
    ItemReactiveRepository itemReactiveRepository;

    List<Item> itemList = Arrays.asList(
            new Item(null,"item1",100.0),
            new Item(null,"item2",200.0),
            new Item(null,"item3",300.0),
            new Item("ID4","item4",400.0) // 따로 Id를 넘기지 않으면 알아서 id를 생성해준다
    );

    @BeforeEach
    void setup(){
        //테스트용 셋팅
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(itemList))
                .flatMap(itemReactiveRepository::save) // item 별로 save()
                .doOnNext((item -> {
                    System.out.println("Inserted Item is : " + item);
                }))
                .blockLast(); // 모든 save 작업이 종료될때까지 block => tc에서만 사용
    }

    @Test
    public void getAllItems(){
        Flux<Item> itemFlux = itemReactiveRepository.findAll(); //flux 반환

        StepVerifier.create(itemFlux)
                .expectSubscription()
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void getItemById(){
        StepVerifier.create(itemReactiveRepository.findById("ID4")) // Mono return
                .expectSubscription()
                .expectNextMatches((item -> item.getDescription().equals("item4"))) // 해당 메서드는 파라미터로 Predicate(=functional interface)를 받는다
                .verifyComplete();
    }

    @Test
    public void findItemByDescription(){
        StepVerifier.create(itemReactiveRepository.findByDescription("item4").log("findItemByDescription : "))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void saveItem(){

        Item item = new Item(null, "item5", 500.0);
        Mono<Item> insertedItem = itemReactiveRepository.save(item);

        StepVerifier.create(insertedItem.log("insertedItem : "))
                .expectSubscription()
                .expectNextMatches(i -> (i.getId() != null) && (i.getDescription().equals("item5")))
                .verifyComplete();
    }

    @Test
    public void updateItem(){
        double newPrice = 2000.0;

        Mono<Item> updatedItem = itemReactiveRepository.findByDescription("item2")
                .map(item -> {
                    item.setPrice(newPrice); // setting new price
                    return item;
                })
                .flatMap(item ->{
                    return itemReactiveRepository.save(item); // save updated item
                });

        StepVerifier.create(updatedItem)
                .expectSubscription()
                .expectNextMatches(item -> item.getPrice() == 2000.0)
                .verifyComplete();
    }

    @Test
    public void deleteItemById(){
        Mono<Void> deletedItem  = itemReactiveRepository.findById("ID4") // Mono<Item>
            .map(Item::getId) // 타입A -> 타입B로 transform할때 map() 사용
            .flatMap((id) -> {
                return itemReactiveRepository.deleteById(id); //Mono<Void>
            });

        StepVerifier.create(deletedItem.log(""))
                .expectSubscription()
                .verifyComplete();

        //item이 제대로 지워졌는지 체크
        StepVerifier.create(itemReactiveRepository.findAll().log("ItemList after Delete : "))
                .expectNextCount(3) // 기본셋팅item수 - 1
                .verifyComplete();
    }

    @Test
    public void deleteItemByElement(){
        // item객체 자체로 삭제
        Mono<Void> deletedItem  = itemReactiveRepository.findById("ID4") // Mono<Item>
                .flatMap((item) -> {
                    return itemReactiveRepository.delete(item); //Mono<Void>
                });

        StepVerifier.create(deletedItem.log(""))
                .expectSubscription()
                .verifyComplete();

        //item이 제대로 지워졌는지 체크
        StepVerifier.create(itemReactiveRepository.findAll().log("ItemList after Delete : "))
                .expectNextCount(3) // 기본셋팅item수 - 1
                .verifyComplete();
    }
}
