package me.heesu.reactivespring.controller.v1;

import lombok.extern.slf4j.Slf4j;
import me.heesu.reactivespring.constants.ItemConstants;
import me.heesu.reactivespring.document.Item;
import me.heesu.reactivespring.repository.ItemReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static me.heesu.reactivespring.constants.ItemConstants.ITEM_END_POINT_V1;

@RestController
@Slf4j
public class ItemController {

    // 익셉션 발생 시, 해당 메서드 호출 (컨트롤러 레벨에서 관리)
    /* ControllerExceptionHandler 를 통해서 더 글로벌한 레벨로 관리하도록 개선
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex){
        log.error("Exception caught in ExceptionHandler : {} ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ex.getMessage());
    }
     */

    @Autowired
    ItemReactiveRepository itemReactiveRepository;

    @GetMapping(ITEM_END_POINT_V1)
    public Flux<Item> getAllItem(){

        return itemReactiveRepository.findAll();
    }

    @GetMapping(ITEM_END_POINT_V1+"/{id}")
    public Mono<ResponseEntity<Item>> getOneItem(@PathVariable("id") String id){
        return itemReactiveRepository.findById(id) // Mono<Item>
                    .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                    .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // defaultIfEmpty 처리를 위해서 ResponseEntity<Item>으로 반환
    }

    @PostMapping(ITEM_END_POINT_V1)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Item> createItem(@RequestBody Item item){
        return itemReactiveRepository.save(item);
    }

    @DeleteMapping(ITEM_END_POINT_V1+"/{id}")
    public Mono<Void> deleteItem(@PathVariable("id") String id){
        // 비동기, 논블로킹 API 특성상 return 할 것이 없어도 Mono<Void>를 리턴
        return itemReactiveRepository.deleteById(id);
    }

    @PutMapping(ITEM_END_POINT_V1+"/{id}")
    public Mono<ResponseEntity<Item>> updateItem(@PathVariable("id") String id,
                                                 @RequestBody Item item){
        return itemReactiveRepository.findById(id)
                .flatMap((i) -> {
                    i.setPrice(item.getPrice()); //update process
                    i.setDescription(item.getDescription());

                    return itemReactiveRepository.save(i);
                })
                .map(i -> new ResponseEntity<>(i, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    // exception handling
    @GetMapping(ITEM_END_POINT_V1+"/exception")
    public Flux<Item> runtimeException(){
        return itemReactiveRepository.findAll()
                .concatWith(Mono.error(new RuntimeException("RuntimeException occured.")));
    }
}
