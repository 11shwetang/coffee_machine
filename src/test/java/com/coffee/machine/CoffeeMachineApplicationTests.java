package com.coffee.machine;

import com.beverages.BeverageBuilder;
import com.beverages.BeverageBuilderPool;
import com.beverages.BeverageRegistry;
import com.inventory.IngredientsInventory;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
class CoffeeMachineApplicationTests {

	@Autowired
	BeverageBuilderPool beverageBuilderPool;

	@Autowired
	IngredientsInventory ingredientsInventory;

	@Autowired
	BeverageRegistry beverageRegistry;

	void initializeIngredientsInventory() throws IOException {
		ingredientsInventory.initialize();
	}

	@Test
	void maxNOutletsShouldOperateSimultaneously() throws InterruptedException, IOException {
		initializeIngredientsInventory();
		BeverageBuilder [] beverageBuilder = new BeverageBuilder[4];
		int unavailableCount = 0;
		for(int i = 0 ; i< 4 ; i++) {
			beverageBuilder[i] = beverageBuilderPool.borrowObject();
			if (beverageBuilder[i] == null) {
				unavailableCount++;
			} else {
				beverageBuilder[i].buildBeverage("hot_tea");
			}
		}
		/* 4th request should return null, as no outlet is available */
		Assert.assertEquals(1, unavailableCount);

		beverageBuilderPool.returnObject(beverageBuilder[0]);
		beverageBuilder[0] = beverageBuilderPool.borrowObject();
		if (beverageBuilder[0] == null) {
			unavailableCount++;
		} else {
			beverageBuilder[0].buildBeverage("green_tea");
		}
		/* This request should succeed because one outlet object is returned to the pool */
		Assert.assertEquals(1, unavailableCount);

		/* Returning back all the borrowed objects to pool */
		for(int i=0 ; i<4; i++) {
			beverageBuilderPool.returnObject(beverageBuilder[i]);
		}
	}

	@Test
	void beveragesShouldBePreparedIfAllIngredientsAvailable() throws InterruptedException, IOException {
		initializeIngredientsInventory();
		boolean isHotTeaReady, isHotCoffeeReady;
		BeverageBuilder outlet_1 = beverageBuilderPool.borrowObject();
		BeverageBuilder outlet_2 = beverageBuilderPool.borrowObject();

		isHotTeaReady = outlet_1.buildBeverage("hot_tea");
		isHotCoffeeReady = outlet_2.buildBeverage("hot_coffee");

		beverageBuilderPool.returnObject(outlet_1);
		beverageBuilderPool.returnObject(outlet_2);

		Assert.assertTrue(isHotTeaReady & isHotCoffeeReady);
	}

	@Test
	void beverageShouldNotBePreparedIfAllIngredientsNotAvailable() throws InterruptedException, IOException {
		initializeIngredientsInventory();
		boolean  isFirstCoffeeReady, isSecondCoffeeReady;
		BeverageBuilder outlet_1 = beverageBuilderPool.borrowObject();
		BeverageBuilder outlet_2 = beverageBuilderPool.borrowObject();

		isFirstCoffeeReady = outlet_1.buildBeverage("hot_coffee");
		isSecondCoffeeReady = outlet_2.buildBeverage("hot_coffee");

		beverageBuilderPool.returnObject(outlet_1);
		beverageBuilderPool.returnObject(outlet_2);

		Assert.assertTrue(isFirstCoffeeReady);
		Assert.assertFalse(isSecondCoffeeReady);
	}

	@Test
	void beverageShouldBePreparedAfterRefillingIngredient() throws InterruptedException, IOException {
		initializeIngredientsInventory();
		boolean  isFirstCoffeeReady, isSecondCoffeeReady;
		BeverageBuilder outlet_1 = beverageBuilderPool.borrowObject();
		BeverageBuilder outlet_2 = beverageBuilderPool.borrowObject();

		isFirstCoffeeReady = outlet_1.buildBeverage("hot_coffee");
		isSecondCoffeeReady = outlet_2.buildBeverage("hot_coffee");

		beverageBuilderPool.returnObject(outlet_1);
		beverageBuilderPool.returnObject(outlet_2);

		Assert.assertTrue(isFirstCoffeeReady);
		Assert.assertFalse(isSecondCoffeeReady);

		/* Refill hot_milk in the machine */
		ingredientsInventory.refill("hot_milk", 500);

		outlet_2 = beverageBuilderPool.borrowObject();
		isSecondCoffeeReady = outlet_2.buildBeverage("hot_coffee");
		beverageBuilderPool.returnObject(outlet_1);

		Assert.assertTrue(isSecondCoffeeReady);
	}

	@Test
	void unsupportedBeverageTypeShouldNotBePrepared() throws InterruptedException, IOException {
		initializeIngredientsInventory();
		BeverageBuilder outlet = beverageBuilderPool.borrowObject();
		boolean isColdCoffeeReady = outlet.buildBeverage("cold_coffee");
		beverageBuilderPool.returnObject(outlet);

		Assert.assertFalse(isColdCoffeeReady);
	}

	@Test
	void newBeverageTypeShouldBePreparedAfterConfiguring() throws InterruptedException, IOException {
		initializeIngredientsInventory();
		BeverageBuilder outlet = beverageBuilderPool.borrowObject();
		boolean isNewBeverageReady = outlet.buildBeverage("new_beverage");
		Assert.assertFalse(isNewBeverageReady);

		Map<String, Integer> newBeverageComposition = new HashMap<>();
		newBeverageComposition.put("hot_water", 100);
		newBeverageComposition.put("hot_milk", 200);
		newBeverageComposition.put("ginger_syrup", 50);
		beverageRegistry.addBeverage("new_beverage", newBeverageComposition);
		isNewBeverageReady = outlet.buildBeverage("new_beverage");
		beverageBuilderPool.returnObject(outlet);

		Assert.assertTrue(isNewBeverageReady);
	}

	@Test
	void greenTeaShouldBePreparedAfterAddingGreenMixtureAtRuntime() throws IOException, InterruptedException {
		initializeIngredientsInventory();
		BeverageBuilder outlet = beverageBuilderPool.borrowObject();
		ingredientsInventory.addNewItem("green_mixture", 100);
		boolean isGreenTeaReady = outlet.buildBeverage("green_tea");
		beverageBuilderPool.returnObject(outlet);

		Assert.assertTrue(isGreenTeaReady);
	}

	@Test
	void notificationShouldBeSentWhenQuantityCrossesThreshold() throws IOException, InterruptedException {
		initializeIngredientsInventory();
		ingredientsInventory.setIndicatorThreshold(5);
		boolean isHotTeaReady, isHotCoffeeReady;
		BeverageBuilder outlet_1 = beverageBuilderPool.borrowObject();
		BeverageBuilder outlet_2 = beverageBuilderPool.borrowObject();

		isHotTeaReady = outlet_1.buildBeverage("hot_tea");
		isHotCoffeeReady = outlet_2.buildBeverage("hot_coffee");

		beverageBuilderPool.returnObject(outlet_1);
		beverageBuilderPool.returnObject(outlet_2);

		Assert.assertTrue(isHotCoffeeReady & isHotTeaReady);

		ingredientsInventory.refill("hot_milk", 500);
		ingredientsInventory.refill("sugar_syrup", 50);
		isHotCoffeeReady = outlet_2.buildBeverage("hot_coffee");
		beverageBuilderPool.returnObject(outlet_2);

		Assert.assertTrue(isHotCoffeeReady);
	}
}
