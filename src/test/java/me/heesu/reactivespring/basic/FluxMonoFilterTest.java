package me.heesu.reactivespring.basic;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

public class FluxMonoFilterTest {
    List<String> nameList = Arrays.asList("adam", "anna", "jack", "jenny");

    @Test
    public void filterTest(){
        Flux<String> nameFlux = Flux.fromIterable(nameList)
                .filter(s -> s.startsWith("a")) // jack, jenny는 필터링되어서 subscriber로 넘어가지 않음
                .log();

        StepVerifier.create(nameFlux)
                .expectNext("adam","anna")
                .verifyComplete();
    }
}
