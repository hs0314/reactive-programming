package me.heesu.reactivespring.basic;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

public class FluxMonoCombineTest {

    /*
    다른 리액티브 스트림을 합치기
     */
    @Test
    public void combineUsingMerge(){
        Flux<String> flux1 = Flux.just("A","B","C");
        Flux<String> flux2 = Flux.just("D","E","F");

        Flux<String> mergedFlux = Flux.merge(flux1, flux2);

        StepVerifier.create(mergedFlux.log())
                .expectSubscription()
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    public void combineUsingMergeWithDelay(){
        /*
         merge에서 주의해야 할 점은 기대한 순서대로 element를 처리하지 않을 수 있음
         ex) 아래의 에시에서는 A-D-B-E-C-F  /  D-A-E-B-F-C 와 같은 순서로 나올 수 있다
         */
        Flux<String> flux1 = Flux.just("A","B","C").delayElements(Duration.ofSeconds(1));
        Flux<String> flux2 = Flux.just("D","E","F").delayElements(Duration.ofSeconds(1));

        Flux<String> mergedFlux = Flux.merge(flux1, flux2);

        StepVerifier.create(mergedFlux.log())
                .expectSubscription()
                .expectNextCount(6)
                //.expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    public void combineUsingConcat(){
        Flux<String> flux1 = Flux.just("A","B","C");
        Flux<String> flux2 = Flux.just("D","E","F");

        Flux<String> mergedFlux = Flux.concat(flux1, flux2);

        StepVerifier.create(mergedFlux.log())
                .expectSubscription()
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    public void combineUsingZip(){
        Flux<String> flux1 = Flux.just("A","B","C");
        Flux<String> flux2 = Flux.just("D","E","F");

        Flux<String> mergedFlux = Flux.zip(flux1, flux2, (t1,t2) -> {
            return t1.concat(t2); // AD -> BE -> CF
        });

        StepVerifier.create(mergedFlux.log())
                .expectSubscription()
                .expectNext("AD","BE","CF")
                .verifyComplete();
    }


}
