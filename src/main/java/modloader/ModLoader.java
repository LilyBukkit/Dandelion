package modloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public final class ModLoader
{
    private static final List animList = new LinkedList();
    private static final Map blockModels = new HashMap();
    private static final Map blockSpecialInv = new HashMap();
    private static final File cfgdir;
    private static final File cfgfile;
    public static Level cfgLoggingLevel;
    private static Map classMap = null;
    private static long clock = 0L;
    public static final boolean DEBUG = false;
    private static Field field_animList = null;
    private static Field field_armorList = null;
    private static Field field_modifiers = null;
    private static Field field_TileEntityRenderers = null;
    private static boolean hasInit = false;
    private static int highestEntityId = 3000;
    private static final Map inGameHooks = new HashMap();
    private static final Map inGUIHooks = new HashMap();
    private static Minecraft instance = null;
    private static int itemSpriteIndex = 0;
    private static int itemSpritesLeft = 0;
    private static final Map keyList = new HashMap();
    private static final File logfile = new File(Minecraft.getMinecraftDir(), "ModLoader.txt");
    private static final Logger logger = Logger.getLogger("ModLoader");
    private static FileHandler logHandler = null;
    private static Method method_RegisterEntityID = null;
    private static Method method_RegisterTileEntity = null;
    private static final File modDir = new File(Minecraft.getMinecraftDir(), "/mods/");
    private static final LinkedList<BaseMod> modList = new LinkedList<BaseMod>();
    private static int nextBlockModelID = 1000;
    private static final Map overrides = new HashMap();
    public static final Properties props = new Properties();
    private static int terrainSpriteIndex = 0;
    private static int terrainSpritesLeft = 0;
    private static String texPack = null;
    private static boolean texturesAdded = false;
    private static final boolean usedItemSprites[] = new boolean[256];
    private static final boolean usedTerrainSprites[] = new boolean[256];
    public static final String VERSION = "ModLoader 1.1";
    private static Map localizedStrings = new HashMap();
    private static String langPack = null;

    public static int AddAllFuel(int i, int j)
    {
        logger.finest("Finding fuel for " + i);
        int k = 0;
        for (Iterator iterator = modList.iterator(); iterator.hasNext() && k == 0; k = ((BaseMod)iterator.next()).AddFuel(i, j)) { }
        if (k != 0)
        {
            logger.finest("Returned " + k);
        }
        return k;
    }

    public static void AddAllRenderers(Map map)
    {
        if (!hasInit)
        {
            init();
            logger.fine("Initialized");
        }
        BaseMod basemod;
        for (Iterator iterator = modList.iterator(); iterator.hasNext(); basemod.AddRenderer(map))
        {
            basemod = (BaseMod)iterator.next();
        }
    }

    public static void addAnimation(TextureFX texturefx)
    {
        logger.finest("Adding animation " + texturefx.toString());
        for (Iterator iterator = animList.iterator(); iterator.hasNext();)
        {
            TextureFX texturefx1 = (TextureFX)iterator.next();
            if (texturefx1.iconIndex == texturefx.iconIndex && texturefx1.tileImage == texturefx.tileImage)
            {
                animList.remove(texturefx);
                break;
            }
        }

        animList.add(texturefx);
    }

    public static int AddArmor(String s)
    {
        try
        {
            String as[] = (String[])field_armorList.get(null);
            List list = Arrays.asList(as);
            ArrayList arraylist = new ArrayList();
            arraylist.addAll(list);
            if (!arraylist.contains(s))
            {
                arraylist.add(s);
            }
            int i = arraylist.indexOf(s);
            field_armorList.set(null, ((Object) (arraylist.toArray(new String[0]))));
            return i;
        }
        catch (IllegalArgumentException illegalargumentexception)
        {
            logger.throwing("ModLoader", "AddArmor", illegalargumentexception);
            ThrowException("An impossible error has occured!", illegalargumentexception);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "AddArmor", illegalaccessexception);
            ThrowException("An impossible error has occured!", illegalaccessexception);
        }
        return -1;
    }

    public static void AddLocalization(String s, String s1)
    {
        localizedStrings.put(s, s1);
    }

    private static void addMod(ClassLoader classloader, String s)
    {
        try
        {
            String s1 = s.split("\\.")[0];
            if (s1.contains("$"))
            {
                return;
            }
            if (props.containsKey(s1) && (props.getProperty(s1).equalsIgnoreCase("no") || props.getProperty(s1).equalsIgnoreCase("off")))
            {
                return;
            }
            Package package1 = (modloader.ModLoader.class).getPackage();
            if (package1 != null)
            {
                s1 = String.valueOf(package1.getName()) + "." + s1;
            }
            Class class1 = classloader.loadClass(s1);
            if (!(modloader.BaseMod.class).isAssignableFrom(class1))
            {
                return;
            }
            setupProperties(class1);
            BaseMod basemod = (BaseMod)class1.newInstance();
            if (basemod != null)
            {
                modList.add(basemod);
                logger.fine("Mod Initialized: \"" + basemod.toString() + "\" from " + s);
                System.out.println("Mod Initialized: " + basemod.toString());
            }
        }
        catch (Throwable throwable)
        {
            logger.fine("Failed to load mod from \"" + s + "\"");
            System.out.println("Failed to load mod from \"" + s + "\"");
            logger.throwing("ModLoader", "addMod", throwable);
            ThrowException(throwable);
        }
    }

    public static void AddName(Object obj, String s)
    {
        String s1 = null;
        if (obj instanceof Item)
        {
            Item item = (Item)obj;
            if (item.getItemName() != null)
            {
                s1 = String.valueOf(item.getItemName()) + ".name";
            }
        }
        else if (obj instanceof Block)
        {
            Block block = (Block)obj;
            if (block.getBlockName() != null)
            {
                s1 = String.valueOf(block.getBlockName()) + ".name";
            }
        }
        else if (obj instanceof ItemStack)
        {
            ItemStack itemstack = (ItemStack)obj;
            String s2 = Item.itemsList[itemstack.itemID].getItemNameIS(itemstack);
            if (s2 != null)
            {
                s1 = String.valueOf(s2) + ".name";
            }
        }
        else
        {
            Exception exception = new Exception(String.valueOf(obj.getClass().getName()) + " cannot have name attached to it!");
            logger.throwing("ModLoader", "AddName", exception);
            ThrowException(exception);
        }
        if (s1 != null)
        {
            AddLocalization(s1, s);
        }
        else
        {
            Exception exception1 = new Exception(obj + " is missing name tag!");
            logger.throwing("ModLoader", "AddName", exception1);
            ThrowException(exception1);
        }
    }

    public static int addOverride(String s, String s1)
    {
        try
        {
            int i = getUniqueSpriteIndex(s);
            addOverride(s, s1, i);
            return i;
        }
        catch (Throwable throwable)
        {
            logger.throwing("ModLoader", "addOverride", throwable);
            ThrowException(throwable);
            throw new RuntimeException(throwable);
        }
    }

    public static void addOverride(String s, String s1, int i)
    {
        int j = -1;
        int k = 0;
        if (s.equals("/terrain.png"))
        {
            j = 0;
            k = terrainSpritesLeft;
        }
        else if (s.equals("/gui/items.png"))
        {
            j = 1;
            k = itemSpritesLeft;
        }
        else
        {
            return;
        }
        System.out.println("Overriding " + s + " with " + s1 + " @ " + i + ". " + k + " left.");
        logger.finer("addOverride(" + s + "," + s1 + "," + i + "). " + k + " left.");
        Object obj = (Map)overrides.get(Integer.valueOf(j));
        if (obj == null)
        {
            obj = new HashMap();
            overrides.put(Integer.valueOf(j), obj);
        }
        ((Map) (obj)).put(s1, Integer.valueOf(i));
    }

    public static void AddRecipe(ItemStack itemstack, Object aobj[])
    {
        CraftingManager.getInstance().addRecipe(itemstack, aobj);
    }

    public static void AddShapelessRecipe(ItemStack itemstack, Object aobj[])
    {
        CraftingManager.getInstance().addShapelessRecipe(itemstack, aobj);
    }

    public static void AddSmelting(int i, ItemStack itemstack)
    {
        FurnaceRecipes.smelting().addSmelting(i, itemstack);
    }

    public static void AddSpawn(Class class1, int i, int j, int k, EnumCreatureType enumcreaturetype)
    {
        AddSpawn(class1, i, j, k, enumcreaturetype, null);
    }

    public static void AddSpawn(Class class1, int i, int j, int k, EnumCreatureType enumcreaturetype, BiomeGenBase abiomegenbase[])
    {
        if (class1 == null)
        {
            throw new IllegalArgumentException("entityClass cannot be null");
        }
        if (enumcreaturetype == null)
        {
            throw new IllegalArgumentException("spawnList cannot be null");
        }
        if (abiomegenbase == null)
        {
            abiomegenbase = standardBiomes;
        }
        for (int l = 0; l < abiomegenbase.length; l++)
        {
            List list = abiomegenbase[l].getSpawnableList(enumcreaturetype);
            if (list != null)
            {
                boolean flag = false;
                for (Iterator iterator = list.iterator(); iterator.hasNext();)
                {
                    SpawnListEntry spawnlistentry = (SpawnListEntry)iterator.next();
                    if (spawnlistentry.entityClass == class1)
                    {
                        spawnlistentry.itemWeight = i;
                        spawnlistentry.field_35591_b = j;
                        spawnlistentry.field_35592_c = k;
                        flag = true;
                        break;
                    }
                }

                if (!flag)
                {
                    list.add(new SpawnListEntry(class1, i, j, k));
                }
            }
        }
    }

    public static void AddSpawn(String s, int i, int j, int k, EnumCreatureType enumcreaturetype)
    {
        AddSpawn(s, i, j, k, enumcreaturetype, null);
    }

    public static void AddSpawn(String s, int i, int j, int k, EnumCreatureType enumcreaturetype, BiomeGenBase abiomegenbase[])
    {
        Class class1 = (Class)classMap.get(s);
        if (class1 != null && (io.github.lilybukkit.dandelion.compat.EntityLiving.class).isAssignableFrom(class1))
        {
            AddSpawn(class1, i, j, k, enumcreaturetype, abiomegenbase);
        }
    }

    public static boolean DispenseEntity(World world, double d, double d1, double d2, int i,
            int j, ItemStack itemstack)
    {
        boolean flag = false;
        for (Iterator iterator = modList.iterator(); iterator.hasNext() && !flag; flag = ((BaseMod)iterator.next()).DispenseEntity(world, d, d1, d2, i, j, itemstack)) { }
        return flag;
    }

    public static List<BaseMod> getLoadedMods()
    {
        return Collections.unmodifiableList(modList);
    }

    public static Logger getLogger()
    {
        return logger;
    }

    public static Minecraft getMinecraftInstance()
    {
        if (instance == null)
        {
            try
            {
                ThreadGroup threadgroup = Thread.currentThread().getThreadGroup();
                int i = threadgroup.activeCount();
                Thread athread[] = new Thread[i];
                threadgroup.enumerate(athread);
                for (int j = 0; j < athread.length; j++)
                {
                    System.out.println(athread[j].getName());
                }

                for (int k = 0; k < athread.length; k++)
                {
                    if (!athread[k].getName().equals("Minecraft main thread"))
                    {
                        continue;
                    }
                    instance = (Minecraft)getPrivateValue(java.lang.Thread.class, athread[k], "target");
                    break;
                }
            }
            catch (SecurityException securityexception)
            {
                logger.throwing("ModLoader", "getMinecraftInstance", securityexception);
                throw new RuntimeException(securityexception);
            }
            catch (NoSuchFieldException nosuchfieldexception)
            {
                logger.throwing("ModLoader", "getMinecraftInstance", nosuchfieldexception);
                throw new RuntimeException(nosuchfieldexception);
            }
        }
        return instance;
    }

    public static Object getPrivateValue(Class class1, Object obj, int i)
    throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
            Field field = class1.getDeclaredFields()[i];
            field.setAccessible(true);
            return field.get(obj);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "getPrivateValue", illegalaccessexception);
            ThrowException("An impossible error has occured!", illegalaccessexception);
            return null;
        }
    }

    public static Object getPrivateValue(Class class1, Object obj, String s)
    throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
            Field field = class1.getDeclaredField(s);
            field.setAccessible(true);
            return field.get(obj);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "getPrivateValue", illegalaccessexception);
            ThrowException("An impossible error has occured!", illegalaccessexception);
            return null;
        }
    }

    public static int getUniqueBlockModelID(BaseMod basemod, boolean flag)
    {
        int i = nextBlockModelID++;
        blockModels.put(Integer.valueOf(i), basemod);
        blockSpecialInv.put(Integer.valueOf(i), Boolean.valueOf(flag));
        return i;
    }

    public static int getUniqueEntityId()
    {
        return highestEntityId++;
    }

    private static int getUniqueItemSpriteIndex()
    {
        for (; itemSpriteIndex < usedItemSprites.length; itemSpriteIndex++)
        {
            if (!usedItemSprites[itemSpriteIndex])
            {
                usedItemSprites[itemSpriteIndex] = true;
                itemSpritesLeft--;
                return itemSpriteIndex++;
            }
        }

        Exception exception = new Exception("No more empty item sprite indices left!");
        logger.throwing("ModLoader", "getUniqueItemSpriteIndex", exception);
        ThrowException(exception);
        return 0;
    }

    public static int getUniqueSpriteIndex(String s)
    {
        if (s.equals("/gui/items.png"))
        {
            return getUniqueItemSpriteIndex();
        }
        if (s.equals("/terrain.png"))
        {
            return getUniqueTerrainSpriteIndex();
        }
        else
        {
            Exception exception = new Exception("No registry for this texture: " + s);
            logger.throwing("ModLoader", "getUniqueItemSpriteIndex", exception);
            ThrowException(exception);
            return 0;
        }
    }

    private static int getUniqueTerrainSpriteIndex()
    {
        for (; terrainSpriteIndex < usedTerrainSprites.length; terrainSpriteIndex++)
        {
            if (!usedTerrainSprites[terrainSpriteIndex])
            {
                usedTerrainSprites[terrainSpriteIndex] = true;
                terrainSpritesLeft--;
                return terrainSpriteIndex++;
            }
        }

        Exception exception = new Exception("No more empty terrain sprite indices left!");
        logger.throwing("ModLoader", "getUniqueItemSpriteIndex", exception);
        ThrowException(exception);
        return 0;
    }

    private static void init()
    {
        hasInit = true;
        String s = "1111111111111111111111111111111111111101111111111111111111111111111111111111111111111111111111111111110111111111111111000111111111111101111111110000000101111111000000010100111100000000000000110000000000000000000000000000000000000000000000001111111111111111";
        String s1 = "1111111111111111111111111111110111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111110111111111111110000001111111111000000001111000000000111111000000000001111111000000001111111111111111111";
        for (int i = 0; i < 256; i++)
        {
            usedItemSprites[i] = s.charAt(i) == '1';
            if (!usedItemSprites[i])
            {
                itemSpritesLeft++;
            }
            usedTerrainSprites[i] = s1.charAt(i) == '1';
            if (!usedTerrainSprites[i])
            {
                terrainSpritesLeft++;
            }
        }

        try
        {
            instance = (Minecraft)getPrivateValue(net.minecraft.client.Minecraft.class, null, 1);
            instance.entityRenderer = new EntityRendererProxy(instance);
            classMap = (Map)getPrivateValue(io.github.lilybukkit.dandelion.compat.EntityList.class, null, 0);
            field_modifiers = (java.lang.reflect.Field.class).getDeclaredField("modifiers");
            field_modifiers.setAccessible(true);
            field_TileEntityRenderers = (io.github.lilybukkit.dandelion.compat.TileEntityRenderer.class).getDeclaredFields()[0];
            field_TileEntityRenderers.setAccessible(true);
            field_armorList = (io.github.lilybukkit.dandelion.compat.RenderPlayer.class).getDeclaredFields()[3];
            field_modifiers.setInt(field_armorList, field_armorList.getModifiers() & 0xffffffef);
            field_armorList.setAccessible(true);
            field_animList = (io.github.lilybukkit.dandelion.compat.RenderEngine.class).getDeclaredFields()[6];
            field_animList.setAccessible(true);
            Field afield[] = (io.github.lilybukkit.dandelion.compat.BiomeGenBase.class).getDeclaredFields();
            LinkedList linkedlist = new LinkedList();
            for (int j = 0; j < afield.length; j++)
            {
                Class class1 = afield[j].getType();
                if ((afield[j].getModifiers() & 8) != 0 && class1.isAssignableFrom(io.github.lilybukkit.dandelion.compat.BiomeGenBase.class))
                {
                    BiomeGenBase biomegenbase = (BiomeGenBase)afield[j].get(null);
                    if (!(biomegenbase instanceof BiomeGenHell) && !(biomegenbase instanceof BiomeGenEnd))
                    {
                        linkedlist.add(biomegenbase);
                    }
                }
            }

            standardBiomes = (BiomeGenBase[])linkedlist.toArray(new BiomeGenBase[0]);
            try
            {
                method_RegisterTileEntity = (io.github.lilybukkit.dandelion.compat.TileEntity.class).getDeclaredMethod("a", new Class[]
                        {
                            java.lang.Class.class, java.lang.String.class
                        });
            }
            catch (NoSuchMethodException nosuchmethodexception1)
            {
                method_RegisterTileEntity = (io.github.lilybukkit.dandelion.compat.TileEntity.class).getDeclaredMethod("addMapping", new Class[]
                        {
                            java.lang.Class.class, java.lang.String.class
                        });
            }
            method_RegisterTileEntity.setAccessible(true);
            try
            {
                method_RegisterEntityID = (io.github.lilybukkit.dandelion.compat.EntityList.class).getDeclaredMethod("a", new Class[]
                        {
                            java.lang.Class.class, java.lang.String.class, Integer.TYPE
                        });
            }
            catch (NoSuchMethodException nosuchmethodexception2)
            {
                method_RegisterEntityID = (io.github.lilybukkit.dandelion.compat.EntityList.class).getDeclaredMethod("addMapping", new Class[]
                        {
                            java.lang.Class.class, java.lang.String.class, Integer.TYPE
                        });
            }
            method_RegisterEntityID.setAccessible(true);
        }
        catch (SecurityException securityexception)
        {
            logger.throwing("ModLoader", "init", securityexception);
            ThrowException(securityexception);
            throw new RuntimeException(securityexception);
        }
        catch (NoSuchFieldException nosuchfieldexception)
        {
            logger.throwing("ModLoader", "init", nosuchfieldexception);
            ThrowException(nosuchfieldexception);
            throw new RuntimeException(nosuchfieldexception);
        }
        catch (NoSuchMethodException nosuchmethodexception)
        {
            logger.throwing("ModLoader", "init", nosuchmethodexception);
            ThrowException(nosuchmethodexception);
            throw new RuntimeException(nosuchmethodexception);
        }
        catch (IllegalArgumentException illegalargumentexception)
        {
            logger.throwing("ModLoader", "init", illegalargumentexception);
            ThrowException(illegalargumentexception);
            throw new RuntimeException(illegalargumentexception);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "init", illegalaccessexception);
            ThrowException(illegalaccessexception);
            throw new RuntimeException(illegalaccessexception);
        }
        try
        {
            loadConfig();
            if (props.containsKey("loggingLevel"))
            {
                cfgLoggingLevel = Level.parse(props.getProperty("loggingLevel"));
            }
            if (props.containsKey("grassFix"))
            {
                RenderBlocks.cfgGrassFix = Boolean.parseBoolean(props.getProperty("grassFix"));
            }
            logger.setLevel(cfgLoggingLevel);
            if ((logfile.exists() || logfile.createNewFile()) && logfile.canWrite() && logHandler == null)
            {
                logHandler = new FileHandler(logfile.getPath());
                logHandler.setFormatter(new SimpleFormatter());
                logger.addHandler(logHandler);
            }
            logger.fine("ModLoader 1.1 Initializing...");
            System.out.println("ModLoader 1.1 Initializing...");
            File file = new File((io.github.lilybukkit.dandelion.compat.ModLoader.class).getProtectionDomain().getCodeSource().getLocation().toURI());
            modDir.mkdirs();
            readFromClassPath(file);
            readFromModFolder(modDir);
            sortModList();
            for (Iterator iterator = modList.iterator(); iterator.hasNext();)
            {
                BaseMod basemod = (BaseMod)iterator.next();
                basemod.load();
                logger.fine("Mod Loaded: \"" + basemod.toString() + "\"");
                System.out.println("Mod Loaded: " + basemod.toString());
                if (!props.containsKey(basemod.getClass().getSimpleName()))
                {
                    props.setProperty(basemod.getClass().getSimpleName(), "on");
                }
            }

            BaseMod basemod1;
            for (Iterator iterator1 = modList.iterator(); iterator1.hasNext(); basemod1.ModsLoaded())
            {
                basemod1 = (BaseMod)iterator1.next();
            }

            System.out.println("Done.");
            props.setProperty("loggingLevel", cfgLoggingLevel.getName());
            props.setProperty("grassFix", Boolean.toString(RenderBlocks.cfgGrassFix));
            instance.gameSettings.keyBindings = RegisterAllKeys(instance.gameSettings.keyBindings);
            instance.gameSettings.loadOptions();
            initStats();
            saveConfig();
        }
        catch (Throwable throwable)
        {
            logger.throwing("ModLoader", "init", throwable);
            ThrowException("ModLoader has failed to initialize.", throwable);
            if (logHandler != null)
            {
                logHandler.close();
            }
            throw new RuntimeException(throwable);
        }
    }

    private static void sortModList()
    throws Exception
    {
        HashMap hashmap = new HashMap();
        BaseMod basemod;
        for (Iterator iterator = getLoadedMods().iterator(); iterator.hasNext(); hashmap.put(basemod.getClass().getSimpleName(), basemod))
        {
            basemod = (BaseMod)iterator.next();
        }

        LinkedList linkedlist = new LinkedList();
        for (int i = 0; linkedlist.size() != modList.size(); i++)
        {
            if (i > 10)
            {
                break;
            }
            Iterator iterator1 = modList.iterator();
            label0:
            while (iterator1.hasNext())
            {
                BaseMod basemod1 = (BaseMod)iterator1.next();
                if (linkedlist.contains(basemod1))
                {
                    continue;
                }
                String s = basemod1.getPriorities();
                if (s == null || s.length() == 0 || s.indexOf(':') == -1)
                {
                    linkedlist.add(basemod1);
                    continue;
                }
                if (i <= 0)
                {
                    continue;
                }
                int j = -1;
                int k = 0x80000000;
                int l = 0x7fffffff;
                String as[];
                if (s.indexOf(';') > 0)
                {
                    as = s.split(";");
                }
                else
                {
                    as = (new String[]
                            {
                                s
                            });
                }
                for (int i1 = 0; i1 < as.length; i1++)
                {
                    String s1 = as[i1];
                    if (s1.indexOf(':') == -1)
                    {
                        continue;
                    }
                    String as1[] = s1.split(":");
                    String s2 = as1[0];
                    String s3 = as1[1];
                    if (!s2.contentEquals("required-before") && !s2.contentEquals("before") && !s2.contentEquals("after") && !s2.contentEquals("required-after"))
                    {
                        continue;
                    }
                    if (s3.contentEquals("*"))
                    {
                        if (s2.contentEquals("required-before") || s2.contentEquals("before"))
                        {
                            j = 0;
                        }
                        else if (s2.contentEquals("required-after") || s2.contentEquals("after"))
                        {
                            j = linkedlist.size();
                        }
                        break;
                    }
                    if ((s2.contentEquals("required-before") || s2.contentEquals("required-after")) && !hashmap.containsKey(s3))
                    {
                        throw new Exception(String.format("%s is missing dependency: %s", new Object[]
                                {
                                    basemod1, s3
                                }));
                    }
                    BaseMod basemod2 = (BaseMod)hashmap.get(s3);
                    if (!linkedlist.contains(basemod2))
                    {
                        continue label0;
                    }
                    int j1 = linkedlist.indexOf(basemod2);
                    if (s2.contentEquals("required-before") || s2.contentEquals("before"))
                    {
                        j = j1;
                        if (j < l)
                        {
                            l = j;
                        }
                        else
                        {
                            j = l;
                        }
                    }
                    else if (s2.contentEquals("required-after") || s2.contentEquals("after"))
                    {
                        j = j1 + 1;
                        if (j > k)
                        {
                            k = j;
                        }
                        else
                        {
                            j = k;
                        }
                    }
                }

                if (j != -1)
                {
                    linkedlist.add(j, basemod1);
                }
            }
        }

        modList.clear();
        modList.addAll(linkedlist);
    }

    private static void initStats()
    {
        for (int i = 0; i < Block.blocksList.length; i++)
        {
            if (!StatList.oneShotStats.containsKey(Integer.valueOf(0x1000000 + i)) && Block.blocksList[i] != null && Block.blocksList[i].getEnableStats())
            {
                String s = StringTranslate.getInstance().translateKeyFormat("stat.mineBlock", new Object[]
                        {
                            Block.blocksList[i].translateBlockName()
                        });
                StatList.mineBlockStatArray[i] = (new StatCrafting(0x1000000 + i, s, i)).registerStat();
                StatList.objectMineStats.add(StatList.mineBlockStatArray[i]);
            }
        }

        for (int j = 0; j < Item.itemsList.length; j++)
        {
            if (!StatList.oneShotStats.containsKey(Integer.valueOf(0x1020000 + j)) && Item.itemsList[j] != null)
            {
                String s1 = StringTranslate.getInstance().translateKeyFormat("stat.useItem", new Object[]
                        {
                            Boolean.valueOf(Item.itemsList[j].func_46056_k())
                        });
                StatList.objectUseStats[j] = (new StatCrafting(0x1020000 + j, s1, j)).registerStat();
                if (j >= Block.blocksList.length)
                {
                    StatList.itemStats.add(StatList.objectUseStats[j]);
                }
            }
            if (!StatList.oneShotStats.containsKey(Integer.valueOf(0x1030000 + j)) && Item.itemsList[j] != null && Item.itemsList[j].isDamageable())
            {
                String s2 = StringTranslate.getInstance().translateKeyFormat("stat.breakItem", new Object[]
                        {
                            Boolean.valueOf(Item.itemsList[j].func_46056_k())
                        });
                StatList.objectBreakStats[j] = (new StatCrafting(0x1030000 + j, s2, j)).registerStat();
            }
        }

        HashSet hashset = new HashSet();
        Object obj;
        for (Iterator iterator = CraftingManager.getInstance().getRecipeList().iterator(); iterator.hasNext(); hashset.add(Integer.valueOf(((IRecipe)obj).getRecipeOutput().itemID)))
        {
            obj = iterator.next();
        }

        Object obj1;
        for (Iterator iterator1 = FurnaceRecipes.smelting().getSmeltingList().values().iterator(); iterator1.hasNext(); hashset.add(Integer.valueOf(((ItemStack)obj1).itemID)))
        {
            obj1 = iterator1.next();
        }

        for (Iterator iterator2 = hashset.iterator(); iterator2.hasNext();)
        {
            int k = ((Integer)iterator2.next()).intValue();
            if (!StatList.oneShotStats.containsKey(Integer.valueOf(0x1010000 + k)) && Item.itemsList[k] != null)
            {
                String s3 = StringTranslate.getInstance().translateKeyFormat("stat.craftItem", new Object[]
                        {
                            Boolean.valueOf(Item.itemsList[k].func_46056_k())
                        });
                StatList.objectCraftStats[k] = (new StatCrafting(0x1010000 + k, s3, k)).registerStat();
            }
        }
    }

    public static boolean isGUIOpen(Class class1)
    {
        Minecraft minecraft = getMinecraftInstance();
        if (class1 == null)
        {
            return minecraft.currentScreen == null;
        }
        if (minecraft.currentScreen == null && class1 != null)
        {
            return false;
        }
        else
        {
            return class1.isInstance(minecraft.currentScreen);
        }
    }

    public static boolean isModLoaded(String s)
    {
        Class class1 = null;
        try
        {
            class1 = Class.forName(s);
        }
        catch (ClassNotFoundException classnotfoundexception)
        {
            return false;
        }
        if (class1 != null)
        {
            for (Iterator iterator = modList.iterator(); iterator.hasNext();)
            {
                BaseMod basemod = (BaseMod)iterator.next();
                if (class1.isInstance(basemod))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static void loadConfig()
    throws IOException
    {
        cfgdir.mkdir();
        if (!cfgfile.exists() && !cfgfile.createNewFile())
        {
            return;
        }
        if (cfgfile.canRead())
        {
            FileInputStream fileinputstream = new FileInputStream(cfgfile);
            props.load(fileinputstream);
            fileinputstream.close();
        }
    }

    public static java.awt.image.BufferedImage loadImage(RenderEngine renderengine, String s)
    throws Exception
    {
        TexturePackList texturepacklist = (TexturePackList)getPrivateValue(io.github.lilybukkit.dandelion.compat.RenderEngine.class, renderengine, 11);
        InputStream inputstream = texturepacklist.selectedTexturePack.getResourceAsStream(s);
        if (inputstream == null)
        {
            throw new Exception("Image not found: " + s);
        }
        java.awt.image.BufferedImage bufferedimage = ImageIO.read(inputstream);
        if (bufferedimage == null)
        {
            throw new Exception("Image corrupted: " + s);
        }
        else
        {
            return bufferedimage;
        }
    }

    public static void OnItemPickup(EntityPlayer entityplayer, ItemStack itemstack)
    {
        BaseMod basemod;
        for (Iterator iterator = modList.iterator(); iterator.hasNext(); basemod.OnItemPickup(entityplayer, itemstack))
        {
            basemod = (BaseMod)iterator.next();
        }
    }

    public static void OnTick(float f, Minecraft minecraft)
    {
        Profiler.endSection();
        Profiler.endSection();
        Profiler.startSection("modtick");
        if (!hasInit)
        {
            init();
            logger.fine("Initialized");
        }
        if (texPack == null || minecraft.gameSettings.skin != texPack)
        {
            texturesAdded = false;
            texPack = minecraft.gameSettings.skin;
        }
        if (langPack == null || StringTranslate.getInstance().func_44024_c() != langPack)
        {
            Properties properties = null;
            try
            {
                properties = (Properties)getPrivateValue(io.github.lilybukkit.dandelion.compat.StringTranslate.class, StringTranslate.getInstance(), 1);
            }
            catch (SecurityException securityexception)
            {
                logger.throwing("ModLoader", "AddLocalization", securityexception);
                ThrowException(securityexception);
            }
            catch (NoSuchFieldException nosuchfieldexception)
            {
                logger.throwing("ModLoader", "AddLocalization", nosuchfieldexception);
                ThrowException(nosuchfieldexception);
            }
            if (properties != null)
            {
                properties.putAll(localizedStrings);
            }
            langPack = StringTranslate.getInstance().func_44024_c();
        }
        if (!texturesAdded && minecraft.renderEngine != null)
        {
            RegisterAllTextureOverrides(minecraft.renderEngine);
            texturesAdded = true;
        }
        long l = 0L;
        if (minecraft.theWorld != null)
        {
            l = minecraft.theWorld.getWorldTime();
            for (Iterator iterator = inGameHooks.entrySet().iterator(); iterator.hasNext();)
            {
                java.util.Map.Entry entry1 = (java.util.Map.Entry)iterator.next();
                if ((clock != l || !((Boolean)entry1.getValue()).booleanValue()) && !((BaseMod)entry1.getKey()).OnTickInGame(f, minecraft))
                {
                    iterator.remove();
                }
            }
        }
        if (minecraft.standardGalacticFontRenderer != null)
        {
            for (Iterator iterator1 = inGUIHooks.entrySet().iterator(); iterator1.hasNext();)
            {
                java.util.Map.Entry entry2 = (java.util.Map.Entry)iterator1.next();
                if ((clock != l || !(((Boolean)entry2.getValue()).booleanValue() & (minecraft.theWorld != null))) && !((BaseMod)entry2.getKey()).OnTickInGUI(f, minecraft, minecraft.currentScreen))
                {
                    iterator1.remove();
                }
            }
        }
        if (clock != l)
        {
            for (Iterator iterator2 = keyList.entrySet().iterator(); iterator2.hasNext();)
            {
                java.util.Map.Entry entry = (java.util.Map.Entry)iterator2.next();
                for (Iterator iterator3 = ((Map)entry.getValue()).entrySet().iterator(); iterator3.hasNext();)
                {
                    java.util.Map.Entry entry3 = (java.util.Map.Entry)iterator3.next();
                    int i = ((KeyBinding)entry3.getKey()).keyCode;
                    boolean flag;
                    if (i < 0)
                    {
                        flag = Mouse.isButtonDown(i += 100);
                    }
                    else
                    {
                        flag = Keyboard.isKeyDown(i);
                    }
                    boolean aflag[] = (boolean[])entry3.getValue();
                    boolean flag1 = aflag[1];
                    aflag[1] = flag;
                    if (flag && (!flag1 || aflag[0]))
                    {
                        ((BaseMod)entry.getKey()).KeyboardEvent((KeyBinding)entry3.getKey());
                    }
                }
            }
        }
        clock = l;
        Profiler.endSection();
        Profiler.startSection("render");
        Profiler.startSection("gameRenderer");
    }

    public static void OpenGUI(EntityPlayer entityplayer, GuiScreen guiscreen)
    {
        if (!hasInit)
        {
            init();
            logger.fine("Initialized");
        }
        Minecraft minecraft = getMinecraftInstance();
        if (minecraft.renderViewEntity != entityplayer)
        {
            return;
        }
        if (guiscreen != null)
        {
            minecraft.displayGuiScreen(guiscreen);
        }
    }

    public static void PopulateChunk(IChunkProvider ichunkprovider, int i, int j, World world)
    {
        if (!hasInit)
        {
            init();
            logger.fine("Initialized");
        }
        Random random = new Random(world.getWorldSeed());
        long l = (random.nextLong() / 2L) * 2L + 1L;
        long l1 = (random.nextLong() / 2L) * 2L + 1L;
        random.setSeed((long)i * l + (long)j * l1 ^ world.getWorldSeed());
        for (Iterator iterator = modList.iterator(); iterator.hasNext();)
        {
            BaseMod basemod = (BaseMod)iterator.next();
            if (ichunkprovider.makeString().equals("RandomLevelSource"))
            {
                basemod.GenerateSurface(world, random, i << 4, j << 4);
            }
            else if (ichunkprovider.makeString().equals("HellRandomLevelSource"))
            {
                basemod.GenerateNether(world, random, i << 4, j << 4);
            }
        }
    }

    private static void readFromClassPath(File file)
    throws FileNotFoundException, IOException
    {
        logger.finer("Adding mods from " + file.getCanonicalPath());
        ClassLoader classloader = (modloader.ModLoader.class).getClassLoader();
        if (file.isFile() && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")))
        {
            logger.finer("Zip found.");
            FileInputStream fileinputstream = new FileInputStream(file);
            ZipInputStream zipinputstream = new ZipInputStream(fileinputstream);
            Object obj = null;
            do
            {
                ZipEntry zipentry = zipinputstream.getNextEntry();
                if (zipentry == null)
                {
                    break;
                }
                String s1 = zipentry.getName();
                if (!zipentry.isDirectory() && s1.startsWith("mod_") && s1.endsWith(".class"))
                {
                    addMod(classloader, s1);
                }
            }
            while (true);
            fileinputstream.close();
        }
        else if (file.isDirectory())
        {
            Package package1 = (io.github.lilybukkit.dandelion.compat.ModLoader.class).getPackage();
            if (package1 != null)
            {
                String s = package1.getName().replace('.', File.separatorChar);
                file = new File(file, s);
            }
            logger.finer("Directory found.");
            File afile[] = file.listFiles();
            if (afile != null)
            {
                for (int i = 0; i < afile.length; i++)
                {
                    String s2 = afile[i].getName();
                    if (afile[i].isFile() && s2.startsWith("mod_") && s2.endsWith(".class"))
                    {
                        addMod(classloader, s2);
                    }
                }
            }
        }
    }

    private static void readFromModFolder(File file)
    throws IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException
    {
        ClassLoader classloader = (net.minecraft.client.Minecraft.class).getClassLoader();
        Method method = (java.net.URLClassLoader.class).getDeclaredMethod("addURL", new Class[]
                {
                    java.net.URL.class
                });
        method.setAccessible(true);
        if (!file.isDirectory())
        {
            throw new IllegalArgumentException("folder must be a Directory.");
        }
        File afile[] = file.listFiles();
        Arrays.sort(afile);
        if (classloader instanceof URLClassLoader)
        {
            for (int i = 0; i < afile.length; i++)
            {
                File file1 = afile[i];
                if (file1.isDirectory() || file1.isFile() && (file1.getName().endsWith(".jar") || file1.getName().endsWith(".zip")))
                {
                    method.invoke(classloader, new Object[]
                            {
                                file1.toURI().toURL()
                            });
                }
            }
        }
        for (int j = 0; j < afile.length; j++)
        {
            File file2 = afile[j];
            if (file2.isDirectory() || file2.isFile() && (file2.getName().endsWith(".jar") || file2.getName().endsWith(".zip")))
            {
                logger.finer("Adding mods from " + file2.getCanonicalPath());
                if (file2.isFile())
                {
                    logger.finer("Zip found.");
                    FileInputStream fileinputstream = new FileInputStream(file2);
                    ZipInputStream zipinputstream = new ZipInputStream(fileinputstream);
                    Object obj = null;
                    do
                    {
                        ZipEntry zipentry = zipinputstream.getNextEntry();
                        if (zipentry == null)
                        {
                            break;
                        }
                        String s1 = zipentry.getName();
                        if (!zipentry.isDirectory() && s1.startsWith("mod_") && s1.endsWith(".class"))
                        {
                            addMod(classloader, s1);
                        }
                    }
                    while (true);
                    zipinputstream.close();
                    fileinputstream.close();
                }
                else if (file2.isDirectory())
                {
                    Package package1 = (io.github.lilybukkit.dandelion.compat.ModLoader.class).getPackage();
                    if (package1 != null)
                    {
                        String s = package1.getName().replace('.', File.separatorChar);
                        file2 = new File(file2, s);
                    }
                    logger.finer("Directory found.");
                    File afile1[] = file2.listFiles();
                    if (afile1 != null)
                    {
                        for (int k = 0; k < afile1.length; k++)
                        {
                            String s2 = afile1[k].getName();
                            if (afile1[k].isFile() && s2.startsWith("mod_") && s2.endsWith(".class"))
                            {
                                addMod(classloader, s2);
                            }
                        }
                    }
                }
            }
        }
    }

    public static KeyBinding[] RegisterAllKeys(KeyBinding akeybinding[])
    {
        LinkedList linkedlist = new LinkedList();
        linkedlist.addAll(Arrays.asList(akeybinding));
        Map map;
        for (Iterator iterator = keyList.values().iterator(); iterator.hasNext(); linkedlist.addAll(map.keySet()))
        {
            map = (Map)iterator.next();
        }

        return (KeyBinding[])linkedlist.toArray(new KeyBinding[0]);
    }

    public static void RegisterAllTextureOverrides(RenderEngine renderengine)
    {
        animList.clear();
        Minecraft minecraft = getMinecraftInstance();
        BaseMod basemod;
        for (Iterator iterator = modList.iterator(); iterator.hasNext(); basemod.RegisterAnimation(minecraft))
        {
            basemod = (BaseMod)iterator.next();
        }

        TextureFX texturefx;
        for (Iterator iterator1 = animList.iterator(); iterator1.hasNext(); renderengine.registerTextureFX(texturefx))
        {
            texturefx = (TextureFX)iterator1.next();
        }

        for (Iterator iterator2 = overrides.entrySet().iterator(); iterator2.hasNext();)
        {
            java.util.Map.Entry entry = (java.util.Map.Entry)iterator2.next();
            for (Iterator iterator3 = ((Map)entry.getValue()).entrySet().iterator(); iterator3.hasNext();)
            {
                java.util.Map.Entry entry1 = (java.util.Map.Entry)iterator3.next();
                String s = (String)entry1.getKey();
                int i = ((Integer)entry1.getValue()).intValue();
                int j = ((Integer)entry.getKey()).intValue();
                try
                {
                    java.awt.image.BufferedImage bufferedimage = loadImage(renderengine, s);
                    ModTextureStatic modtexturestatic = new ModTextureStatic(i, j, bufferedimage);
                    renderengine.registerTextureFX(modtexturestatic);
                }
                catch (Exception exception)
                {
                    logger.throwing("ModLoader", "RegisterAllTextureOverrides", exception);
                    ThrowException(exception);
                    throw new RuntimeException(exception);
                }
            }
        }
    }

    public static void RegisterBlock(Block block)
    {
        RegisterBlock(block, null);
    }

    public static void RegisterBlock(Block block, Class class1)
    {
        try
        {
            if (block == null)
            {
                throw new IllegalArgumentException("block parameter cannot be null.");
            }
            int i = block.blockID;
            ItemBlock itemblock = null;
            if (class1 != null)
            {
                itemblock = (ItemBlock)class1.getConstructor(new Class[]
                        {
                            Integer.TYPE
                        }).newInstance(new Object[]
                                {
                                    Integer.valueOf(i - 256)
                                });
            }
            else
            {
                itemblock = new ItemBlock(i - 256);
            }
            if (Block.blocksList[i] != null && Item.itemsList[i] == null)
            {
                Item.itemsList[i] = itemblock;
            }
        }
        catch (IllegalArgumentException illegalargumentexception)
        {
            logger.throwing("ModLoader", "RegisterBlock", illegalargumentexception);
            ThrowException(illegalargumentexception);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "RegisterBlock", illegalaccessexception);
            ThrowException(illegalaccessexception);
        }
        catch (SecurityException securityexception)
        {
            logger.throwing("ModLoader", "RegisterBlock", securityexception);
            ThrowException(securityexception);
        }
        catch (InstantiationException instantiationexception)
        {
            logger.throwing("ModLoader", "RegisterBlock", instantiationexception);
            ThrowException(instantiationexception);
        }
        catch (InvocationTargetException invocationtargetexception)
        {
            logger.throwing("ModLoader", "RegisterBlock", invocationtargetexception);
            ThrowException(invocationtargetexception);
        }
        catch (NoSuchMethodException nosuchmethodexception)
        {
            logger.throwing("ModLoader", "RegisterBlock", nosuchmethodexception);
            ThrowException(nosuchmethodexception);
        }
    }

    public static void RegisterEntityID(Class class1, String s, int i)
    {
        try
        {
            method_RegisterEntityID.invoke(null, new Object[]
                    {
                        class1, s, Integer.valueOf(i)
                    });
        }
        catch (IllegalArgumentException illegalargumentexception)
        {
            logger.throwing("ModLoader", "RegisterEntityID", illegalargumentexception);
            ThrowException(illegalargumentexception);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "RegisterEntityID", illegalaccessexception);
            ThrowException(illegalaccessexception);
        }
        catch (InvocationTargetException invocationtargetexception)
        {
            logger.throwing("ModLoader", "RegisterEntityID", invocationtargetexception);
            ThrowException(invocationtargetexception);
        }
    }

    public static void RegisterEntityID(Class class1, String s, int i, int j, int k)
    {
        RegisterEntityID(class1, s, i);
        EntityList.field_44041_a.put(Integer.valueOf(i), new EntityEggInfo(i, j, k));
    }

    public static void RegisterKey(BaseMod basemod, KeyBinding keybinding, boolean flag)
    {
        Object obj = (Map)keyList.get(basemod);
        if (obj == null)
        {
            obj = new HashMap();
        }
        boolean aflag[] = new boolean[2];
        aflag[0] = flag;
        ((Map) (obj)).put(keybinding, aflag);
        keyList.put(basemod, obj);
    }

    public static void RegisterTileEntity(Class class1, String s)
    {
        RegisterTileEntity(class1, s, null);
    }

    public static void RegisterTileEntity(Class class1, String s, TileEntitySpecialRenderer tileentityspecialrenderer)
    {
        try
        {
            method_RegisterTileEntity.invoke(null, new Object[]
                    {
                        class1, s
                    });
            if (tileentityspecialrenderer != null)
            {
                TileEntityRenderer tileentityrenderer = TileEntityRenderer.instance;
                Map map = (Map)field_TileEntityRenderers.get(tileentityrenderer);
                map.put(class1, tileentityspecialrenderer);
                tileentityspecialrenderer.setTileEntityRenderer(tileentityrenderer);
            }
        }
        catch (IllegalArgumentException illegalargumentexception)
        {
            logger.throwing("ModLoader", "RegisterTileEntity", illegalargumentexception);
            ThrowException(illegalargumentexception);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "RegisterTileEntity", illegalaccessexception);
            ThrowException(illegalaccessexception);
        }
        catch (InvocationTargetException invocationtargetexception)
        {
            logger.throwing("ModLoader", "RegisterTileEntity", invocationtargetexception);
            ThrowException(invocationtargetexception);
        }
    }

    public static void RemoveSpawn(Class class1, EnumCreatureType enumcreaturetype)
    {
        RemoveSpawn(class1, enumcreaturetype, null);
    }

    public static void RemoveSpawn(Class class1, EnumCreatureType enumcreaturetype, BiomeGenBase abiomegenbase[])
    {
        if (class1 == null)
        {
            throw new IllegalArgumentException("entityClass cannot be null");
        }
        if (enumcreaturetype == null)
        {
            throw new IllegalArgumentException("spawnList cannot be null");
        }
        if (abiomegenbase == null)
        {
            abiomegenbase = standardBiomes;
        }
        for (int i = 0; i < abiomegenbase.length; i++)
        {
            List list = abiomegenbase[i].getSpawnableList(enumcreaturetype);
            if (list != null)
            {
                for (Iterator iterator = list.iterator(); iterator.hasNext();)
                {
                    SpawnListEntry spawnlistentry = (SpawnListEntry)iterator.next();
                    if (spawnlistentry.entityClass == class1)
                    {
                        iterator.remove();
                    }
                }
            }
        }
    }

    public static void RemoveSpawn(String s, EnumCreatureType enumcreaturetype)
    {
        RemoveSpawn(s, enumcreaturetype, null);
    }

    public static void RemoveSpawn(String s, EnumCreatureType enumcreaturetype, BiomeGenBase abiomegenbase[])
    {
        Class class1 = (Class)classMap.get(s);
        if (class1 != null && (io.github.lilybukkit.dandelion.compat.EntityLiving.class).isAssignableFrom(class1))
        {
            RemoveSpawn(class1, enumcreaturetype, abiomegenbase);
        }
    }

    public static boolean RenderBlockIsItemFull3D(int i)
    {
        if (!blockSpecialInv.containsKey(Integer.valueOf(i)))
        {
            return i == 16;
        }
        else
        {
            return ((Boolean)blockSpecialInv.get(Integer.valueOf(i))).booleanValue();
        }
    }

    public static void RenderInvBlock(RenderBlocks renderblocks, Block block, int i, int j)
    {
        BaseMod basemod = (BaseMod)blockModels.get(Integer.valueOf(j));
        if (basemod == null)
        {
            return;
        }
        else
        {
            basemod.RenderInvBlock(renderblocks, block, i, j);
            return;
        }
    }

    public static boolean RenderWorldBlock(RenderBlocks renderblocks, IBlockAccess iblockaccess, int i, int j, int k, Block block, int l)
    {
        BaseMod basemod = (BaseMod)blockModels.get(Integer.valueOf(l));
        if (basemod == null)
        {
            return false;
        }
        else
        {
            return basemod.RenderWorldBlock(renderblocks, iblockaccess, i, j, k, block, l);
        }
    }

    public static void saveConfig()
    throws IOException
    {
        cfgdir.mkdir();
        if (!cfgfile.exists() && !cfgfile.createNewFile())
        {
            return;
        }
        if (cfgfile.canWrite())
        {
            FileOutputStream fileoutputstream = new FileOutputStream(cfgfile);
            props.store(fileoutputstream, "ModLoader Config");
            fileoutputstream.close();
        }
    }

    public static void SetInGameHook(BaseMod basemod, boolean flag, boolean flag1)
    {
        if (flag)
        {
            inGameHooks.put(basemod, Boolean.valueOf(flag1));
        }
        else
        {
            inGameHooks.remove(basemod);
        }
    }

    public static void SetInGUIHook(BaseMod basemod, boolean flag, boolean flag1)
    {
        if (flag)
        {
            inGUIHooks.put(basemod, Boolean.valueOf(flag1));
        }
        else
        {
            inGUIHooks.remove(basemod);
        }
    }

    public static void setPrivateValue(Class class1, Object obj, int i, Object obj1)
    throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
            Field field = class1.getDeclaredFields()[i];
            field.setAccessible(true);
            int j = field_modifiers.getInt(field);
            if ((j & 0x10) != 0)
            {
                field_modifiers.setInt(field, j & 0xffffffef);
            }
            field.set(obj, obj1);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "setPrivateValue", illegalaccessexception);
            ThrowException("An impossible error has occured!", illegalaccessexception);
        }
    }

    public static void setPrivateValue(Class class1, Object obj, String s, Object obj1)
    throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
            Field field = class1.getDeclaredField(s);
            int i = field_modifiers.getInt(field);
            if ((i & 0x10) != 0)
            {
                field_modifiers.setInt(field, i & 0xffffffef);
            }
            field.setAccessible(true);
            field.set(obj, obj1);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "setPrivateValue", illegalaccessexception);
            ThrowException("An impossible error has occured!", illegalaccessexception);
        }
    }

    private static void setupProperties(Class class1)
    throws IllegalArgumentException, IllegalAccessException, IOException, SecurityException, NoSuchFieldException, NoSuchAlgorithmException, DigestException
    {
        LinkedList linkedlist = new LinkedList();
        Properties properties = new Properties();
        int i = 0;
        int j = 0;
        File file = new File(cfgdir, String.valueOf(class1.getSimpleName()) + ".cfg");
        if (file.exists() && file.canRead())
        {
            properties.load(new FileInputStream(file));
        }
        if (properties.containsKey("checksum"))
        {
            j = Integer.parseInt(properties.getProperty("checksum"), 36);
        }
        Field afield[];
        int l = (afield = class1.getDeclaredFields()).length;
        for (int k = 0; k < l; k++)
        {
            Field field = afield[k];
            if ((field.getModifiers() & 8) != 0 && field.isAnnotationPresent(io.github.lilybukkit.dandelion.compat.MLProp.class))
            {
                linkedlist.add(field);
                Object obj = field.get(null);
                i += obj.hashCode();
            }
        }

        StringBuilder stringbuilder = new StringBuilder();
        Iterator iterator = linkedlist.iterator();
        while (iterator.hasNext())
        {
            Field field1 = (Field)iterator.next();
            if ((field1.getModifiers() & 8) == 0 || !field1.isAnnotationPresent(io.github.lilybukkit.dandelion.compat.MLProp.class))
            {
                continue;
            }
            Class class2 = field1.getType();
            MLProp mlprop = (MLProp)field1.getAnnotation(io.github.lilybukkit.dandelion.compat.MLProp.class);
            String s = mlprop.name().length() != 0 ? mlprop.name() : field1.getName();
            Object obj1 = field1.get(null);
            StringBuilder stringbuilder1 = new StringBuilder();
            if (mlprop.min() != (-1.0D / 0.0D))
            {
                stringbuilder1.append(String.format(",>=%.1f", new Object[]
                        {
                            Double.valueOf(mlprop.min())
                        }));
            }
            if (mlprop.max() != (1.0D / 0.0D))
            {
                stringbuilder1.append(String.format(",<=%.1f", new Object[]
                        {
                            Double.valueOf(mlprop.max())
                        }));
            }
            StringBuilder stringbuilder2 = new StringBuilder();
            if (mlprop.info().length() > 0)
            {
                stringbuilder2.append(" -- ");
                stringbuilder2.append(mlprop.info());
            }
            stringbuilder.append(String.format("%s (%s:%s%s)%s\n", new Object[]
                    {
                        s, class2.getName(), obj1, stringbuilder1, stringbuilder2
                    }));
            if (j == i && properties.containsKey(s))
            {
                String s1 = properties.getProperty(s);
                Object obj2 = null;
                if (class2.isAssignableFrom(java.lang.String.class))
                {
                    obj2 = s1;
                }
                else if (class2.isAssignableFrom(Integer.TYPE))
                {
                    obj2 = Integer.valueOf(Integer.parseInt(s1));
                }
                else if (class2.isAssignableFrom(Short.TYPE))
                {
                    obj2 = Short.valueOf(Short.parseShort(s1));
                }
                else if (class2.isAssignableFrom(Byte.TYPE))
                {
                    obj2 = Byte.valueOf(Byte.parseByte(s1));
                }
                else if (class2.isAssignableFrom(Boolean.TYPE))
                {
                    obj2 = Boolean.valueOf(Boolean.parseBoolean(s1));
                }
                else if (class2.isAssignableFrom(Float.TYPE))
                {
                    obj2 = Float.valueOf(Float.parseFloat(s1));
                }
                else if (class2.isAssignableFrom(Double.TYPE))
                {
                    obj2 = Double.valueOf(Double.parseDouble(s1));
                }
                if (obj2 == null)
                {
                    continue;
                }
                if (obj2 instanceof Number)
                {
                    double d = ((Number)obj2).doubleValue();
                    if (mlprop.min() != (-1.0D / 0.0D) && d < mlprop.min() || mlprop.max() != (1.0D / 0.0D) && d > mlprop.max())
                    {
                        continue;
                    }
                }
                logger.finer(String.valueOf(s) + " set to " + obj2);
                if (!obj2.equals(obj1))
                {
                    field1.set(null, obj2);
                }
            }
            else
            {
                logger.finer(String.valueOf(s) + " not in config, using default: " + obj1);
                properties.setProperty(s, obj1.toString());
            }
        }
        properties.put("checksum", Integer.toString(i, 36));
        if (!properties.isEmpty() && (file.exists() || file.createNewFile()) && file.canWrite())
        {
            properties.store(new FileOutputStream(file), stringbuilder.toString());
        }
    }

    public static void TakenFromCrafting(EntityPlayer entityplayer, ItemStack itemstack, IInventory iinventory)
    {
        BaseMod basemod;
        for (Iterator iterator = modList.iterator(); iterator.hasNext(); basemod.TakenFromCrafting(entityplayer, itemstack, iinventory))
        {
            basemod = (BaseMod)iterator.next();
        }
    }

    public static void TakenFromFurnace(EntityPlayer entityplayer, ItemStack itemstack)
    {
        BaseMod basemod;
        for (Iterator iterator = modList.iterator(); iterator.hasNext(); basemod.TakenFromFurnace(entityplayer, itemstack))
        {
            basemod = (BaseMod)iterator.next();
        }
    }

    public static void ThrowException(String s, Throwable throwable)
    {
        Minecraft minecraft = getMinecraftInstance();
        if (minecraft != null)
        {
            minecraft.displayUnexpectedThrowable(new UnexpectedThrowable(s, throwable));
        }
        else
        {
            throw new RuntimeException(throwable);
        }
    }

    private static void ThrowException(Throwable throwable)
    {
        ThrowException("Exception occured in ModLoader", throwable);
    }

    private ModLoader()
    {
    }

    static
    {
        cfgdir = new File(Minecraft.getMinecraftDir(), "/config/");
        cfgfile = new File(cfgdir, "ModLoader.cfg");
        cfgLoggingLevel = Level.FINER;
    }
}
