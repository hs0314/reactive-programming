package me.heesu.reactivespring.repository;

import me.heesu.reactivespring.document.Item;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ItemReactiveRepository extends ReactiveMongoRepository<Item,String> { // <domain, id type>

    Mono<Item> findByDescription(String description);
}
