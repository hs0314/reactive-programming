package me.heesu.reactivespring.basic;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class FluxMonoFactoryTest {

    List<String> nameList = Arrays.asList("adam", "anna", "jack", "jenny");

    @Test
    public void fluxUsingIterable(){
        Flux<String> nameFlux = Flux.fromIterable(nameList);

        StepVerifier.create(nameFlux.log())
                .expectNext("adam", "anna", "jack", "jenny")
                .verifyComplete();
    }

    @Test
    public void fluxUsingArray(){
        String[] names = new String[]{"adam", "anna", "jack", "jenny"};

        Flux<String> nameFlux = Flux.fromArray(names);
        StepVerifier.create(nameFlux.log())
                .expectNext("adam", "anna", "jack", "jenny")
                .verifyComplete();
    }

    @Test
    public void fluxUsingStream(){
        Flux<String> nameFlux = Flux.fromStream(nameList.stream());

        StepVerifier.create(nameFlux.log())
                .expectNext("adam", "anna", "jack", "jenny")
                .verifyComplete();
    }

    @Test
    public void fluxUsingRange(){
        Flux<Integer> integerFlux = Flux.range(1,5);

        StepVerifier.create(integerFlux.log())
                .expectNext(1,2,3,4,5)
                .verifyComplete();
    }


    @Test
    public void monoUsingJustOrEmpty(){
        Mono<String> mono = Mono.justOrEmpty(null); // =Mono.Empty()

        StepVerifier.create(mono) // 빈 Mono이기 때문에 데이터 전송이 없다
                .verifyComplete();
    }

    @Test
    public void monoUsingSupplier(){
        Supplier<String> stringSupplier = () -> {return "adam";};

        Mono<String> mono = Mono.fromSupplier(stringSupplier);

        StepVerifier.create(mono.log())
                .expectNext("adam")
                .verifyComplete();
    }


}
