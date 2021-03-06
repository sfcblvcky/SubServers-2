package net.ME1312.SubServers.Bungee.Network;

import net.ME1312.SubServers.Bungee.Library.Config.YAMLSection;
import net.ME1312.SubServers.Bungee.Library.Exception.IllegalPacketException;
import net.ME1312.SubServers.Bungee.Library.Util;
import net.ME1312.SubServers.Bungee.Network.Packet.PacketAuthorization;
import org.msgpack.core.MessageInsufficientBufferException;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

/**
 * Network Client Class
 */
public class Client {
    private Socket socket;
    private InetSocketAddress address;
    private ClientHandler handler;
    private MessagePacker out;
    private Timer authorized;
    private SubDataServer subdata;
    boolean closed;

    /**
     * Network Client
     *
     * @param subdata SubData Direct Server
     * @param client Socket to Bind
     */
    public Client(SubDataServer subdata, Socket client) throws IOException {
        if (Util.isNull(subdata, client)) throw new NullPointerException();
        this.subdata = subdata;
        closed = false;
        socket = client;
        out = MessagePack.newDefaultPacker(client.getOutputStream());
        address = new InetSocketAddress(client.getInetAddress(), client.getPort());
        authorized = new Timer("SubServers.Bungee::SubData_Authorization_Timeout(" + address.toString() + ')');
        authorized.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!socket.isClosed()) try {
                    subdata.removeClient(Client.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 15000);
        loop();
    }

    /**
     * Network Loop
     */
    private void loop() {
        new Thread(() -> {
            try {
                MessageUnpacker in = MessagePack.newDefaultUnpacker(socket.getInputStream());
                Value input;
                while ((input = in.unpackValue()) != null) {
                    recievePacket(input);
                }
                try {
                    subdata.removeClient(Client.this);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (Exception e) {
                if (!(e instanceof SocketException || e instanceof MessageInsufficientBufferException)) e.printStackTrace();
                try {
                    subdata.removeClient(Client.this);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }, "SubServers.Bungee::SubData_Packet_Listener(" + address.toString() + ')').start();
    }

    private void recievePacket(Value input) {
        try {
            YAMLSection data = subdata.getCipher().decrypt(subdata.plugin.config.get().getSection("Settings").getSection("SubData").getRawString("Password"), input);
            for (PacketIn packet : SubDataServer.decodePacket(this, data)) {
                boolean auth = authorized == null;
                if (auth || packet instanceof PacketAuthorization) {
                    try {
                        if (data.contains("f")) {
                            if (data.getString("f").length() <= 0) {
                                List<Client> clients = new ArrayList<Client>();
                                clients.addAll(subdata.getClients());
                                for (Client client : clients) {
                                    client.out.packValue(input);
                                }
                            } else {
                                Client client = subdata.getClient(data.getString("f"));
                                if (client != null) {
                                    client.out.packValue(input);
                                } else {
                                    throw new IllegalPacketException(getAddress().toString() + ": Unknown Forward Address: " + data.getString("f"));
                                }
                            }
                        } else {
                            packet.execute(Client.this, (data.contains("c")) ? data.getSection("c") : null);
                        }
                    } catch (Throwable e) {
                        new InvocationTargetException(e, getAddress().toString() + ": Exception while executing PacketIn").printStackTrace();
                    }
                } else {
                    sendPacket(new PacketAuthorization(-1, "Unauthorized"));
                    throw new IllegalPacketException(getAddress().toString() + ": Unauthorized call to packet type: " + data.getSection("h"));
                }
            }
        } catch (YAMLException e) { // TODO
            new IllegalPacketException(getAddress().toString() + ": Unknown Packet Format: " + input).printStackTrace();
        } catch (IllegalPacketException e) {
            e.printStackTrace();
        } catch (Exception e) {
            new InvocationTargetException(e, getAddress().toString() + ": Exception while decoding packet").printStackTrace();
        }
    }

    /**
     * Send Packet to Client
     *
     * @param packet Packet to send
     */
    public void sendPacket(PacketOut packet) {
        if (Util.isNull(packet)) throw new NullPointerException();
        if (!isClosed()) try {
            out.packValue(subdata.getCipher().encrypt(subdata.plugin.config.get().getSection("Settings").getSection("SubData").getRawString("Password"), SubDataServer.encodePacket(this, packet)));
            out.flush();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Authorize Connection
     */
    public void authorize() {
        if (authorized != null) {
            authorized.cancel();
            System.out.println("SubData > " + socket.getRemoteSocketAddress().toString() + " logged in");
        }
        authorized = null;
    }

    /**
     * Get Raw Connection
     *
     * @return Socket
     */
    public Socket getConnection() {
        return socket;
    }

    /**
     * Get if the connection has been closed
     *
     * @return Closed Stauts
     */
    public boolean isClosed() {
        return closed && socket.isClosed();
    }

    /**
     * Get Remote Address
     *
     * @return Address
     */
    public InetSocketAddress getAddress() {
        return address;
    }

    /**
     * If the connection is authorized
     *
     * @return Authorization Status
     */
    public boolean isAuthorized() {
        return authorized == null;
    }

    /**
     * Gets the Linked Handler
     *
     * @return Handler
     */
    public ClientHandler getHandler() {
        return handler;
    }

    /**
     * Sets the Handler
     *
     * @param obj Handler
     */
    public void setHandler(ClientHandler obj) {
        if (handler != null && handler.getSubData() != null && equals(handler.getSubData())) handler.setSubData(null);
        handler = obj;
        if (handler != null && (handler.getSubData() == null || !equals(handler.getSubData()))) handler.setSubData(this);
    }

    /**
     * Disconnects the Client
     *
     * @throws IOException
     */
    public void disconnect() throws IOException {
        if (!socket.isClosed()) getConnection().close();
        if (handler != null) {
            setHandler(null);
            handler = null;
        }
        closed = true;
        if (subdata.getClients().contains(this)) subdata.removeClient(this);
    }
}
