package de.felix.tradecycling;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.merchant.MerchantRecipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Reroll logic: in vanilla, a villager regenerates its trade offers whenever
 * its profession changes (e.g. when it claims a new workstation block). We
 * reuse that exact game-triggered mechanism: set the profession to NONE and
 * immediately back to the original -> the game itself rerolls new trades,
 * with no NMS/reflection required.
 */
public class TradeCyclingListener implements Listener {

    private final TradeCyclingPlugin plugin;
    private final Map<UUID, Long> lastReroll = new HashMap<>();

    public TradeCyclingListener(TradeCyclingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Villager villager)) return;

        Player player = event.getPlayer();

        if (!player.hasPermission("tradecycling.use")) {
            return;
        }

        boolean requireSneak = plugin.getConfig().getBoolean("require-sneak", true);
        if (requireSneak && !player.isSneaking()) {
            return;
        }

        String requiredItemName = plugin.getConfig().getString("required-item", "");
        if (requiredItemName != null && !requiredItemName.isBlank()) {
            Material required = Material.matchMaterial(requiredItemName);
            Material inHand = player.getInventory().getItemInMainHand().getType();
            if (required == null || inHand != required) {
                player.sendMessage(Messages.color(plugin.getConfig().getString("messages.wrong-item", "&cWrong item.")));
                return;
            }
        }

        // Jobless villagers (Nitwit) and babies don't have meaningful trades
        if (villager.getProfession() == Villager.Profession.NONE
                || villager.getProfession() == Villager.Profession.NITWIT
                || !villager.isAdult()) {
            return;
        }

        UUID id = villager.getUniqueId();
        long cooldownMillis = plugin.getConfig().getLong("cooldown-seconds", 5) * 1000L;
        long now = System.currentTimeMillis();
        Long last = lastReroll.get(id);
        if (last != null && now - last < cooldownMillis) {
            long remaining = (cooldownMillis - (now - last)) / 1000L + 1;
            String msg = plugin.getConfig().getString("messages.cooldown", "&cWait %seconds%s.")
                    .replace("%seconds%", String.valueOf(remaining));
            player.sendMessage(Messages.color(msg));
            event.setCancelled(true);
            return;
        }

        rerollTrades(villager);
        lastReroll.put(id, now);
        event.setCancelled(true);

        player.sendMessage(Messages.color(plugin.getConfig().getString("messages.refreshed", "&aTrades refreshed!")));

        String soundName = plugin.getConfig().getString("success-sound", "");
        if (soundName != null && !soundName.isBlank()) {
            try {
                Sound sound = Sound.valueOf(soundName);
                player.playSound(villager.getLocation(), sound, 1f, 1f);
            } catch (IllegalArgumentException ignored) {
            }
        }

        String particleName = plugin.getConfig().getString("success-particle", "");
        if (particleName != null && !particleName.isBlank()) {
            try {
                Particle particle = Particle.valueOf(particleName);
                villager.getWorld().spawnParticle(particle, villager.getLocation().add(0, 2, 0), 8, 0.3, 0.3, 0.3);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void rerollTrades(Villager villager) {
        Villager.Profession originalProfession = villager.getProfession();

        // Trick: NONE -> original forces the trade list to regenerate,
        // exactly like assigning a workstation block for the first time.
        villager.setProfession(Villager.Profession.NONE);
        villager.setProfession(originalProfession);

        if (plugin.getConfig().getBoolean("reset-uses", true)) {
            List<MerchantRecipe> recipes = villager.getRecipes();
            for (MerchantRecipe recipe : recipes) {
                recipe.setUses(0);
            }
            villager.setRecipes(recipes);
        }
    }
}
