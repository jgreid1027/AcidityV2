package ac.grim.grimac.manager;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.events.CommandExecuteEvent;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.events.packets.ProxyAlertMessenger;
import ac.grim.grimac.mitigation.PlayerTrustFactor;
import ac.grim.grimac.mitigation.TrustFactorCheckType;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import github.scarsz.configuralize.DynamicConfig;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PunishmentManager {
    GrimPlayer player;
    List<PunishGroup> groups = new ArrayList<>();
    String experimentalSymbol = "*";

    public PunishmentManager(GrimPlayer player) {
        this.player = player;
        reload();
    }

    public void reload() {
        DynamicConfig config = GrimAPI.INSTANCE.getConfigManager().getConfig();
        List<String> punish = config.getStringListElse("Punishments", new ArrayList<>());
        experimentalSymbol = config.getStringElse("experimental-symbol", "*");

        try {
            groups.clear();

            // To support reloading
            for (AbstractCheck check : player.checkManager.allChecks.values()) {
                check.setEnabled(false);
            }

            for (Object s : punish) {
                LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) s;

                List<String> checks = (List<String>) map.getOrDefault("checks", new ArrayList<>());
                List<String> commands = (List<String>) map.getOrDefault("commands", new ArrayList<>());
                int removeViolationsAfter = (int) map.getOrDefault("remove-violations-after", 300);

                List<ParsedCommand> parsed = new ArrayList<>();
                List<AbstractCheck> checksList = new ArrayList<>();
                List<AbstractCheck> excluded = new ArrayList<>();
                for (String command : checks) {
                    command = command.toLowerCase(Locale.ROOT);
                    boolean exclude = false;
                    if (command.startsWith("!")) {
                        exclude = true;
                        command = command.substring(1);
                    }
                    for (AbstractCheck check : player.checkManager.allChecks.values()) { // o(n) * o(n)?
                        if (check.getCheckName() != null &&
                                (check.getCheckName().toLowerCase(Locale.ROOT).contains(command)
                                        || check.getAlternativeName().toLowerCase(Locale.ROOT).contains(command))) { // Some checks have equivalent names like AntiKB and AntiKnockback
                            if (exclude) {
                                excluded.add(check);
                            } else {
                                checksList.add(check);
                                check.setEnabled(true);
                            }
                        }
                    }
                    for (AbstractCheck check : excluded) checksList.remove(check);
                }

                for (String command : commands) {
                    String firstNum = command.substring(0, command.indexOf(":"));
                    String secondNum = command.substring(command.indexOf(":"), command.indexOf(" "));

                    int threshold = Integer.parseInt(firstNum);
                    int interval = Integer.parseInt(secondNum.substring(1));
                    String commandString = command.substring(command.indexOf(" ") + 1);

                    parsed.add(new ParsedCommand(threshold, interval, commandString));
                }

                groups.add(new PunishGroup(checksList, parsed, removeViolationsAfter));
            }
        } catch (Exception e) {
            LogUtil.error("Error while loading punishments.yml! This is likely your fault!");
            e.printStackTrace();
        }
    }

    private String replaceAlertPlaceholders(String original, PunishGroup group, Check check, String alertString, String verbose) {
        // Streams are slow but this isn't a hot path... it's fine.
        String vl = group.violations.values().stream().filter((e) -> e == check).count() + "";

        original = MessageUtil.format(original
                .replace("[alert]", alertString)
                .replace("[proxy]", alertString)
                .replace("%check_name%", check.getCheckName())
                .replace("%experimental%", check.isExperimental() ? experimentalSymbol : "")
                .replace("%vl%", vl)
                .replace("%verbose%", verbose)
                .replace("%description%", check.getDescription())
        );

        original = GrimAPI.INSTANCE.getExternalAPI().replaceVariables(player, original, true);

        return original;
    }

    public boolean handleAlert(GrimPlayer player, String verbose, Check check) {
        // GrimAPI.INSTANCE.getConfigManager().getConfig().getStringElse("alerts-format", "%prefix% &f%player% &bfailed &f%check_name% &f(x&c%vl%&f) &7%verbose%");
        boolean testMode = GrimAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse("test-mode", false);
        boolean sentDebug = false;

        // Check commands
        for (PunishGroup group : groups) {
            if (group.getChecks().contains(check)) {
                int violationCount = group.getViolations().size();
                for (ParsedCommand command : group.getCommands()) {
                    // Verbose that prints all flags
                    if (command.command.equals("[alert]")) {
                        sentDebug = true;

                        if (check.getTrustFactorType() == TrustFactorCheckType.COMBAT) {
                            if (player.combatTrustFactor == PlayerTrustFactor.GREEN) {
                                for (Player bukkitPlayer : GrimAPI.INSTANCE.getAlertManager().getEnabledVerbose()) {
                                    String alertString = "&4[Acidity]: &f%player% failed %check_name% [" + "&2GREEN" + "&f] &f(x&c%vl%&f) &7%verbose%";
                                    String cmd = replaceAlertPlaceholders(command.getCommand(), group, check, alertString, verbose);

                                    bukkitPlayer.sendMessage(cmd);
                                }
                            }
                            if (player.combatTrustFactor == PlayerTrustFactor.YELLOW) {
                                for (Player bukkitPlayer : GrimAPI.INSTANCE.getAlertManager().getEnabledAlerts()) {
                                    String alertString = "&4[Acidity]: &f%player% failed %check_name% [" + "&gYELLOW" + "&f] &f(x&c%vl%&f) &7%verbose%";
                                    String cmd = replaceAlertPlaceholders(command.getCommand(), group, check, alertString, verbose);

                                    bukkitPlayer.sendMessage(cmd);
                                }
                            }
                            if (player.combatTrustFactor == PlayerTrustFactor.ORANGE) {
                                for (Player bukkitPlayer : GrimAPI.INSTANCE.getAlertManager().getEnabledAlerts()) {
                                    String alertString = "&4[Acidity]: &f%player% failed %check_name% [" + "&6ORANGE" + "&f] &f(x&c%vl%&f) &7%verbose%";
                                    String cmd = replaceAlertPlaceholders(command.getCommand(), group, check, alertString, verbose);

                                    bukkitPlayer.sendMessage(cmd);
                                }
                            }
                            if (player.combatTrustFactor == PlayerTrustFactor.RED) {
                                for (Player bukkitPlayer : GrimAPI.INSTANCE.getAlertManager().getEnabledAlerts()) {
                                    String alertString = "&4[Acidity]: &f%player% failed %check_name% [" + "&4RED" + "&f] &f(x&c%vl%&f) &7%verbose%";
                                    String cmd = replaceAlertPlaceholders(command.getCommand(), group, check, alertString, verbose);

                                    bukkitPlayer.sendMessage(cmd);
                                }
                            }
                        }
                        else if (check.getTrustFactorType() == TrustFactorCheckType.MOVEMENT) {
                            if (player.movementTrustFactor == PlayerTrustFactor.GREEN) {
                                for (Player bukkitPlayer : GrimAPI.INSTANCE.getAlertManager().getEnabledVerbose()) {
                                    String alertString = "&4[Acidity]: &f%player% failed %check_name% [" + "&2GREEN" + "&f] &f(x&c%vl%&f) &7%verbose%";
                                    String cmd = replaceAlertPlaceholders(command.getCommand(), group, check, alertString, verbose);

                                    bukkitPlayer.sendMessage(cmd);
                                }
                            }
                            if (player.movementTrustFactor == PlayerTrustFactor.YELLOW) {
                                for (Player bukkitPlayer : GrimAPI.INSTANCE.getAlertManager().getEnabledAlerts()) {
                                    String alertString = "&4[Acidity]: &f%player% failed %check_name% [" + "&gYELLOW" + "&f] &f(x&c%vl%&f) &7%verbose%";
                                    String cmd = replaceAlertPlaceholders(command.getCommand(), group, check, alertString, verbose);

                                    bukkitPlayer.sendMessage(cmd);
                                }
                            }
                            if (player.movementTrustFactor == PlayerTrustFactor.ORANGE) {
                                for (Player bukkitPlayer : GrimAPI.INSTANCE.getAlertManager().getEnabledAlerts()) {
                                    String alertString = "&4[Acidity]: &f%player% failed %check_name% [" + "&6ORANGE" + "&f] &f(x&c%vl%&f) &7%verbose%";
                                    String cmd = replaceAlertPlaceholders(command.getCommand(), group, check, alertString, verbose);

                                    bukkitPlayer.sendMessage(cmd);
                                }
                            }
                            if (player.movementTrustFactor == PlayerTrustFactor.RED) {
                                for (Player bukkitPlayer : GrimAPI.INSTANCE.getAlertManager().getEnabledAlerts()) {
                                    String alertString = "&4[Acidity]: &f%player% failed %check_name% [" + "&4RED" + "&f] &f(x&c%vl%&f) &7%verbose%";
                                    String cmd = replaceAlertPlaceholders(command.getCommand(), group, check, alertString, verbose);

                                    bukkitPlayer.sendMessage(cmd);
                                }
                            }
                        }
                        else {
                            for (Player bukkitPlayer : GrimAPI.INSTANCE.getAlertManager().getEnabledAlerts()) {
                                String alertString = "&4[Acidity]: &f%player% failed %check_name% [" + "N/A" + "] &f(x&c%vl%&f) &7%verbose%";
                                String cmd = replaceAlertPlaceholders(command.getCommand(), group, check, alertString, verbose);

                                bukkitPlayer.sendMessage(cmd);
                            }
                        }
                    }
                    /*
                    if (violationCount >= command.getThreshold()) {
                        // 0 means execute once
                        // Any other number means execute every X interval
                        boolean inInterval = command.getInterval() == 0 ? (command.executeCount == 0) : (violationCount % command.getInterval() == 0);
                        if (inInterval) {
                            CommandExecuteEvent executeEvent = new CommandExecuteEvent(player, check, cmd);
                            Bukkit.getPluginManager().callEvent(executeEvent);
                            if (executeEvent.isCancelled()) continue;

                            if (command.command.equals("[webhook]")) {
                                String vl = group.violations.values().stream().filter((e) -> e == check).count() + "";
                                GrimAPI.INSTANCE.getDiscordManager().sendAlert(player, verbose, check.getCheckName(), vl);
                            } else if (command.command.equals("[proxy]")) {
                                String proxyAlertString = GrimAPI.INSTANCE.getConfigManager().getConfig().getStringElse("alerts-format-proxy", "%prefix% &f[&cproxy&f] &f%player% &bfailed &f%check_name% &f(x&c%vl%&f) &7%verbose%");
                                proxyAlertString = replaceAlertPlaceholders(command.getCommand(), group, check, proxyAlertString, verbose);
                                ProxyAlertMessenger.sendPluginMessage(proxyAlertString);
                            } else {
                                if (command.command.equals("[alert]")) {
                                    sentDebug = true;
                                    if (testMode) { // secret test mode
                                        player.user.sendMessage(cmd);
                                        continue;
                                    }
                                    cmd = "grim sendalert " + cmd; // Not test mode, we can add the command prefix
                                }

                                String finalCmd = cmd;
                                FoliaScheduler.getGlobalRegionScheduler().run(GrimAPI.INSTANCE.getPlugin(), (dummy) -> {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                                });
                            }
                        }

                        command.setExecuteCount(command.getExecuteCount() + 1);
                    }
                    */
                }
            }
        }
        return sentDebug;
    }

    public void handleViolation(Check check) {
        for (PunishGroup group : groups) {
            if (group.getChecks().contains(check)) {
                long currentTime = System.currentTimeMillis();

                group.violations.put(currentTime, check);
                // Remove violations older than the defined time in the config
                group.violations.entrySet().removeIf(time -> currentTime - time.getKey() > group.removeViolationsAfter);
            }
        }
    }
}

class PunishGroup {
    @Getter
    List<AbstractCheck> checks;
    @Getter
    List<ParsedCommand> commands;
    @Getter
    HashMap<Long, Check> violations = new HashMap<>();
    @Getter
    int removeViolationsAfter;

    public PunishGroup(List<AbstractCheck> checks, List<ParsedCommand> commands, int removeViolationsAfter) {
        this.checks = checks;
        this.commands = commands;
        this.removeViolationsAfter = removeViolationsAfter * 1000;
    }
}

class ParsedCommand {
    @Getter
    int threshold;
    @Getter
    int interval;
    @Getter
    @Setter
    int executeCount;
    @Getter
    String command;

    public ParsedCommand(int threshold, int interval, String command) {
        this.threshold = threshold;
        this.interval = interval;
        this.command = command;
    }
}
