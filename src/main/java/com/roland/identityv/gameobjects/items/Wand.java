package com.roland.identityv.gameobjects.items;

import com.mojang.authlib.GameProfile;
import com.roland.identityv.core.IdentityV;
import com.roland.identityv.gameobjects.Hunter;
import com.roland.identityv.gameobjects.Survivor;
import com.roland.identityv.managers.gamecompmanagers.HunterManager;
import com.roland.identityv.utils.Config;
import com.roland.identityv.utils.Console;
import com.roland.identityv.utils.NPCs;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class Wand extends Item {

    public static HashMap<Integer,Integer> clones = new HashMap<Integer,Integer>();

    public Wand(IdentityV plugin, Survivor s) {
        this.plugin = plugin;
        this.s = s;
        this.p = s.getPlayer();
    }

    public boolean use() {
        reduceItem();

        // Speed boost
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Config.getInt("attributes.item","wand_length"), 1, true, false),true);

        // Invis
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Config.getInt("attributes.item","wand_length"), 0, true, false),true);

        final Location location = p.getLocation();
        p.getWorld().getBlockAt(p.getLocation()).setType(Material.BARRIER);
        final LivingEntity v = (LivingEntity) p.getWorld().spawnEntity(location, EntityType.VILLAGER);
        v.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999, 0, true, false),true);

        // Keep the villager there
        new BukkitRunnable() {
            public void run() {
                if (v.isValid()) {
                    v.teleport(location);
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0,5);

        EntityPlayer npc = NPCs.spawnNPC(p, location);

        clones.put(v.getEntityId(),npc.getId());

        // Auto destroy clone later
        new BukkitRunnable() {

            public void run() {
                Wand.removeClone(v.getEntityId(),location);
            }
        }.runTaskLater(plugin, 200);

        return true;
    }


    // Removes any clone
    public static void removeClone(Integer villagerID, Location loc) {
        if (!clones.containsKey(villagerID)) return;
        // Delete villager
        for (Entity en : loc.getWorld().getNearbyEntities(loc,3,3,3)) {
            if (en.getEntityId() == villagerID) {
                loc.getWorld().getBlockAt(loc).setType(Material.AIR);
                en.remove();
                for (Player p : Console.plugin.getServer().getOnlinePlayers()) { // TODO plugin weird
                    final PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
                    PacketPlayOutEntityDestroy pa = new PacketPlayOutEntityDestroy(clones.get(villagerID));
                    connection.sendPacket(pa);
                }
                clones.remove(villagerID);
                return;
            }
        }
    }

    @Override
    public int getCD() {
        return Config.getInt("attributes.item","wand_cd");
    }
}