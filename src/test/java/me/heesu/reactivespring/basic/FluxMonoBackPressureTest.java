package me.heesu.reactivespring.basic;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxMonoBackPressureTest {

    @Test
    public void backPressureTest(){
        Flux<Integer> finiteFlux = Flux.range(1,10)
                .log();

        StepVerifier.create(finiteFlux)
                .expectSubscription()
                .thenRequest(1)
                .expectNext(1)
                .thenRequest(1)
                .expectNext(2)
                .thenCancel()
                .verify();
    }

    // 실제 subscribe 처리 예시
    @Test
    public void backPressure(){
        Flux<Integer> finiteFlux = Flux.range(1,10)
                .log();
        finiteFlux.subscribe((e) -> System.out.println("e is : " + e)  // 실제 퍼블리셔에서 넘겨받은 값
        , (e) -> System.err.println("exception is : " + e) // 익셉션 핸들링
        , () -> System.out.println("Done") // 성공
        , (subscription -> subscription.request(2))); // subscription -> request, cancel..
    }

    @Test
    public void customizedBackPressuer(){
        Flux<Integer> finiteFlux = Flux.range(1,10)
                .log();

        finiteFlux.subscribe(new BaseSubscriber<Integer>() {
            @Override
            protected void hookOnNext(Integer value) {
                //element를 하나씩 받아서 조건만족시 cancel
                request(1);
                System.out.println("val : " + value);
                if(value == 4){
                    cancel();
                }
            }
        });
    }
}
