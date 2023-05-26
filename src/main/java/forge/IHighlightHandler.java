/*
 * This software is provided under the terms of the Minecraft Forge Public
 * License v1.0.
 */
package forge;

import io.github.lilybukkit.dandelion.compat.EntityPlayer;
import io.github.lilybukkit.dandelion.compat.EnumStatus;
import io.github.lilybukkit.dandelion.compat.RenderGlobal;
import io.github.lilybukkit.dandelion.compat.EntityPlayer;
import io.github.lilybukkit.dandelion.compat.MovingObjectPosition;
import io.github.lilybukkit.dandelion.compat.ItemStack;

public interface IHighlightHandler
{
    /**
     * Allow custom handling of highlights.  Return true if the highlight has
     * been handled.
     */
    public boolean onBlockHighlight(RenderGlobal render, EntityPlayer player, MovingObjectPosition target, int i, ItemStack currentItem, float partialTicks);
}

