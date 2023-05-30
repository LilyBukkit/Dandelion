/**
 * This software is provided under the terms of the Minecraft Forge Public
 * License v1.0.
 */

package forge;

import IInventory;
import EntityPlayer;
import ItemStack;

public interface ICraftingHandler
{
    /**
     * Called after an item is taken from crafting.
     */
    public void onTakenFromCrafting(EntityPlayer player, ItemStack stack, IInventory craftMatrix);
}
