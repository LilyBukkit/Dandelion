/**
 * This software is provided under the terms of the Minecraft Forge Public
 * License v1.0.
 */

package forge;

import io.github.lilybukkit.dandelion.compat.EntityPlayer;
import io.github.lilybukkit.dandelion.compat.EnumStatus;

public interface ISleepHandler
{
    /**
     * This is called before a player sleeps in a bed.  If it returns a
     * non-null result, then the normal sleeping process will be skipped, and
     * the value returned by this method will be returned to
     * BlockBed.blockActivated.
     *
     * @see MinecraftForge#registerSleepHandler(ISleepHandler)
     */
    public EnumStatus sleepInBedAt(EntityPlayer player, int X, int Y, int Z);
}

