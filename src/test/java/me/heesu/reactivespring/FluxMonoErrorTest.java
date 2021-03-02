package me.heesu.reactivespring;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import java.time.Duration;

public class FluxMonoErrorTest {

    @Test
    public void fluxErrorHandling(){
        Flux<String> stringFlux = Flux.just("A","B","C")
                .concatWith(Flux.error(new RuntimeException("runtion exception!")))
                .concatWith(Flux.just("D"))
                .onErrorResume((e) -> {   // exception handling
                   System.out.println("Exception is : " + e);
                   return Flux.just("default1", "default2");
                });

        StepVerifier.create(stringFlux.log())
                .expectSubscription()
                .expectNext("A","B","C")
                //.expectError(RuntimeException.class)
                .expectNext("default1", "default2")
                .verifyComplete();
    }

    @Test
    public void fluxErrorHandlingOnErrorReturn(){
        Flux<String> stringFlux = Flux.just("A","B","C")
                .concatWith(Flux.error(new RuntimeException("runtion exception!")))
                .concatWith(Flux.just("D"))
                .onErrorReturn("default");

        StepVerifier.create(stringFlux.log())
                .expectSubscription()
                .expectNext("A","B","C")
                .expectNext("default")
                .verifyComplete();
    }


    @Test
    public void fluxErrorHandlingOnErrorMap(){
        Flux<String> stringFlux = Flux.just("A","B","C")
                .concatWith(Flux.error(new RuntimeException("runtion exception!")))
                .concatWith(Flux.just("D"))
                .onErrorMap((e) ->  new CustomException(e));   // 다른 타입의 익셉션으로 변환 가능


        StepVerifier.create(stringFlux.log())
                .expectSubscription()
                .expectNext("A","B","C")
                .expectError(CustomException.class)
                .verify();
    }


    @Test
    public void fluxErrorHandlingOnErrorMapWithRetry(){
        Flux<String> stringFlux = Flux.just("A","B","C")
                .concatWith(Flux.error(new RuntimeException("runtion exception!")))
                .concatWith(Flux.just("D"))
                .onErrorMap((e) ->  new CustomException(e)) // 다른 타입의 익셉션으로 변환 가능
                .retry(1);


        StepVerifier.create(stringFlux.log())
                .expectSubscription()
                .expectNext("A","B","C")
                .expectNext("A","B","C") // retry를 통해서 한번 더 시퀀스를 처리
                .expectError(CustomException.class)
                .verify();
    }

    //todo retryWhen 다시 확인해봐야함
    @Test
    public void fluxErrorHandlingOnErrorMapWithRetryBackOff(){
        Flux<String> stringFlux = Flux.just("A","B","C")
                .concatWith(Flux.error(new RuntimeException("runtion exception!")))
                .concatWith(Flux.just("D"))
                .onErrorMap((e) ->  new CustomException(e)) // 다른 타입의 익셉션으로 변환 가능
                // .retryBackoff(1, Duration.ofSeconds(3));
                // retryBackoff()은 reactor 3.4 버전부터 deprecated.. retryWhen을 사용
                .retryWhen(Retry.backoff(1, Duration.ofSeconds(1)));


        StepVerifier.create(stringFlux.log())
                .expectSubscription()
                .expectNext("A","B","C")
                .expectNext("A","B","C") // retry를 통해서 한번 더 시퀀스를 처리
                .expectError(CustomException.class)
                .verify();
    }
}
