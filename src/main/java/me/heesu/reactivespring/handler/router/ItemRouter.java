package me.heesu.reactivespring.handler.router;

import me.heesu.reactivespring.handler.handler.ItemHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static me.heesu.reactivespring.constants.ItemConstants.ITEM_FUNCTIONAL_END_POINT_V1;
import static me.heesu.reactivespring.constants.ItemConstants.ITEM_STREAM_FUNCTIONAL_END_POINT_V1;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class ItemRouter {

    @Bean
    public RouterFunction<ServerResponse> itemRoute(ItemHandler itemHandler){

        return RouterFunctions
                .route(GET(ITEM_FUNCTIONAL_END_POINT_V1).and(accept(MediaType.APPLICATION_JSON))
                        , itemHandler::getAllItems)
                .andRoute(GET(ITEM_FUNCTIONAL_END_POINT_V1+"/{id}").and(accept(MediaType.APPLICATION_JSON))
                        , itemHandler::getOneItem)
                .andRoute(POST(ITEM_FUNCTIONAL_END_POINT_V1).and(accept(MediaType.APPLICATION_JSON))
                        , itemHandler::createItem)
                .andRoute(DELETE(ITEM_FUNCTIONAL_END_POINT_V1+"/{id}").and(accept(MediaType.APPLICATION_JSON))
                        , itemHandler::deleteItem)
                .andRoute(PUT(ITEM_FUNCTIONAL_END_POINT_V1+"/{id}").and(accept(MediaType.APPLICATION_JSON))
                        , itemHandler::updateItem);
    }

    // exception handling
    @Bean
    public RouterFunction<ServerResponse> errorRoute(ItemHandler itemHandler){
        return RouterFunctions
                .route(GET("/func/exception").and(accept(MediaType.APPLICATION_JSON))
                        , itemHandler::itemException);
    }

    // capped collection
    @Bean
    public RouterFunction<ServerResponse> itemStreamRoute(ItemHandler itemHandler){
        return RouterFunctions
                .route(GET(ITEM_STREAM_FUNCTIONAL_END_POINT_V1).and(accept(MediaType.APPLICATION_JSON))
                    , itemHandler::itemsStream);

    }

}
