package me.heesu.reactivespring.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * RestController approach
 */
@RestController
public class FluxMonoController {

    @GetMapping("/flux") // 여기서 해당 엔드포인트를 호출하는 브라우저가 subscriber가 되어서 데이터를 받는다
    public Flux<Integer> getIntegerFlux(){

        return Flux.just(1,2,3,4)
                .delayElements(Duration.ofSeconds(1)) // delay를 주면 브라우저에서 기본적으로 json으로 셋팅해서 디스플레이하는데에 블로킹 발생
                .log();
    }

    /**
     * stream으로 결과 MediaType을 설정해주면 데이터 하나를 받을때마다 브라우저에서 렌더
     */
    @GetMapping(value = "/fluxstream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Long> getFluxStream(){

        return Flux.interval(Duration.ofSeconds(1))
                .log();
    }

    /**
     * Mono
     */
    @GetMapping("/mono")
    public Mono<Integer> getMono(){

        return Mono.just(1).log();
    }
}
