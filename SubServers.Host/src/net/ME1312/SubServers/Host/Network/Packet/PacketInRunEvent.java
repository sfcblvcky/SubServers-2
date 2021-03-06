package net.ME1312.SubServers.Host.Network.Packet;

import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Library.Callback;
import net.ME1312.Galaxi.Library.Config.YAMLSection;
import net.ME1312.Galaxi.Library.NamedContainer;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.SubServers.Host.Event.*;
import net.ME1312.SubServers.Host.Network.PacketIn;
import net.ME1312.SubServers.Host.SubAPI;

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
    public PacketInRunEvent() {
        callback("SubAddHostEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                GalaxiEngine.getInstance().getPluginManager().executeEvent(new SubAddHostEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("host")));
                callback("SubAddHostEvent", this);
            }
        });
        callback("SubAddProxyEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                GalaxiEngine.getInstance().getPluginManager().executeEvent(new SubAddProxyEvent(data.getRawString("proxy")));
                callback("SubAddProxyEvent", this);
            }
        });
        callback("SubAddServerEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                GalaxiEngine.getInstance().getPluginManager().executeEvent(new SubAddServerEvent((data.contains("player"))?data.getUUID("player"):null, (data.contains("host"))?data.getRawString("host"):null, data.getRawString("server")));
                callback("SubAddServerEvent", this);
            }
        });
        callback("SubCreateEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                GalaxiEngine.getInstance().getPluginManager().executeEvent(new SubCreateEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("host"), data.getRawString("name"),
                        data.getRawString("template"), data.getVersion("version"), data.getInt("port")));
                callback("SubCreateEvent", this);
            }
        });
        callback("SubSendCommandEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                GalaxiEngine.getInstance().getPluginManager().executeEvent(new SubSendCommandEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("server"), data.getRawString("command")));
                callback("SubSendCommandEvent", this);
            }
        });
        callback("SubEditServerEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                GalaxiEngine.getInstance().getPluginManager().executeEvent(new SubEditServerEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("server"), new NamedContainer<String, Object>(data.getRawString("edit"), data.get("value")), data.getBoolean("perm")));
                callback("SubEditServerEvent", this);
            }
        });
        callback("SubStartEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                GalaxiEngine.getInstance().getPluginManager().executeEvent(new SubStartEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("server")));
                callback("SubStartEvent", this);
            }
        });
        callback("SubStopEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                GalaxiEngine.getInstance().getPluginManager().executeEvent(new SubStopEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("server"), data.getBoolean("force")));
                callback("SubStopEvent", this);
            }
        });
        callback("SubStoppedEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                GalaxiEngine.getInstance().getPluginManager().executeEvent(new SubStoppedEvent(data.getRawString("server")));
                callback("SubStoppedEvent", this);
            }
        });
        callback("SubRemoveServerEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                GalaxiEngine.getInstance().getPluginManager().executeEvent(new SubRemoveServerEvent((data.contains("player"))?data.getUUID("player"):null, (data.contains("host"))?data.getRawString("host"):null, data.getRawString("server")));
                callback("SubRemoveServerEvent", this);
            }
        });
        callback("SubRemoveProxyEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                GalaxiEngine.getInstance().getPluginManager().executeEvent(new SubAddProxyEvent(data.getRawString("proxy")));
                callback("SubRemoveProxyEvent", this);
            }
        });
        callback("SubRemoveHostEvent", new Callback<YAMLSection>() {
            @Override
            public void run(YAMLSection data) {
                GalaxiEngine.getInstance().getPluginManager().executeEvent(new SubRemoveHostEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("host")));
                callback("SubRemoveHostEvent", this);
            }
        });
    }

    @Override
    public void execute(YAMLSection data) {
        if (callbacks.keySet().contains(data.getRawString("type"))) {
            List<Callback<YAMLSection>> callbacks = PacketInRunEvent.callbacks.get(data.getRawString("type"));
            PacketInRunEvent.callbacks.remove(data.getRawString("type"));
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
