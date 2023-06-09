/**
 * This software is provided under the terms of the Minecraft Forge Public
 * License v1.0.
 */

package forge;

import Block;
import Item;
import ModLoader;
import RenderBlocks;

public class MinecraftForgeClient
{
    /**
     * Registers a new block highlight handler.
     */
    public static void registerHighlightHandler(IHighlightHandler handler)
    {
        ForgeHooksClient.highlightHandlers.add(handler);
    }

    /** Register a new render context handler.  A render context is a block
     * of rendering performed with similar OpenGL modes, for example,
     * texture name.
     * @param texture The name of the texture for this render context.
     * @param subid The subid of this render context.  0 is the default pass
     * for normal rendering, higher subids render later.  All subids of 0
     * will render before all subids of 1, etc.
     * @param handler The handler to register.
     */
    public static void registerRenderContextHandler(String texture, int subid, IRenderContextHandler handler)
    {
        ForgeHooksClient.registerRenderContextHandler(texture, subid, handler);
    }

    /** Bind a texture.  This is used to bind a texture file when
     * performing your own rendering, rather than using ITextureProvider.
     *
     * This variation is reserved for future expansion.
     */
    public static void bindTexture(String texture, int subid)
    {
        ForgeHooksClient.bindTexture(texture, subid);
    }

    /** Bind a texture.  This is used to bind a texture file when
     * performing your own rendering, rather than using ITextureProvider.
     */
    public static void bindTexture(String texture)
    {
        ForgeHooksClient.bindTexture(texture, 0);
    }

    /** Unbind a texture.  This binds the default texture, when you are
     * finished performing custom rendering.
     */
    public static void unbindTexture()
    {
        ForgeHooksClient.unbindTexture();
    }

    /** Preload a texture.  Textures must be preloaded before the first
     * use, or they will cause visual anomalies.
     */
    public static void preloadTexture(String texture)
    {
        ModLoader.getMinecraftInstance().renderEngine.getTexture(texture);
    }

    /** Render a block.  Render a block which may have a custom texture.
     */
    public static void renderBlock(RenderBlocks render, Block block, int X, int Y, int Z)
    {
        ForgeHooksClient.beforeBlockRender(block, render);
        render.renderBlockByRenderType(block, X, Y, Z);
        ForgeHooksClient.afterBlockRender(block, render);
    }

    /** Get the current render pass.
     */
    public static int getRenderPass()
    {
        return ForgeHooksClient.renderPass;
    }

    private static ICustomItemRenderer[] customItemRenderers = new ICustomItemRenderer[Item.itemsList.length];

    public static void registerCustomItemRenderer(int itemID, ICustomItemRenderer renderer)
    {
        customItemRenderers[itemID] = renderer;
    }

    public static ICustomItemRenderer getCustomItemRenderer (int itemID)
    {
        return customItemRenderers[itemID];
    }

    private static boolean hasInit = false;
    public static void init()
    {
        if (hasInit)
        {
            return;
        }
        hasInit = true;
        ForgeHooks.setPacketHandler(new PacketHandlerClient());
    }

    static
    {
        init();
    }
}
