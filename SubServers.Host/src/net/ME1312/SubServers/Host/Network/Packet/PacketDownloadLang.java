package net.ME1312.SubServers.Host.Network.Packet;

import net.ME1312.Galaxi.Library.Config.YAMLSection;
import net.ME1312.Galaxi.Library.Log.Logger;
import net.ME1312.Galaxi.Library.NamedContainer;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.SubServers.Host.Network.PacketIn;
import net.ME1312.SubServers.Host.Network.PacketOut;
import net.ME1312.SubServers.Host.Network.SubDataClient;
import net.ME1312.SubServers.Host.ExHost;

import java.lang.reflect.Field;
import java.util.Calendar;

public class PacketDownloadLang implements PacketIn, PacketOut {
    private ExHost host;
    private Logger log = null;

    public PacketDownloadLang() {}

    public PacketDownloadLang(ExHost host) {
        if (Util.isNull(host)) throw new NullPointerException();
        this.host = host;
        Util.isException(() -> this.log = Util.reflect(SubDataClient.class.getDeclaredField("log"), null));
    }

    @Override
    public YAMLSection generate() {
        return null;
    }

    @Override
    public void execute(YAMLSection data) {
        try {
            Util.reflect(ExHost.class.getDeclaredField("lang"), host, new NamedContainer<>(Calendar.getInstance().getTime().getTime(), data.getSection("Lang").get()));
            log.info.println("Lang Settings Downloaded");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            log.error.println(e);
        }
    }

    @Override
    public Version getVersion() {
        return new Version("2.11.0a");
    }
}
