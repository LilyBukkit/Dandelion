/**
 * This software is provided under the terms of the Minecraft Forge Public
 * License v1.0.
 */

package forge;

import io.github.lilybukkit.dandelion.compat.World;
import io.github.lilybukkit.dandelion.compat.ItemStack;
import io.github.lilybukkit.dandelion.compat.EntityPlayer;

public interface IHoeHandler
{
    /** Called when a hoe is used on a block.  This is called on both sides
     * in SMP.
     * @return true to consume a use of the hoe and return.
     */
    public boolean onUseHoe(ItemStack hoe, EntityPlayer player, World world, int X, int Y, int Z);
}

