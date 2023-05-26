package forge.packets;

public class PacketMissingMods extends PacketModList
{

    public PacketMissingMods(boolean server)
    {
        super(!server);
    }

    @Override
    public int getID()
    {
        return MOD_MISSING;
    }

}
