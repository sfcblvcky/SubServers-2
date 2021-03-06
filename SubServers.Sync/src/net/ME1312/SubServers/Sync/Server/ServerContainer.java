package net.ME1312.SubServers.Sync.Server;

import net.ME1312.SubServers.Sync.Library.Util;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Server Class
 */
public class ServerContainer extends BungeeServerInfo {
    private String subdata;
    private List<UUID> whitelist = new ArrayList<UUID>();
    private String nick = null;
    private boolean hidden;
    private final String signature;

    public ServerContainer(String signature, String name, String display, InetSocketAddress address, String subdata, String motd, boolean hidden, boolean restricted, Collection<UUID> whitelist) {
        super(name, address, motd, restricted);
        if (Util.isNull(name, address, motd, hidden, restricted)) throw new NullPointerException();
        this.signature = signature;
        this.subdata = subdata;
        this.whitelist.addAll(whitelist);
        this.hidden = hidden;
        setDisplayName(display);
    }

    /**
     * Gets the SubData Client Address
     *
     * @return SubData Client Address (or null if not linked)
     */
    public String getSubData() {
        return subdata;
    }

    /**
     * Sets the SubData Client Address
     *
     * @param subdata SubData Client Address (null represents not linked)
     */
    public void setSubData(String subdata) {
        this.subdata = subdata;
    }

    /**
     * Get the Display Name of this Server
     *
     * @return Display Name
     */
    public String getDisplayName() {
        return (nick == null)?getName():nick;
    }

    /**
     * Sets the Display Name for this Server
     *
     * @param value Value (or null to reset)
     */
    public void setDisplayName(String value) {
        if (value == null || value.length() == 0 || getName().equals(value)) {
            this.nick = null;
        } else {
            this.nick = value;
        }
    }

    /**
     * See if a player is whitelisted
     *
     * @param player Player
     * @return Whitelisted Status
     */
    public boolean canAccess(CommandSender player) {
        return (player instanceof ProxiedPlayer && whitelist.contains(((ProxiedPlayer) player).getUniqueId())) || super.canAccess(player);
    }

    /**
     * Add a player to the whitelist (for use with restricted servers)
     *
     * @param player Player to add
     */
    public void whitelist(UUID player) {
        if (Util.isNull(player)) throw new NullPointerException();
        whitelist.add(player);
    }

    /**
     * Remove a player to the whitelist
     *
     * @param player Player to remove
     */
    public void unwhitelist(UUID player) {
        whitelist.remove(player);
    }

    /**
     * If the server is hidden from players
     *
     * @return Hidden Status
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Set if the server is hidden from players
     *
     * @param value Value
     */
    public void setHidden(boolean value) {
        if (Util.isNull(value)) throw new NullPointerException();
        this.hidden = value;
    }

    /**
     * Sets the MOTD of the Server
     *
     * @param value Value
     */
    public void setMotd(String value) {
        if (Util.isNull(value)) throw new NullPointerException();
        try {
            Util.reflect(BungeeServerInfo.class.getDeclaredField("motd"), this, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets if the Server is Restricted
     *
     * @param value Value
     */
    public void setRestricted(boolean value) {
        if (Util.isNull(value)) throw new NullPointerException();
        try {
            Util.reflect(BungeeServerInfo.class.getDeclaredField("restricted"), this, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the Signature of this Object
     *
     * @return Object Signature
     */
    public final String getSignature() {
        return signature;
    }
}
