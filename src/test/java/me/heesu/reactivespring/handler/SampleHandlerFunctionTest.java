package me.heesu.reactivespring.handler;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest // functional handler는 @Componet이기 때문에 컴포넌트 @WebFluxTest어노테이션으로는 스캔이 불가
@AutoConfigureWebTestClient // webTestClient bean 생성을 위해서 처리
@DirtiesContext // test용, application context state를 변경하는 부분에서 선언해줘야함
public class SampleHandlerFunctionTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    public void flux_call(){
        Flux<Integer> result = webTestClient.get().uri("/functional/flux")
                .accept(MediaType.APPLICATION_JSON)
                .exchange() // 실제로 해당 엔드포인트를 호출
                .expectStatus().isOk()
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(result)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    public void mono_call(){
        Integer expectedValue = 1;

        webTestClient.get().uri("/functional/mono")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .consumeWith((response) -> {
                    assertEquals(expectedValue, response.getResponseBody());
                });
    }

}
