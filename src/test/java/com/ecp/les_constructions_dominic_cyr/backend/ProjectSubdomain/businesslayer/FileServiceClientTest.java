package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.FileServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceClientTest {

    private static FileServiceClient buildClientWithExchange(ExchangeFunction exchangeFunction) throws Exception {
        FileServiceClient client = new FileServiceClient("http://test");
        WebClient mockWebClient = WebClient
                .builder()
                .baseUrl("http://test")
                .exchangeFunction(exchangeFunction)
                .build();

        Field f = FileServiceClient.class.getDeclaredField("webClient");
        f.setAccessible(true);
        f.set(client, mockWebClient);
        return client;
    }

    @Test
    void validateFileExists_returnsTrueOn2xx() throws Exception {
        ExchangeFunction okExchange = request -> Mono.just(ClientResponse.create(HttpStatus.OK).build());
        FileServiceClient client = buildClientWithExchange(okExchange);

        Boolean result = client.validateFileExists("file-123").block();
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    void validateFileExists_returnsFalseOn404() throws Exception {
        ExchangeFunction notFoundExchange = request -> Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        FileServiceClient client = buildClientWithExchange(notFoundExchange);

        Boolean result = client.validateFileExists("file-404").block();
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    void validateFileExists_returnsFalseOnError() throws Exception {
        ExchangeFunction errorExchange = request -> Mono.error(new RuntimeException("boom"));
        FileServiceClient client = buildClientWithExchange(errorExchange);

        Boolean result = client.validateFileExists("file-err").block();
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    void validateFileExists_returnsFalseForNullOrEmpty() {
        FileServiceClient client = new FileServiceClient("http://test");

        assertFalse(client.validateFileExists(null).block());
        assertFalse(client.validateFileExists("  ").block());
    }
}


