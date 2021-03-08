package me.heesu.reactivespring.basic;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;

public class ColdAndHotPublisherTest {

    @Test
    public void coldPublisherTest() throws InterruptedException {

        Flux<String> stringFlux = Flux.just("A","B","C","D","E","F")
                .delayElements(Duration.ofSeconds(1))
                .log();

        /** subs를 두개 붙여도 flux의 처음값부터 emit한다 (Cold publisher) **/
        stringFlux.subscribe(s -> System.out.println("sub1 : " + s));

        Thread.sleep(2000);

        stringFlux.subscribe(s -> System.out.println("sub2 : " + s));

        Thread.sleep(4000);
    }


    @Test
    public void hotPublisherTest() throws InterruptedException {

        Flux<String> stringFlux = Flux.just("A","B","C","D","E","F")
                .delayElements(Duration.ofSeconds(1));

        ConnectableFlux<String> connectableFlux = stringFlux.publish();
        connectableFlux.connect(); // sub2는 flux의 처음값부터 받지 않고 emit된 값 이후 값부터 받는다.
        connectableFlux.subscribe(s -> System.out.println("sub1 : " + s));
        Thread.sleep(3000);

        connectableFlux.subscribe(s -> System.out.println("sub2 : " + s));
        Thread.sleep(4000);
    }
}
