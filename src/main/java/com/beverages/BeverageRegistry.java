package com.beverages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class BeverageRegistry {

    /*
        Data Structure to store beverage -> {ingredients, quantity} mappings
        It is assumed that for adding new type of beverage, addBeverage() method can be called only from a single thread,
        while outlets are not operational.
    */
    Map<String, Map<String, Integer>> beverageRegistry;

    @Value("input.json")
    Resource resourceLoader;

    @Autowired
    BeverageRegistry() {
        beverageRegistry = new HashMap<>();
    }

    /*
        Initial set of beverages will be provided in input.json file
        New beverages can be added to machine by calling addBeverage() method
    */
    @PostConstruct
    void initialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(resourceLoader.getFile()).get("machine").get("beverages");
        beverageRegistry=mapper.convertValue(jsonNode,Map.class);
        System.out.println(beverageRegistry);
    }

    /*
        This method provides flexibility to add new type of beverage at run-time.
    */
    public void addBeverage(String beverageName, Map<String, Integer> ingredients) {
        beverageRegistry.put(beverageName, ingredients);
    }

    /*
        Method to return <ingredient, quantity> mappings required for preparing beverage with name "beverage name"
    */
    public ImmutableMap<String, Integer> getBeverageInfo(String beverageName) {
        if (beverageRegistry.containsKey(beverageName)) {
            return ImmutableMap.copyOf(beverageRegistry.get(beverageName));
        }
        return null;
    }
}
