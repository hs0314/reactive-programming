package me.heesu.reactivespring.initialize;

import lombok.extern.slf4j.Slf4j;
import me.heesu.reactivespring.document.Item;
import me.heesu.reactivespring.document.ItemCapped;
import me.heesu.reactivespring.repository.ItemReactiveCappedRepository;
import me.heesu.reactivespring.repository.ItemReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("!test")
@Slf4j
public class ItemDataInitializer implements CommandLineRunner {

    @Autowired
    ItemReactiveRepository itemReactiveRepository;

    @Autowired
    ItemReactiveCappedRepository itemReactiveCappedRepository;

    @Autowired
    MongoOperations mongoOperations;

    @Override
    public void run(String... args) throws Exception {
        initialDataSetup();

        createCappedCollection();
        dataSetUpForCappedCollection();
    }

    private void createCappedCollection() {
        mongoOperations.dropCollection(ItemCapped.class);
        mongoOperations.createCollection(ItemCapped.class, CollectionOptions.empty().maxDocuments(20).size(50000).capped());
    }

    /*
      서버가 뜨고 data init 처리
     */
    public List<Item> getData(){
        return Arrays.asList(
                new Item(null, "item1", 100.0),
                new Item(null, "item2", 200.0),
                new Item(null, "item3", 300.0),
                new Item("item4", "item4", 400.0)
        );
    }

    // capped collection에 계속해서 데이터를 넣고 inserted된 데이터를 계속해서 스트림을 통해서 받아오는 endpoint를 생성
    public void dataSetUpForCappedCollection(){

        // 1초마다 ItemCapped 생성
        Flux<ItemCapped> itemCappedFlux = Flux.interval(Duration.ofSeconds(1))
                .map(i -> new ItemCapped(null, "Random Item" + i, (100.00+i)));

        // 여기서의 insert는 위의 flux를 subscribe함으로써 1초마다 db에 데이터를 insert처리
        itemReactiveCappedRepository
                .insert(itemCappedFlux)
                .subscribe(itemCapped -> {
                    log.info("Inserted Item : " + itemCapped);
                });
    }

    private void initialDataSetup() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable( getData()))
                .flatMap(itemReactiveRepository::save)
                .thenMany(itemReactiveRepository.findAll()) // log확인용
                .subscribe(item -> System.out.println("[Item init] item inserted : " + item));
    }
}

