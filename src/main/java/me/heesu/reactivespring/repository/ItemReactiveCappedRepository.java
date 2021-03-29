package me.heesu.reactivespring.repository;

import me.heesu.reactivespring.document.Item;
import me.heesu.reactivespring.document.ItemCapped;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ItemReactiveCappedRepository extends ReactiveMongoRepository<ItemCapped,String> { // <domain, id type>

    @Tailable
    Flux<ItemCapped> findItemsBy();
}
