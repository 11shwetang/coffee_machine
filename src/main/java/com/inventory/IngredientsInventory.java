package com.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class IngredientsInventory {

    @Value("input.json")
    Resource resourceLoader;

    /*
        Data structure to hold inventory of all the available items in stock
        It is assumed that refill()/addItem() methods will not be invoked while outlets are operational
     */
    Map<String, Integer> ingredientStock = new HashMap<>();

    /*
        Default value of this property is set to 0.
        This value can be changed by calling setIndicatorThreshold() method
     */
    Integer indicatorThreshold;

    @Autowired
    QuantityIndicator quantityIndicator;

    /*
        At the time of initialization, machine will be filled with ingredients provided in input json
        machine -> total_items_quantity
     */
    @PostConstruct
    public void initialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(resourceLoader.getFile()).get("machine").get("total_items_quantity");
        ingredientStock=mapper.convertValue(jsonNode,Map.class);
        indicatorThreshold = 0;
    }

    public void setIndicatorThreshold(Integer newThreshold) {
        Integer oldThreshold = this.indicatorThreshold;
        this.indicatorThreshold = newThreshold;
        ingredientStock.forEach((item, quantity) -> {
            /* newThreshold > oldThreshold case */
            if((quantity < newThreshold) && (quantity >= oldThreshold)) {
                quantityIndicator.notifyShortage(item);
            }
            /* newThreshold > oldThreshold case */
            if((quantity < oldThreshold) && (quantity >= newThreshold)) {
                quantityIndicator.notifyAvailability(item);
            }
        });
    }


    public Integer getQuantity(String item) {
        return ingredientStock.getOrDefault(item, 0);
    }

    /*
        To be called for refilling any pre-existing ingredient in the machine
     */
    public void refill(String item, Integer quantity) {
        if(ingredientStock.containsKey(item)) {
            Integer oldQuantity = getQuantity(item);
            Integer newQuantity = oldQuantity + quantity;
            if ((oldQuantity < indicatorThreshold) && (newQuantity >= indicatorThreshold)) {
                quantityIndicator.notifyAvailability(item);
            }
            ingredientStock.replace(item, newQuantity);
        } else {
            System.out.println(item + " cannot be refilled");
        }
    }

    /*
        To be called for adding any new ingredient to the machine
        This routine provides flexibility to add new ingredient at runtime.
     */
    public void addNewItem(String item, Integer quantity) {
        ingredientStock.put(item, quantity);
    }

    /*
        This method is called for getting ingredient from stock.
        It also configures indicator to include item for sending the alert if item quantity comes below required threshold.
        negative return value means request cannot be fulfilled, as required quantity is not available
     */
    private Integer getItem(String item, Integer quantity) {
        Integer oldQuantity = getQuantity(item);
        int newQuantity = oldQuantity - quantity;
        if ((oldQuantity >= indicatorThreshold) && (newQuantity < indicatorThreshold)) {
            /* pass this information to indicator i.e. item quantity is running low */
            quantityIndicator.notifyShortage(item);
        }
        ingredientStock.replace(item, newQuantity);
        return newQuantity;
    }

    /*
        returns List of unavailable items if beverage cannot be prepared, otherwise reduces quantity of required ingredients
        synchronized: because this method can be accessed in parallel from multiple threads
     */
    public synchronized List<String> getIngredients(ImmutableMap<String, Integer> beverageIngredients) {
        List<String> unavailableItemsList = new ArrayList<>();
        beverageIngredients.forEach((ingredient, quantity) ->{
            if (getQuantity(ingredient) < quantity) {
                unavailableItemsList.add(ingredient);
            }
        });
        if(unavailableItemsList.size() == 0) {
            /* All required ingredients are available */
            beverageIngredients.forEach((ingredient, quantity) ->{
                getItem(ingredient, quantity);
            });
        }
        return unavailableItemsList;
    }

    /* For testing purpose */
    public void printAvailableIngredientsQuantity() {
        ingredientStock.forEach((ingredient, quantity) -> System.out.println(ingredient+" : "+ quantity));
    }
}
