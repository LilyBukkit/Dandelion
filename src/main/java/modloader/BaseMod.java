package modloader;

import java.util.Map;
import java.util.Random;

import io.github.lilybukkit.dandelion.compat.Block;
import io.github.lilybukkit.dandelion.compat.IBlockAccess;
import io.github.lilybukkit.dandelion.compat.IInventory;
import net.buj.rml.RosepadMod;
import net.buj.rml.world.ItemStack;
import io.github.lilybukkit.dandelion.compat.Minecraft;

public abstract class BaseMod extends RosepadMod
{
    public BaseMod()
    {
    }

    public int AddFuel(int i, int j)
    {
        return 0;
    }

    public void AddRenderer(Map map)
    {
    }

    public boolean DispenseEntity(World world, double d, double d1, double d2,
            int i, int j, ItemStack itemstack)
    {
        return false;
    }

    public void GenerateSurface(World world, Random random, int i, int j)
    {
    }

    public String getName()
    {
        return getClass().getSimpleName();
    }

    public String getPriorities()
    {
        return "";
    }

    public abstract String getVersion();

    public void KeyboardEvent(KeyBinding keybinding)
    {
    }

    public abstract void load();

    public void ModsLoaded()
    {
    }

    public void OnItemPickup(EntityPlayer entityplayer, ItemStack itemstack)
    {
    }

    public boolean OnTickInGame(float f, Minecraft minecraft)
    {
        return false;
    }

    public boolean OnTickInGUI(float f, Minecraft minecraft, GuiScreen guiscreen)
    {
        return false;
    }

    public void RegisterAnimation(Minecraft minecraft)
    {
    }

    public void RenderInvBlock(RenderBlocks renderblocks, Block block, int i, int j)
    {
    }

    public boolean RenderWorldBlock(RenderBlocks renderblocks, IBlockAccess iblockaccess, int i, int j, int k, Block block, int l)
    {
        return false;
    }

    public void TakenFromCrafting(EntityPlayer entityplayer, ItemStack itemstack, IInventory iinventory)
    {
    }

    public void TakenFromFurnace(EntityPlayer entityplayer, ItemStack itemstack)
    {
    }

    public String toString()
    {
        return String.valueOf(getName()) + ' ' + getVersion();
    }
}
