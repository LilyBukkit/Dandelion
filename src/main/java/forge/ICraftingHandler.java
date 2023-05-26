/**
 * This software is provided under the terms of the Minecraft Forge Public
 * License v1.0.
 */

package forge;

import io.github.lilybukkit.dandelion.compat.IInventory;
import io.github.lilybukkit.dandelion.compat.EntityPlayer;
import io.github.lilybukkit.dandelion.compat.ItemStack;

public interface ICraftingHandler
{
    /**
     * Called after an item is taken from crafting.
     */
    public void onTakenFromCrafting(EntityPlayer player, ItemStack stack, IInventory craftMatrix);
}
