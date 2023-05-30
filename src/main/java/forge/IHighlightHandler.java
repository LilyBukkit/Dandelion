/*
 * This software is provided under the terms of the Minecraft Forge Public
 * License v1.0.
 */
package forge;

import EntityPlayer;
import EnumStatus;
import RenderGlobal;
import EntityPlayer;
import MovingObjectPosition;
import ItemStack;

public interface IHighlightHandler
{
    /**
     * Allow custom handling of highlights.  Return true if the highlight has
     * been handled.
     */
    public boolean onBlockHighlight(RenderGlobal render, EntityPlayer player, MovingObjectPosition target, int i, ItemStack currentItem, float partialTicks);
}

