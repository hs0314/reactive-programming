package me.heesu.reactivespring.initialize;

import me.heesu.reactivespring.document.Item;
import me.heesu.reactivespring.repository.ItemReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Component
@Profile("!test")
public class ItemDataInitializer implements CommandLineRunner {

    @Autowired
    ItemReactiveRepository itemReactiveRepository;

    @Override
    public void run(String... args) throws Exception {
        initialDataSetup();
    }

    /*
      서버가 뜨고 data init 처리
     */
    public List<Item> getData(){
        return Arrays.asList(
                new Item(null, "item1", 100.0),
                new Item(null, "item2", 200.0),
                new Item(null, "item3", 300.0),
                new Item(null, "item4", 400.0)
        );
    }

    private void initialDataSetup() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable( getData()))
                .flatMap(itemReactiveRepository::save)
                .thenMany(itemReactiveRepository.findAll()) // log확인용
                .subscribe(item -> System.out.println("[Item init] item inserted : " + item));



    }
}

