/**
 * This software is provided under the terms of the Minecraft Forge Public
 * License v1.0.
 */

package forge;

import io.github.lilybukkit.dandelion.compat.ItemStack;
import io.github.lilybukkit.dandelion.compat.EntityPlayer;

public interface IDestroyToolHandler
{
    /** Called when the user's currently equipped item is destroyed.
     */
    public void onDestroyCurrentItem(EntityPlayer player, ItemStack orig);
}

