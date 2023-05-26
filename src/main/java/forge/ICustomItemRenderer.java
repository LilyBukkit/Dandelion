package forge;

import io.github.lilybukkit.dandelion.compat.RenderBlocks;

public interface ICustomItemRenderer
{
    public void renderInventory(RenderBlocks render, int itemID, int metadata);
}
