package net.ME1312.SubServers.Host;

import net.ME1312.SubServers.Host.API.Command;
import net.ME1312.SubServers.Host.API.SubPluginInfo;
import net.ME1312.SubServers.Host.Library.Config.YAMLSection;
import net.ME1312.SubServers.Host.Library.TextColor;
import net.ME1312.SubServers.Host.Library.Util;
import net.ME1312.SubServers.Host.Library.Version.Version;
import net.ME1312.SubServers.Host.Network.API.Host;
import net.ME1312.SubServers.Host.Network.API.Proxy;
import net.ME1312.SubServers.Host.Network.API.Server;
import net.ME1312.SubServers.Host.Network.API.SubServer;
import net.ME1312.SubServers.Host.Network.Packet.*;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Command Class
 */
public class SubCommand {
    private SubCommand() {}
    protected static void load(ExHost host) {
        new Command(null) {
            @Override
            public void command(String handle, String[] args) {
                if (args.length == 0 || host.api.plugins.get(args[0].toLowerCase()) != null) {
                    host.log.message.println(
                            "These are the platforms and versions that are running " + ((args.length == 0)?"SubServers.Host":host.api.plugins.get(args[0].toLowerCase()).getName()) +":",
                            "  " + System.getProperty("os.name") + ' ' + System.getProperty("os.version") + ',',
                            "  Java " + System.getProperty("java.version") + ',',
                            "  SubServers.Host v" + host.version.toExtendedString() + ((host.api.getAppBuild() != null)?" (" + host.api.getAppBuild() + ')':""));
                    if (args.length == 0) {
                        host.log.message.println("");
                        new Thread(() -> {
                            try {
                                YAMLSection tags = new YAMLSection(new JSONObject("{\"tags\":" + Util.readAll(new BufferedReader(new InputStreamReader(new URL("https://api.github.com/repos/ME1312/SubServers-2/git/refs/tags").openStream(), Charset.forName("UTF-8")))) + '}'));
                                List<Version> versions = new LinkedList<Version>();

                                Version updversion = host.version;
                                int updcount = 0;
                                for (YAMLSection tag : tags.getSectionList("tags")) versions.add(Version.fromString(tag.getString("ref").substring(10)));
                                Collections.sort(versions);
                                for (Version version : versions) {
                                    if (version.compareTo(updversion) > 0) {
                                        updversion = version;
                                        updcount++;
                                    }
                                }
                                if (updcount == 0) {
                                    host.log.message.println("You are on the latest version.");
                                } else {
                                    host.log.message.println("SubServers.Host v" + updversion + " is available. You are " + updcount + " version" + ((updcount == 1)?"":"s") + " behind.");
                                }
                            } catch (Exception e) {}
                        }).start();
                    } else {
                        SubPluginInfo plugin = host.api.plugins.get(args[0].toLowerCase());
                        String title = "  " + plugin.getName() + " v" + plugin.getVersion().toExtendedString();
                        String subtitle = "    by ";
                        int i = 0;
                        for (String author : plugin.getAuthors()) {
                            i++;
                            if (i > 1) {
                                if (plugin.getAuthors().size() > 2) subtitle += ", ";
                                else if (plugin.getAuthors().size() == 2) subtitle += ' ';
                                if (i == plugin.getAuthors().size()) subtitle += "and ";
                            }
                            subtitle += author;
                        }
                        if (plugin.getWebsite() != null) {
                            if (title.length() > subtitle.length() + 5 + plugin.getWebsite().toString().length()) {
                                i = subtitle.length();
                                while (i < title.length() - plugin.getWebsite().toString().length() - 2) {
                                    i++;
                                    subtitle += ' ';
                                }
                            } else {
                                subtitle += " - ";
                            }
                            subtitle += plugin.getWebsite().toString();
                        }
                        host.log.message.println(title, subtitle);
                        if (plugin.getDescription() != null) host.log.message.println("", plugin.getDescription());
                    }
                } else {
                    host.log.message.println("There is no plugin with that name");
                }
            }
        }.usage("[plugin]").description("Gets the version of the System and SubServers or the specified Plugin").help(
                "This command will print what OS you're running, your OS version,",
                "your Java version, and the SubServers.Host version.",
                "",
                "If the [plugin] option is provided, it will print information about the specified plugin as well.",
                "",
                "Examples:",
                "  /version",
                "  /version ExamplePlugin"
        ).register("ver", "version");
        new Command(null) {
            @Override
            public void command(String handle, String[] args) {
                host.api.getGroups(groups -> host.api.getHosts(hosts -> host.api.getServers(servers -> host.api.getMasterProxy(proxymaster -> host.api.getProxies(proxies -> {
                    int i = 0;
                    boolean sent = false;
                    String div = TextColor.RESET + ", ";
                    if (groups.keySet().size() > 0) {
                        host.log.message.println("Group/Server List:");
                        for (String group : groups.keySet()) {
                            String message = "  ";
                            message += TextColor.GOLD + group + TextColor.RESET + ": ";
                            for (Server server : groups.get(group)) {
                                if (i != 0) message += div;
                                if (!(server instanceof SubServer)) {
                                    message += TextColor.WHITE;
                                } else if (((SubServer) server).isTemporary()) {
                                    message += TextColor.AQUA;
                                } else if (((SubServer) server).isRunning()) {
                                    message += TextColor.GREEN;
                                } else if (((SubServer) server).isEnabled() && ((SubServer) server).getCurrentIncompatibilities().size() == 0) {
                                    message += TextColor.YELLOW;
                                } else {
                                    message += TextColor.RED;
                                }
                                message += server.getDisplayName() + " (" + server.getAddress().getAddress().getHostAddress()+':'+server.getAddress().getPort() + ((server.getName().equals(server.getDisplayName())) ? "" : TextColor.stripColor(div) + server.getName()) + ")";
                                i++;
                            }
                            if (i == 0) message += TextColor.RESET + "(none)";
                            host.log.message.println(message);
                            i = 0;
                            sent = true;
                        }
                        if (!sent) host.log.message.println(TextColor.RESET + "(none)");
                        sent = false;
                    }
                    ExHost h = host;
                    host.log.message.println("Host/SubServer List:");
                    for (Host host : hosts.values()) {
                        String message = "  ";
                        if (host.isEnabled()) {
                            message += TextColor.AQUA;
                        } else {
                            message += TextColor.RED;
                        }
                        message += host.getDisplayName() + " (" + host.getAddress().getHostAddress() + ((host.getName().equals(host.getDisplayName()))?"":TextColor.stripColor(div)+host.getName()) + ")" + TextColor.RESET + ": ";
                        for (SubServer subserver : host.getSubServers().values()) {
                            if (i != 0) message += div;
                            if (subserver.isTemporary()) {
                                message += TextColor.AQUA;
                            } else if (subserver.isRunning()) {
                                message += TextColor.GREEN;
                            } else if (subserver.isEnabled() && subserver.getCurrentIncompatibilities().size() == 0) {
                                message += TextColor.YELLOW;
                            } else {
                                message += TextColor.RED;
                            }
                            message += subserver.getDisplayName() + " (" + subserver.getAddress().getPort() + ((subserver.getName().equals(subserver.getDisplayName()))?"":TextColor.stripColor(div)+subserver.getName()) + ")";
                            i++;
                        }
                        if (i == 0) message += TextColor.RESET + "(none)";
                        h.log.message.println(message);
                        i = 0;
                        sent = true;
                    }
                    if (!sent) host.log.message.println(TextColor.RESET + "(none)");
                    host.log.message.println("Server List:");
                    String message = "  ";
                    for (Server server : servers.values()) if (!(server instanceof SubServer)) {
                        if (i != 0) message += div;
                        message += TextColor.WHITE + server.getDisplayName() + " (" + server.getAddress().getAddress().getHostAddress()+':'+server.getAddress().getPort() + ((server.getName().equals(server.getDisplayName()))?"":TextColor.stripColor(div)+server.getName()) + ")";
                        i++;
                    }
                    if (i == 0) message += TextColor.RESET + "(none)";
                    host.log.message.println(message);
                    if (proxies.keySet().size() > 0) {
                        host.log.message.println("Proxy List:");
                        message = "  (master)";
                        for (Proxy proxy : proxies.values()) {
                            message += div;
                            if (proxy.getSubData() != null && proxy.isRedis()) {
                                message += TextColor.GREEN;
                            } else if (proxy.getSubData() != null) {
                                message += TextColor.AQUA;
                            } else if (proxy.isRedis()) {
                                message += TextColor.WHITE;
                            } else {
                                message += TextColor.RED;
                            }
                            message += proxy.getDisplayName() + ((proxy.getName().equals(proxy.getDisplayName()))?"":" ("+proxy.getName()+')');
                        }
                        host.log.message.println(message);
                    }
                })))));
            }
        }.description("Lists the available Hosts and Servers").help(
                "This command will print a list of the available Hosts and Servers.",
                "You can then use these names in commands where applicable.",
                "",
                "Example:",
                "  /list"
        ).register("list");
        new Command(null) {
            @Override
            public void command(String handle, String[] args) {
                if (args.length > 0) {
                    host.api.getServer(args[0], server -> {
                        ExHost h = host;
                        if (server == null) {
                            h.log.message.println("There is no server with that name");
                        } else if (!(server instanceof SubServer)) {
                            h.log.message.println("That Server is not a SubServer");
                        } else ((SubServer) server).getHost(host -> {
                            if (host == null) {
                                h.log.message.println("That Server is not a SubServer");
                            } else {
                                h.log.message.println("Info on " + server.getDisplayName() + ':');
                                if (!server.getName().equals(server.getDisplayName())) h.log.message.println("  - Real Name: " + server.getName());
                                h.log.message.println("  - Host: " + host.getName());
                                h.log.message.println("  - Enabled: " + ((((SubServer) server).isEnabled())?"yes":"no"));
                                h.log.message.println("  - Editable: " + ((((SubServer) server).isEditable())?"yes":"no"));
                                if (server.getGroups().size() > 0) {
                                    h.log.message.println("  - Group:");
                                    for (String group : server.getGroups())
                                        h.log.message.println("    - " + group);
                                }
                                if (((SubServer) server).isTemporary()) h.log.message.println("  - Temporary: yes");
                                h.log.message.println("  - Running: " + ((((SubServer) server).isRunning())?"yes":"no"));
                                h.log.message.println("  - Logging: " + ((((SubServer) server).isLogging())?"yes":"no"));
                                h.log.message.println("  - Address: " + server.getAddress().getAddress().getHostAddress()+':'+server.getAddress().getPort());
                                h.log.message.println("  - Auto Restart: " + ((((SubServer) server).willAutoRestart())?"yes":"no"));
                                h.log.message.println("  - Hidden: " + ((server.isHidden())?"yes":"no"));
                                if (((SubServer) server).getIncompatibilities().size() > 0) {
                                    List<String> current = new ArrayList<String>();
                                    for (String other : ((SubServer) server).getCurrentIncompatibilities()) current.add(other.toLowerCase());
                                    h.log.message.println("  - Incompatibilities:");
                                    for (String other : ((SubServer) server).getIncompatibilities())
                                        h.log.message.println("    - " + other + ((current.contains(other))?"*":""));
                                }
                                h.log.message.println("  - Signature: " + server.getSignature());
                            }
                        });
                    });
                } else {
                    host.log.message.println("Usage: /" + handle + " <SubServer>");
                }
            }
        }.usage("<SubServer>").description("Gets information about a SubServer").help(
                "This command will print a list of information about",
                "the specified SubServer.",
                "",
                "The <SubServer> argument is required, and should be the name of",
                "the SubServer you want to obtain information about.",
                "",
                "Example:",
                "  /info ExampleServer"
        ).register("info", "status");
        new Command(null) {
            @Override
            public void command(String handle, String[] args) {
                if (args.length > 0) {
                    host.subdata.sendPacket(new PacketStartServer(null, args[0], data -> {
                        switch (data.getInt("r")) {
                            case 3:
                                host.log.message.println("There is no server with that name");
                                break;
                            case 4:
                                host.log.message.println("That Server is not a SubServer");
                                break;
                            case 5:
                                host.log.message.println("That SubServer's Host is not enabled");
                                break;
                            case 6:
                                host.log.message.println("That SubServer is not enabled");
                                break;
                            case 7:
                                host.log.message.println("That SubServer is already running");
                                break;
                            case 8:
                                host.log.message.println("That SubServer cannot start while these server(s) are running:", data.getRawString("m").split(":\\s")[1]);
                                break;
                            case 0:
                            case 1:
                                host.log.message.println("Server was started successfully");
                                break;
                            default:
                                host.log.warn.println("PacketStartServer(null, " + args[0] + ") responded with: " + data.getRawString("m"));
                                host.log.message.println("Server was started successfully");
                                break;
                        }
                    }));
                } else {
                    host.log.message.println("Usage: /" + handle + " <SubServer>");
                }
            }
        }.usage("<SubServer>").description("Starts a SubServer").help(
                "This command is used to start a SubServer on the network.",
                "Once it has been started, you can control it via the other commands",
                "",
                "The <SubServer> argument is required, and should be the name of",
                "the SubServer you want to start.",
                "",
                "Example:",
                "  /start ExampleServer"
        ).register("start");
        new Command(null) {
            @Override
            public void command(String handle, String[] args) {
                if (args.length > 0) {
                    host.subdata.sendPacket(new PacketStopServer(null, args[0], false, data -> {
                        switch (data.getInt("r")) {
                            case 3:
                                host.log.message.println("There is no server with that name");
                                break;
                            case 4:
                                host.log.message.println("That Server is not a SubServer");
                                break;
                            case 5:
                                host.log.message.println("That SubServer is not running");
                                break;
                            case 0:
                            case 1:
                                host.log.message.println("Server was stopped successfully");
                                break;
                            default:
                                host.log.warn.println("PacketStopServer(null, " + args[0] + ", false) responded with: " + data.getRawString("m"));
                                host.log.message.println("Server was stopped successfully");
                                break;
                        }
                    }));
                } else {
                    host.log.message.println("Usage: /" + handle + " <SubServer>");
                }
            }
        }.usage("<SubServer>").description("Stops a SubServer").help(
                "This command is used to request a SubServer to stop via the network.",
                "Stopping a SubServer in this way will run the stop command",
                "specified in the server's configuration",
                "",
                "The <SubServer> argument is required, and should be the name of",
                "the SubServer you want to stop.",
                "",
                "Example:",
                "  /stop ExampleServer"
        ).register("stop");
        new Command(null) {
            @Override
            public void command(String handle, String[] args) {
                if (args.length > 0) {
                    host.subdata.sendPacket(new PacketStopServer(null, args[0], true, data -> {
                        switch (data.getInt("r")) {
                            case 3:
                                host.log.message.println("There is no server with that name");
                                break;
                            case 4:
                                host.log.message.println("That Server is not a SubServer");
                                break;
                            case 5:
                                host.log.message.println("That SubServer is not running");
                                break;
                            case 0:
                            case 1:
                                host.log.message.println("Server was terminated successfully");
                                break;
                            default:
                                host.log.warn.println("PacketStopServer(null, " + args[0] + ", true) responded with: " + data.getRawString("m"));
                                host.log.message.println("Server was terminated successfully");
                                break;
                        }
                    }));
                } else {
                    host.log.message.println("Usage: /" + handle + " <SubServer>");
                }
            }
        }.usage("<SubServer>").description("Terminates a SubServer").help(
                "This command is used to forcefully stop a SubServer on the network.",
                "Stopping a SubServer in this way can make you lose unsaved data though,",
                "so it is generally recommended to use this command only when it stops responding.",
                "",
                "The <SubServer> argument is required, and should be the name of",
                "the SubServer you want to terminate.",
                "",
                "Example:",
                "  /kill ExampleServer"
        ).register("kill", "terminate");
        new Command(null) {
            @Override
            public void command(String handle, String[] args) {
                if (args.length > 1) {
                    int i = 1;
                    String str = args[1];
                    if (args.length > 2) {
                        do {
                            i++;
                            str = str + " " + args[i];
                        } while ((i + 1) != args.length);
                    }
                    final String cmd = str;
                    host.subdata.sendPacket(new PacketCommandServer(null, args[0], cmd, data -> {
                        switch (data.getInt("r")) {
                            case 3:
                                host.log.message.println("There is no server with that name");
                                break;
                            case 4:
                                host.log.message.println("That Server is not a SubServer");
                                break;
                            case 5:
                                host.log.message.println("That SubServer is not running");
                                break;
                            case 0:
                            case 1:
                                host.log.message.println("Command was sent successfully");
                                break;
                            default:
                                host.log.warn.println("PacketCommandServer(null, " + args[0] + ", /" + cmd + ") responded with: " + data.getRawString("m"));
                                host.log.message.println("Command was sent successfully");
                                break;
                        }
                    }));
                } else {
                    host.log.message.println("Usage: /" + handle + " <SubServer> <Command> [Args...]");
                }
            }
        }.usage("<SubServer>", "<Command>", "[Args...]").description("Sends a Command to a SubServer").help(
                "This command is used to send a command to a SubServer's Console via the network.",
                "",
                "The <SubServer> argument is required, and should be the name of",
                "the SubServer you want to send a command to.",
                "",
                "The <Command> argument is required, and should be the command you",
                "want to send, the following [Args...] will be passed to that command.",
                "",
                "Examples:",
                "  /cmd ExampleServer help",
                "  /cmd ExampleServer say Hello World!"
        ).register("cmd", "command");
        new Command(null) {
            @Override
            public void command(String handle, String[] args) {
                if (args.length > 4) {
                    if (Util.isException(() -> Integer.parseInt(args[4]))) {
                        host.log.message.println("Invalid Port Number");
                    } else {
                        host.subdata.sendPacket(new PacketCreateServer(null, args[0], args[1], args[2], new Version(args[3]), Integer.parseInt(args[4]), data -> {
                            switch (data.getInt("r")) {
                                case 3:
                                    host.log.message.println("Server names cannot use spaces");
                                case 4:
                                    host.log.message.println("There is already a SubServer with that name");
                                    break;
                                case 5:
                                    host.log.message.println("There is no host with that name");
                                    break;
                                case 6:
                                    host.log.message.println("There is no template with that name");
                                    break;
                                case 7:
                                    host.log.message.println("SubCreator cannot create servers before Minecraft 1.8");
                                    break;
                                case 8:
                                    host.log.message.println("Invalid Port Number");
                                    break;
                                case 0:
                                case 1:
                                    host.log.message.println("Launching SubCreator...");
                                    break;
                                default:
                                    host.log.warn.println("PacketCreateServer(null, " + args[0] + ", " + args[1] + ", " + args[2] + ", " + args[3] + ", " + args[4] + ") responded with: " + data.getRawString("m"));
                                    host.log.message.println("Launching SubCreator...");
                                    break;
                            }
                        }));
                    }
                } else {
                    host.log.message.println("Usage: /" + handle + " <Name> <Host> <Template> <Version> <Port>");
                }
            }
        }.usage("<Name>", "<Host>", "<Template>", "<Version>", "<Port>").description("Creates a SubServer").help(
                "This command is used to create and launch a SubServer on the specified host via the network.",
                "Templates are downloaded from SubServers.Bungee to ~/Templates.",
                "",
                "The <Name> argument is required, and should be the name of",
                "the SubServer you want to create.",
                "",
                "The <Host> argument is required, and should be the name of",
                "the host you want to the server to run on.",
                "",
                "The <Template> argument is required, and should be the name of",
                "the template you want to create your server with.",
                "",
                "The <Version> argument is required, and should be a version",
                "string of the type of server that you want to create",
                "",
                "The <Port> argument is required, and should be the port number",
                "that you want the server to listen on after it has been created.",
                "",
                "Examples:",
                "  /create ExampleServer ExampleHost Spigot 1.11 25565"
        ).register("create");
        new Command(null) {
            public void command(String handle, String[] args) {
                HashMap<String, String> commands = new LinkedHashMap<String, String>();
                HashMap<Command, String> handles = new LinkedHashMap<Command, String>();

                int length = 0;
                for(String command : host.api.commands.keySet()) {
                    String formatted = "/ ";
                    Command cmd = host.api.commands.get(command);
                    String alias = (handles.keySet().contains(cmd))?handles.get(cmd):null;

                    if (alias != null) formatted = commands.get(alias);
                    if (cmd.usage().length == 0 || alias != null) {
                        formatted = formatted.replaceFirst("\\s", ((alias != null)?"|":"") + command + ' ');
                    } else {
                        String usage = "";
                        for (String str : cmd.usage()) usage += ((usage.length() == 0)?"":" ") + str;
                        formatted = formatted.replaceFirst("\\s", command + ' ' + usage + ' ');
                    }
                    if(formatted.length() > length) {
                        length = formatted.length();
                    }

                    if (alias == null) {
                        commands.put(command, formatted);
                        handles.put(cmd, command);
                    } else {
                        commands.put(alias, formatted);
                    }
                }

                if (args.length == 0) {
                    host.log.message.println("SubServers.Host Command List:");
                    for (String command : commands.keySet()) {
                        String formatted = commands.get(command);
                        Command cmd = host.api.commands.get(command);

                        while (formatted.length() < length) {
                            formatted += ' ';
                        }
                        formatted += ((cmd.description() == null || cmd.description().length() == 0)?"  ":"- "+cmd.description());

                        host.log.message.println(formatted);
                    }
                } else if (host.api.commands.keySet().contains((args[0].startsWith("/"))?args[0].toLowerCase().substring(1):args[0].toLowerCase())) {
                    Command cmd = host.api.commands.get((args[0].startsWith("/"))?args[0].toLowerCase().substring(1):args[0].toLowerCase());
                    String formatted = commands.get(Util.getBackwards(host.api.commands, cmd).get(0));
                    host.log.message.println(formatted.substring(0, formatted.length() - 1));
                    for (String line : cmd.help()) {
                        host.log.message.println("  " + line);
                    }
                } else {
                    host.log.message.println("There is no command with that name");
                }
            }
        }.usage("[command]").description("Prints a list of the commands and/or their descriptions").help(
                "This command will print a list of all currently registered commands and aliases,",
                "along with their usage and a short description.",
                "",
                "If the [command] option is provided, it will print that command, it's aliases,",
                "it's usage, and an extended description like the one you see here instead.",
                "",
                "Examples:",
                "  /help",
                "  /help end"
        ).register("help", "?");
        new Command(null) {
            @Override
            public void command(String handle, String[] args) {
                host.stop(0);
            }
        }.description("Stops this SubServers instance").help(
                "This command will shutdown this instance of SubServers.Host,",
                "SubServers running on this host, and any plugins currently running via SubAPI.",
                "",
                "Example:",
                "  /exit"
        ).register("exit", "end");
    }
}
