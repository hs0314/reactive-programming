package me.heesu.reactivespring.basic;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static reactor.core.scheduler.Schedulers.parallel;

public class FluxMonoTransformTest {

    List<String> nameList = Arrays.asList("adam", "anna", "jack", "jenny");

    @Test
    public void transformUsingMap(){
        Flux<String> nameFlux = Flux.fromIterable(nameList)
                .map(s -> s.toUpperCase())
                .log();

        StepVerifier.create(nameFlux)
                .expectNext("ADAM","ANNA","JACK","JENNY")
                .verifyComplete();
    }

    @Test
    public void transformUsingMapLength(){
        Flux<Integer> nameFlux = Flux.fromIterable(nameList)
                .map(s -> s.length())
                .repeat(1)
                .log();

        StepVerifier.create(nameFlux)
                .expectNext(4,4,4,5,4,4,4,5)
                .verifyComplete();
    }

    @Test
    public void transformUsingMapFilter(){
        Flux<String> nameFlux = Flux.fromIterable(nameList)
                .filter(s -> s.length() > 4)
                .map(s -> s.toUpperCase())
                .log();

        StepVerifier.create(nameFlux)
                .expectNext("JENNY")
                .verifyComplete();
    }


    /*
     flatMap
      -> 예를 들어서, Flux<T>를 반환하는 외부 서비스를 호출한다고 했을때 결과를 단일 원소스트림으로 반환
       map ex) return     -> Stream<String[]>
       flatmap ex) return -> Stream<String>
     */
    @Test
    public void transformUsingFlatMap(){
        Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A","B","C","D","E","F"))
                .flatMap(s -> {
                    return Flux.fromIterable(converToList(s));
                })
                .log();

        StepVerifier.create(stringFlux)
                .expectNextCount(12)
                .verifyComplete();
    }

    private List<String> converToList(String s) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        return Arrays.asList(s, "newValue");
    }

    /*
     병렬처리를 통해서 6가지 요소에 대한 처리를
     */
    @Test
    public void transformUsingFlatMapUsingParallel(){
        Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A","B","C","D","E","F"))
                .window(2)       // Flux<Flux<String>> -> 하나씩 처리하지 않고 2개가 쌓일때 한번에 subs에 보내도록함
                .flatMapSequential((s) ->  // 순서를 지켜서 처리하고 싶으면 flatMapSequential() 사용
                        // subscribeOn 을 통해서 새로운 쓰레드에서 작업을 수행
                    s.map(this::converToList).subscribeOn(parallel()) // Flux<List<String>>
                )
                .flatMap(s -> Flux.fromIterable(s)) // Flux<String>
                .log();

        StepVerifier.create(stringFlux)
                .expectNextCount(12)
                .verifyComplete();
    }
}
