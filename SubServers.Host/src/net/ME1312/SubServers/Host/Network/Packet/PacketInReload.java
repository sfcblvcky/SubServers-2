package net.ME1312.SubServers.Host.Network.Packet;

import net.ME1312.Galaxi.Library.Config.YAMLSection;
import net.ME1312.Galaxi.Library.Log.Logger;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.SubServers.Host.ExHost;
import net.ME1312.SubServers.Host.Network.PacketIn;
import net.ME1312.SubServers.Host.Network.SubDataClient;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Reload Packet
 */
public class PacketInReload implements PacketIn {
    private ExHost host;
    private Logger log;

    public PacketInReload(ExHost host) {
        this.host = host;
        Util.isException(() -> this.log = Util.reflect(SubDataClient.class.getDeclaredField("log"), null));
    }

    @Override
    public void execute(YAMLSection data) {
        try {
            host.reload();
        } catch (IOException e) {
            log.error.println(e);
        }
    }

    @Override
    public Version getVersion() {
        return new Version("2.11.0a");
    }
}
