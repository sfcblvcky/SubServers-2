package net.ME1312.SubServers.Sync.Network.Packet;

import net.ME1312.SubServers.Sync.Library.Callback;
import net.ME1312.SubServers.Sync.Library.Config.YAMLSection;
import net.ME1312.SubServers.Sync.Library.Util;
import net.ME1312.SubServers.Sync.Library.Version.Version;
import net.ME1312.SubServers.Sync.Network.PacketIn;
import net.ME1312.SubServers.Sync.Network.PacketOut;

import java.util.HashMap;
import java.util.UUID;

/**
 * Download Proxy Info Packet
 */
public class PacketDownloadProxyInfo implements PacketIn, PacketOut {
    private static HashMap<String, Callback<YAMLSection>[]> callbacks = new HashMap<String, Callback<YAMLSection>[]>();
    private String proxy;
    private String id;

    /**
     * New PacketDownloadProxyInfo (In)
     */
    public PacketDownloadProxyInfo() {}

    /**
     * New PacketDownloadProxyInfo (Out)
     *
     * @param proxy Proxy name (or null for all)
     * @param callback Callbacks
     */
    @SafeVarargs
    public PacketDownloadProxyInfo(String proxy, Callback<YAMLSection>... callback) {
        if (Util.isNull((Object) callback)) throw new NullPointerException();
        this.proxy = proxy;
        this.id = Util.getNew(callbacks.keySet(), UUID::randomUUID).toString();
        callbacks.put(id, callback);
    }

    @Override
    public YAMLSection generate() {
        YAMLSection json = new YAMLSection();
        json.set("id", id);
        if (proxy != null) json.set("proxy", proxy);
        return json;
    }

    @Override
    public void execute(YAMLSection data) {
        for (Callback<YAMLSection> callback : callbacks.get(data.getRawString("id"))) callback.run(data);
        callbacks.remove(data.getRawString("id"));
    }

    @Override
    public Version getVersion() {
        return new Version("2.13b");
    }
}
