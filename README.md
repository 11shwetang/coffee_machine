# coffee_machine

Design Considerations:

* In order to allow coffee machine to dispense different type of drinks with different compositions (not known beforehand), a registry of supported beverages is used insted of  hard coded model classes for each type of beverage. This provides flexibility to add different types of beverages.
* Beverage Registry(Singleton) maintains beverage -> [{ingredient, quantity}] list mappinngs. This is initialized during post-construct phase of the bean by parsing input.json file.

    hot_tea -> 	hot_milk 	: 200
		   	    hot_water	: 200
		   	    ginger_syrup: 50
		   	    sugar_syrup	: 50

    rose_tea -> hot_water	: 300
			      ginger_syrup: 20
			      rose_syrup	: 50
			      sugar_syrup	: 50
            
* Ingredients Inventory is used for keeping track of available quantity of ingredients. It is initialized during post-construct phase of the bean by parsing input.json file.  This can be accessed by all N outlets for preparing beverages.Beverage will be prepared only if all the ingredients required(for that particular beverage) are available in the stock.
    
    hot_water	:	  500
    hot_milk	: 	500
    ginger_syrup:	100
    sugar_syrup	: 200

* Composition of beverages can be configured at runtime. We needn't know about the attributes at the time of writing classes.

* In order to allow only N outlets to be used simultaneously an object pool of BeverageBuilder instances is used. For requesting tea/coffee user needs to get an instance of BeverageBuilder from object pool i.e. BeverageBuilderPool(Singleton), and it should be returned back to object pool once tea/coffee is ready. At any point of time, this pool will not allow no of active objects count to be greater than N (configured during initialization).

* New beverages can be added at runtime by calling addBeverage() method of BeverageRegistry class.

* Ingredients can be refilled by calling refill() routine of IngredientsInventory class. For adding new ingredient addItem() method can be used.

* QuantityIndicator class notifies about shortage of any ingredient. If quantity goes below a certain threshold, method of QuantityIndicator class is invoked. A list of ingredient whose quantity is below threshold is maintained. Once an item is back in stock, it is removed from that list.

* Threshold can be configured by calling setIndicatorThreshold() routine of IngredientsInventory() method. It is assumed that threshold will be same for all ingredients.
