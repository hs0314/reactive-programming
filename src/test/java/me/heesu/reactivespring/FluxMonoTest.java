package me.heesu.reactivespring;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class FluxMonoTest {

    /**
     * 기본적인 Flux, Mono 작동 확인
     */
    @Test
    public void fluxTest(){
        Flux<String> stringFlux = Flux.just("Spring", "Spring Boot", "Reactive Spring")
                //.concatWith(Flux.error(new RuntimeException("made error!")))
                .concatWith(Flux.just("After Error")) // 에러 발이후에도 data를 emit하는가?생 => X
                .log();

        //subscribing을 통해서 Flux 값에 액세스 (subscriber에 값을 emit)
        stringFlux.subscribe(System.out::println,
                (e) -> System.err.println(e)
        ,() -> System.out.println("Completed!!!!"));
    }


    @Test
    public void fluxTestExpectNext(){
        Flux<String> stringFlux = Flux.just("Spring", "Spring Boot", "Reactive Spring")
                //.concatWith(Flux.error(new RuntimeException("made error!")))
                .log();

        //reactor test용
        //StepVerifier는 subscriber에 대해서 알아서 핸들링해줌
        StepVerifier.create(stringFlux)
                .expectNext("Spring")
                .expectNext("Spring Boot")
                .expectNext("Reactive Spring")
                .verifyComplete(); // (=subscribe())
    }

    @Test
    public void fluxTestExpectNextCount(){
        Flux<String> stringFlux = Flux.just("Spring", "Spring Boot", "Reactive Spring")
                //.concatWith(Flux.error(new RuntimeException("made error!")))
                .log();

        //reactor test용
        //StepVerifier는 subscriber에 대해서 알아서 핸들링해줌
        StepVerifier.create(stringFlux)
                .expectNextCount(3)
                .verifyComplete(); // (=subscribe())
    }


    @Test
    public void monoTest(){
        Mono<String> mono = Mono.just("Spring");

        StepVerifier.create(mono.log())
                .expectNext("Spring")
                .verifyComplete();
    }

    @Test
    public void monoErrorTest(){
        StepVerifier.create(Mono.error(new RuntimeException("mono error!")).log())
                .expectError(RuntimeException.class)
                .verify();
    }

}
