package me.heesu.reactivespring.handler.handler;

import me.heesu.reactivespring.document.Item;
import me.heesu.reactivespring.document.ItemCapped;
import me.heesu.reactivespring.repository.ItemReactiveCappedRepository;
import me.heesu.reactivespring.repository.ItemReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ItemHandler {

    @Autowired
    ItemReactiveRepository itemReactiveRepository;

    @Autowired
    ItemReactiveCappedRepository itemReactiveCappedRepository;

    static Mono<ServerResponse> NOT_FOUND = ServerResponse.notFound().build();

    public Mono<ServerResponse> getAllItems(ServerRequest req){

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(itemReactiveRepository.findAll(), Item.class);
    }

    public Mono<ServerResponse> getOneItem(ServerRequest req){

        String id = req.pathVariable("id");

        Mono<Item> itemMono = itemReactiveRepository.findById(id);

        return itemMono.flatMap(item ->
            ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(item))
            .switchIfEmpty(NOT_FOUND);
    }

    public Mono<ServerResponse> createItem(ServerRequest req){

        // req body를 Mono로 셋팅
        Mono<Item> itemTobeInserted = req.bodyToMono(Item.class);

        return itemTobeInserted.flatMap(item ->
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(itemReactiveRepository.save(item), Item.class));
    }

    public Mono<ServerResponse> deleteItem(ServerRequest req){
        String id = req.pathVariable("id");

        Mono<Void> deleteItem = itemReactiveRepository.deleteById(id);

        return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(deleteItem, Void.class);
    }

    public Mono<ServerResponse> updateItem(ServerRequest req){
        String id = req.pathVariable("id");

        Mono<Item> updatedItem = req.bodyToMono(Item.class)
                .flatMap(item -> {  // 변경요청body -> Mono<Item>
                    Mono<Item> itemMono = itemReactiveRepository.findById(id)
                            .flatMap(currentItem -> {  // item update
                                currentItem.setDescription(item.getDescription());
                                currentItem.setPrice(item.getPrice());

                                return itemReactiveRepository.save(currentItem);
                            });

                    return itemMono;
                });

        return updatedItem.flatMap(item ->
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(item))
                .switchIfEmpty(NOT_FOUND);

    }

    public Mono<ServerResponse> itemException(ServerRequest req){
        throw new RuntimeException("RuntimeException occured");
    }

    //item stream
    public Mono<ServerResponse> itemsStream(ServerRequest req){
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_STREAM_JSON)
                .body(itemReactiveCappedRepository.findItemsBy(), ItemCapped.class);

    }
}
