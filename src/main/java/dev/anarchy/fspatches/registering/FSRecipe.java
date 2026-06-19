package dev.anarchy.fspatches.registering;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

// Minecraft's crafting system is dumb as fuck.
public class FSRecipe {
    public ItemStack[][] recipe = {
        {null, null, null},
        {null, null, null},
        {null, null, null},
    };

    public FSRecipe setRecipe(ItemStack[][] newRecipe) {
        recipe = newRecipe;
        return this;
    }

    public Object[] toMinecraft() {
        if (recipe.length != 3)
            throw new RuntimeException("a recipe is malformed!");
        for (int i = 0; i < 3; i++)
            if (recipe[i].length != 3)
                throw new RuntimeException("a recipe is malformed!");

        List<ItemStack> seen = new ArrayList<>();
        char[][] grid = new char[3][3];
        char[] letters = "abcdefghijklmnopqrstuvwxyz".toCharArray();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                ItemStack stack = recipe[row][col];
                if (stack == null)
                    grid[row][col] = ' ';
                else {
                    int i = -1;
                    for (int k = 0; k < seen.size(); k++) {
                        ItemStack s = seen.get(k);
                        if (s.getItem() == stack.getItem() && s.getItemDamage() == stack.getItemDamage()) {
                            i = k;
                            break;
                        }
                    }
                    if (i == -1) {
                        i = seen.size();
                        seen.add(stack);
                    }
                    grid[row][col] = letters[i];
                }
            }
        }

        List<Object> list = new ArrayList<>();

        list.add(new String(grid[0]));
        list.add(new String(grid[1]));
        list.add(new String(grid[2]));

        for (int k = 0; k < seen.size(); k++) {
            list.add(letters[k]);
            list.add(seen.get(k));
        }

        return list.toArray();
    }

}
