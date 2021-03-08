package me.heesu.reactivespring.controller;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@WebFluxTest // controller 어노테이션만 스캔
public class FluxMonoControllerTest {

    @Autowired
    WebTestClient webTestClient; //WebFluxTest 어노테이션을 통해서 해당 객체주입처리


    /**
     * Non Blocking API 테스트
     *  WebTestClient를 이용해서 논블로킹 API 테스트를 하는 방법 리스팅
     */

    @Test
    public void flux_call1(){
        Flux<Integer> result = webTestClient.get().uri("/flux")
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
    public void flux_call2(){
        webTestClient.get().uri("/flux")
                .accept(MediaType.APPLICATION_JSON)
                .exchange() // 실제로 해당 엔드포인트를 호출
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Integer.class)
                .hasSize(4);
    }

    @Test
    public void flux_call3(){

        List<Integer> expectedIntegerList = Arrays.asList(1,2,3,4);

        EntityExchangeResult<List<Integer>> entityExchangeResult = webTestClient.get().uri("/flux")
                .accept(MediaType.APPLICATION_JSON)
                .exchange() // 실제로 해당 엔드포인트를 호출
                .expectStatus().isOk()
                .expectBodyList(Integer.class)
                .returnResult();

        assertEquals(expectedIntegerList, entityExchangeResult.getResponseBody());
    }

    @Test
    public void flux_call4(){

        List<Integer> expectedIntegerList = Arrays.asList(1,2,3,4);

        webTestClient.get().uri("/flux")
                .accept(MediaType.APPLICATION_JSON)
                .exchange() // 실제로 해당 엔드포인트를 호출
                .expectStatus().isOk()
                .expectBodyList(Integer.class)
                .consumeWith((response) -> {
                    assertEquals(expectedIntegerList, response.getResponseBody());
                });
    }


    /**
     * infinite stream에 대한 test
     */
    @Test
    public void fluxStream(){
        Flux<Long> longStreamFlux = webTestClient.get().uri("/fluxstream")
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange() // 실제로 해당 엔드포인트를 호출
                .expectStatus().isOk()
                .returnResult(Long.class)
                .getResponseBody();

        StepVerifier.create(longStreamFlux)
                .expectNext(0L,1L,2L)// 무한대의 flux stream에서 발생시키는 데이터를 모두 확인할 수 없으므로 어느정도 확인 후 cancel
                .thenCancel()
                .verify();
    }

    @Test
    public void mono(){
        Integer expectedValue = 1;

        webTestClient.get().uri("/mono")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .consumeWith((response) -> {
                    assertEquals(expectedValue, response.getResponseBody());
                });
    }
}
