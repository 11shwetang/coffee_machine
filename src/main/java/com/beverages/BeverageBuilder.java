package com.beverages;

import com.google.common.collect.ImmutableMap;
import com.inventory.IngredientsInventory;
import java.util.List;

public class BeverageBuilder {

    IngredientsInventory ingredientsInventory;

    BeverageRegistry beverageRegistry;

    Integer preparationTime;

    BeverageBuilder(IngredientsInventory ingredientsInventory, BeverageRegistry beverageRegistry, Integer preparationTime) {
        this.ingredientsInventory = ingredientsInventory;
        this.beverageRegistry = beverageRegistry;
        this.preparationTime = preparationTime;
    }

    /* To be called for beverages having all ingredients available */
    private void prepareBeverage(String beverageName, ImmutableMap<String, Integer> beverageIngredients) throws InterruptedException{
        Thread.sleep(preparationTime);
        System.out.println(beverageName + " is prepared");
    }


    public boolean buildBeverage(String beverageName) throws InterruptedException{
        ImmutableMap<String, Integer> beverageIngredients = beverageRegistry.getBeverageInfo(beverageName);
        if(beverageIngredients != null) {
            List<String> unavailableItemsList = ingredientsInventory.getIngredients(beverageIngredients);
            if (unavailableItemsList.size() == 0) {
                prepareBeverage(beverageName, beverageIngredients);
                return true;
            } else {
                System.out.println(beverageName + " cannot be prepared because following items are not sufficient/available : ");
                System.out.println(unavailableItemsList.toString());
                return false;
            }
        } else {
            System.out.println("Unsupported beverage type : " + beverageName);
            return false;
        }
    }
}
