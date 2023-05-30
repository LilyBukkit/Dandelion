package forge;

import NetworkManager;

public interface IPacketHandler
{
    /**
     * Called when we receive a Packet250CustomPayload for a channel that this
     * handler is registered to.
     *
     * @param network The NetworkManager for the current connection.
     * @param channel The Channel the message came on.
     * @param data The message payload.
     */
    public void onPacketData(NetworkManager network, String channel, byte[] data);
}
