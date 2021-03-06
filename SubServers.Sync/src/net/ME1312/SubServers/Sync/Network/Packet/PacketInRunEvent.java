package net.ME1312.SubServers.Sync.Network.Packet;

import net.ME1312.SubServers.Sync.Event.*;
import net.ME1312.SubServers.Sync.Library.Callback;
import net.ME1312.SubServers.Sync.Library.Config.YAMLSection;
import net.ME1312.SubServers.Sync.Library.NamedContainer;
import net.ME1312.SubServers.Sync.Library.Version.Version;
import net.ME1312.SubServers.Sync.Network.PacketIn;
import net.ME1312.SubServers.Sync.SubPlugin;
import net.md_5.bungee.api.ProxyServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Run Event Packet
 */
public class PacketInRunEvent implements PacketIn {
    private static HashMap<String, List<Callback<YAMLSection>>> callbacks = new HashMap<String, List<Callback<YAMLSection>>>();

    /**
     * New PacketInRunEvent
     */
    public PacketInRunEvent(SubPlugin plugin) {
        callback("SubAddHostEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                ProxyServer.getInstance().getPluginManager().callEvent(new SubAddHostEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("host")));
                callback("SubAddHostEvent", this);
            }
        });
        callback("SubAddProxyEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                ProxyServer.getInstance().getPluginManager().callEvent(new SubAddProxyEvent(data.getRawString("proxy")));
                callback("SubAddProxyEvent", this);
            }
        });
        callback("SubAddServerEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                ProxyServer.getInstance().getPluginManager().callEvent(new SubAddServerEvent((data.contains("player"))?data.getUUID("player"):null, (data.contains("host"))?data.getRawString("host"):null, data.getRawString("server")));
                callback("SubAddServerEvent", this);
            }
        });
        callback("SubCreateEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                ProxyServer.getInstance().getPluginManager().callEvent(new SubCreateEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("host"), data.getRawString("name"),
                        data.getRawString("template"), data.getVersion("version"), data.getInt("port")));
                callback("SubCreateEvent", this);
            }
        });
        callback("SubSendCommandEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                ProxyServer.getInstance().getPluginManager().callEvent(new SubSendCommandEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("server"), data.getRawString("command")));
                callback("SubSendCommandEvent", this);
            }
        });
        callback("SubEditServerEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                ProxyServer.getInstance().getPluginManager().callEvent(new SubEditServerEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("server"), new NamedContainer<String, Object>(data.getRawString("edit"), data.get("value")), data.getBoolean("perm")));
                callback("SubEditServerEvent", this);
            }
        });
        callback("SubStartEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                ProxyServer.getInstance().getPluginManager().callEvent(new SubStartEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("server")));
                callback("SubStartEvent", this);
            }
        });
        callback("SubNetworkConnectEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                plugin.connect(plugin.servers.get(data.getRawString("server").toLowerCase()), data.getRawString("address"));
                callback("SubNetworkConnectEvent", this);
            }
        });
        callback("SubNetworkDisconnectEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                plugin.disconnect(plugin.servers.get(data.getRawString("server").toLowerCase()));
                callback("SubNetworkDisconnectEvent", this);
            }
        });
        callback("SubStopEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                ProxyServer.getInstance().getPluginManager().callEvent(new SubStopEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("server"), data.getBoolean("force")));
                callback("SubStopEvent", this);
            }
        });
        callback("SubStoppedEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                ProxyServer.getInstance().getPluginManager().callEvent(new SubStoppedEvent(data.getRawString("server")));
                callback("SubStoppedEvent", this);
            }
        });
        callback("SubRemoveServerEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                ProxyServer.getInstance().getPluginManager().callEvent(new SubRemoveServerEvent((data.contains("player"))?data.getUUID("player"):null, (data.contains("host"))?data.getRawString("host"):null, data.getRawString("server")));
                callback("SubRemoveServerEvent", this);
            }
        });
        callback("SubRemoveProxyEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                ProxyServer.getInstance().getPluginManager().callEvent(new SubAddProxyEvent(data.getRawString("proxy")));
                callback("SubRemoveProxyEvent", this);
            }
        });
        callback("SubRemoveHostEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                ProxyServer.getInstance().getPluginManager().callEvent(new SubRemoveHostEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("host")));
                callback("SubRemoveHostEvent", this);
            }
        });
    }

    @Override
    public void execute(YAMLSection data) {
        if (callbacks.keySet().contains(data.getString("type"))) {
            List<Callback<YAMLSection>> callbacks = PacketInRunEvent.callbacks.get(data.getString("type"));
            PacketInRunEvent.callbacks.remove(data.getString("type"));
            for (Callback<YAMLSection> callback : callbacks) {
                callback.run(data.getSection("args"));
            }
        }
    }

    @Override
    public Version getVersion() {
        return new Version("2.11.0a");
    }

    public static void callback(String event, Callback<YAMLSection> callback) {
        List<Callback<YAMLSection>> callbacks = (PacketInRunEvent.callbacks.keySet().contains(event))?PacketInRunEvent.callbacks.get(event):new ArrayList<Callback<YAMLSection>>();
        callbacks.add(callback);
        PacketInRunEvent.callbacks.put(event, callbacks);
    }
}
