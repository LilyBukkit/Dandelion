package forge;

import RenderBlocks;

public interface ICustomItemRenderer
{
    public void renderInventory(RenderBlocks render, int itemID, int metadata);
}
