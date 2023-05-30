/**
 * This software is provided under the terms of the Minecraft Forge Public
 * License v1.0.
 */
package forge;

import EntityPlayer;
import ItemStack;
import World;

public interface IArrowLooseHandler
{
    /**
     * This is called before a bow tries to shoot an arrow. If it
     * returns a true result, then the normal arrow will not be shot.
     *
     * @param itemstack The ItemStack for the bow doing the firing
     * @param world The current world
     * @param player The player that is firing the bow
     * @param heldTime The amount of ticks the bow was held ready.
     * @return True if the event should be canceled.
     */
    public boolean onArrowLoose(ItemStack itemstack, World world, EntityPlayer player, int heldTime);
}
