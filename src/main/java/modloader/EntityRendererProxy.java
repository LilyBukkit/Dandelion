package modloader;

import net.minecraft.client.Minecraft;

public class EntityRendererProxy extends EntityRenderer
{
    private Minecraft game;

    public EntityRendererProxy(Minecraft minecraft)
    {
        super(minecraft);
        game = minecraft;
    }

    public void updateCameraAndRender(float f)
    {
        super.updateCameraAndRender(f);
        ModLoader.OnTick(f, game);
    }
}
