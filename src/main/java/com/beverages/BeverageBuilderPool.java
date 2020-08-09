package com.beverages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.IngredientsInventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class BeverageBuilderPool {

    @Value("input.json")
    Resource resourceLoader;

    @Autowired
    BeverageRegistry beverageRegistry;

    @Autowired
    IngredientsInventory ingredientsInventory;

    /*
        This queue can be accessed concurrently from multiple threads
     */
    private ConcurrentLinkedQueue<BeverageBuilder> outletPool = new ConcurrentLinkedQueue<>();

    private Integer numberOfOutlets;

    /*
        At the time of initialization pool size is configured to count_n provided in input.json.
        This class ensures that only a fixed number of objects of BeverageBuilder can be used simultaneously
     */
    @PostConstruct
    public void initialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(resourceLoader.getFile()).get("machine").get("outlets").get("count_n");
        numberOfOutlets=mapper.convertValue(jsonNode, Integer.class);
        for(int i=0; i<numberOfOutlets; i++) {
            outletPool.add(new BeverageBuilder(ingredientsInventory, beverageRegistry, 3000));
        }
    }

    /*
        BeverageBuilder's object will be returned only if number of active outlets < N (count_n), null otherwise
     */
    public BeverageBuilder borrowObject() {
        BeverageBuilder beverageBuilder = outletPool.poll();
        if (beverageBuilder == null) {
            System.out.println("All "+ numberOfOutlets+ " outlets are already in use. Please try after some time");
        }
        return beverageBuilder;
    }

    /*
        After borrowing the BeverageBuilder object from pool and getting the required drink, object should be
        returned to pool so that it can be used by others
     */
    public void returnObject(BeverageBuilder beverageBuilder) {
        if (beverageBuilder != null) {
            outletPool.offer(beverageBuilder);
        }
    }
}
