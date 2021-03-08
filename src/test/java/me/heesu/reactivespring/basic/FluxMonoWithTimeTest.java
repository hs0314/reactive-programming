package me.heesu.reactivespring.basic;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

public class FluxMonoWithTimeTest {

    @Test
    public void infiniteSequenceTest() throws InterruptedException{

        Flux<Integer> finiteFlux = Flux.interval(Duration.ofMillis((200))) // 이 flux는 200ms마다 계속 값을 emit한다.
                .delayElements(Duration.ofSeconds(1)) // flux에서 값을 보내는것과 상관없이 1초에 한번씩 받아서 처리
                .map(l -> l.intValue())
                .take(3)
                .log();
        StepVerifier.create(finiteFlux)
                .expectSubscription()
                .expectNext(0, 1, 2)
                .verifyComplete();
    }
}
