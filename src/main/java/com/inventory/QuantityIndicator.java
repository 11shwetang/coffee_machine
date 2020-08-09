package com.inventory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class QuantityIndicator {

    /*
        This list will be modified only during refill()/getIngredients() call.
        Refill() is assumed to be call from a single thread, which machine is not in use.
        getIngredients() method is thread safe
     */
    List<String> shortageList = new ArrayList<>();

    /*
        This method is invoked when quantity of any ingredient goes below required threshold
     */
    public void notifyShortage(String item) {
        shortageList.add(item);
        System.out.println(item + " quantity is running low");
    }

    /*
        This method is called when an item whose quantity was below the required threshold is added back to the stock
     */
    public void notifyAvailability(String item) {
        shortageList.remove(item);
        System.out.println(item + " is back in stock");
    }

    /*
        This routine will periodically send alerts, in case some ingredients are not available
     */
    @Scheduled(fixedDelay = 2000)
    void sendAlert() {
        System.out.println(shortageList.size());
        if (shortageList.size() > 0) {
            System.out.println("Following items' quantity is running low : ");
            shortageList.forEach(System.out::println);
        }
    }
}
