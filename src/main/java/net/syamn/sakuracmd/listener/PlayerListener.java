/**
 * SakuraCmd - Package: net.syamn.sakuracmd.listener
 * Created: 2012/12/31 3:18:20
 */
package net.syamn.sakuracmd.listener;

import net.syamn.sakuracmd.SakuraCmd;
import net.syamn.utils.ItemUtil;
import net.syamn.utils.Util;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * PlayerListener (PlayerListener.java)
 *
 * @author syam(syamn)
 */
public class PlayerListener implements Listener {

    private final static String hangulRegex = "[\\x{1100}-\\x{11f9}\\x{3131}-\\x{318e}\\x{ac00}-\\x{d7a3}]";// 発言禁止正規表現

    private final SakuraCmd plugin;

    public PlayerListener(final SakuraCmd plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamagedByPlayer(final EntityDamageByEntityEvent event) {
        // Check attacker/damager is player
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        final Player target = (Player) event.getEntity();
        final Player attacker = (Player) event.getDamager();
        final ItemStack is = attacker.getItemInHand();

        if (is != null) {
            switch (is.getType()) {
                case EMERALD:
                    attacker.setItemInHand(ItemUtil.decrementItem(is, 1));

                    // 体力・空腹回復、燃えてたら消化
                    target.setHealth(target.getMaxHealth());
                    target.setFoodLevel(20);
                    target.setFireTicks(0);

                    // ポーション効果付与
                    target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 45 * 20, 0)); // 45 secs
                    target.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 7 * 60 * 20, 0)); // 7 mins
                    target.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 9 * 60 * 20, 1)); // 9 mins
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9 * 60 * 20, 0)); // 9 mins
                    target.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 5 * 60 * 20, 1)); // 5 mins
                    target.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 3 * 60 * 20, 0)); // 3 mins

                    // effect, messaging
                    target.getWorld().playEffect(target.getLocation(), Effect.ENDER_SIGNAL, 0, 10);
                    Util.message(target, attacker.getName() + " &aから特殊効果を与えられました！");
                    Util.message(attacker, target.getName() + " &aに特殊効果を与えました！");

                    event.setCancelled(true);
                    event.setDamage(0);
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Location fromLoc = event.getFrom();
        final Location toLoc = event.getTo();

        // check emerald block
        Block under = toLoc.getBlock().getRelative(BlockFace.DOWN);
        if (under.getType() == Material.EMERALD_BLOCK) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1)); // 1sec
        }

        Location diffLoc = toLoc.clone().subtract(fromLoc);
        Location checkLoc = toLoc.clone().add(diffLoc.clone().multiply(3.0D));
        checkLoc.setY(toLoc.getY());
        Block up = checkLoc.getBlock();
        Block down = up.getRelative(BlockFace.UP, 1);

        if (up.getType() == Material.DIAMOND_BLOCK || down.getType() == Material.DIAMOND_BLOCK) {
            Util.message(player, "&c Touch Diamond block!");

            final Vector dir = diffLoc.getDirection();
            Vector vect = new Vector((-(dir.getX())) * 5.0D, 2.0D, (-(dir.getZ())) * 5.0D);

            if (player.getVehicle() == null) {
                player.setVelocity(vect);
            } else {
                player.getVehicle().setVelocity(vect);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH) // ignoreCancelled = true
    public void onPlayerRightClickWithItem(final PlayerInteractEvent event) {
        if (event.useItemInHand() == org.bukkit.event.Event.Result.DENY) {
            return; // instead of ignoreCancelled = true
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack is = player.getItemInHand();
        if (is == null || is.getType().equals(Material.AIR) || player.getWorld().getEnvironment().equals(Environment.THE_END)) {
            return; // return if player not item in hand, or player on end environment
        }

        switch (is.getType()) {
            case FEATHER:
                if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                    player.setItemInHand(ItemUtil.decrementItem(is, 1));
                }
                player.setVelocity(player.getEyeLocation().getDirection().multiply(5));
        }
    }
}
