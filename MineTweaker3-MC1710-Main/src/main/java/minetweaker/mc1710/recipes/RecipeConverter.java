/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package minetweaker.mc1710.recipes;

import minetweaker.api.recipes.ShapedRecipe;
import minetweaker.api.recipes.ShapelessRecipe;
import java.util.ArrayList;
import java.util.Arrays;
import minetweaker.api.item.IIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

/**
 *
 * @author Stan
 */
public class RecipeConverter {
	private RecipeConverter() {}
	
	public static final int TYPE_ADVANCED = 0;
	public static final int TYPE_ORE = 1;
	public static final int TYPE_BASIC = 2;
	
	private static int getIngredientType(IIngredient pattern) {
		Object internal = pattern.getInternal();
		if (internal == null) {
			return TYPE_ADVANCED;
		} else if (internal instanceof ItemStack) {
			return TYPE_BASIC;
		} else {
			return TYPE_ORE;
		}
	}
	
	private static int getRecipeType(IIngredient[] ingredients) {
		int type = TYPE_BASIC;
		for (IIngredient ingredient : ingredients) {
			type = Math.min(type, getIngredientType(ingredient));
		}
		return type;
	}
	
	public static IRecipe convert(ShapelessRecipe recipe) {
		IIngredient[] ingredients = recipe.getIngredients();
		int type = getRecipeType(ingredients);
		
		if (type == TYPE_BASIC) {
			ItemStack[] items = new ItemStack[ingredients.length];
			for (int i = 0; i < ingredients.length; i++) {
				items[i] = (ItemStack) ingredients[i].getInternal();
			}
			return new ShapelessRecipeBasic(items, recipe);
		} else if (type == TYPE_ORE) {
			Object[] items = new Object[ingredients.length];
			for (int i = 0; i < ingredients.length; i++) {
				items[i] = ingredients[i].getInternal();
			}
			return new ShapelessRecipeOre(items, recipe);
		} else {
			return new ShapelessRecipeAdvanced(recipe);
		}
	}
	
	public static IRecipe convert(ShapedRecipe recipe) {
		IIngredient[] ingredients = recipe.getIngredients();
		byte[] posx = recipe.getIngredientsX();
		byte[] posy = recipe.getIngredientsY();
		
		// determine recipe type
		int type = getRecipeType(ingredients);
		
		// construct recipe
		if (type == TYPE_BASIC) {
			System.out.println("Converted to basic recipe");
			ItemStack[] basicIngredients = new ItemStack[recipe.getHeight() * recipe.getWidth()];
			for (int i = 0; i < ingredients.length; i++) {
				basicIngredients[posx[i] + posy[i] * recipe.getWidth()] = (ItemStack) ingredients[i].getInternal();
			}
			
			return new ShapedRecipeBasic(basicIngredients, recipe);
		} else if (type == TYPE_ORE) {
			System.out.println("Converted to ore recipe");
			Object[] converted = new Object[recipe.getHeight() * recipe.getWidth()];
			for (int i = 0; i < ingredients.length; i++) {
				converted[posx[i] + posy[i] * recipe.getWidth()] = ingredients[i].getInternal();
			}
			
			// arguments contents:
			// 1) recipe patterns
			// 2) characters + ingredients
			
			int counter = 0;
			String[] parts = new String[recipe.getHeight()];
			ArrayList rarguments = new ArrayList();
			for (int i = 0; i < recipe.getHeight(); i++) {
				char[] pattern = new char[recipe.getWidth()];
				for (int j = 0; j < recipe.getWidth(); j++) {
					int off = i * recipe.getWidth() + j;
					if (converted[off] == null) {
						pattern[j] = ' ';
					} else {
						pattern[j] = (char) ('A' + counter);
						rarguments.add(pattern[j]);
						rarguments.add(converted[off]);
						counter++;
					}
				}
				parts[i] = new String(pattern);
			}
			
			rarguments.addAll(0, Arrays.asList(parts));
			
			return new ShapedRecipeOre(rarguments.toArray(), recipe);
		} else {
			System.out.println("Converted to advanced recipe");
			return new ShapedRecipeAdvanced(recipe);
		}
	}
}