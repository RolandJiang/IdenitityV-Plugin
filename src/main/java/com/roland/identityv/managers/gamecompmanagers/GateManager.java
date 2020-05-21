package com.roland.identityv.managers.gamecompmanagers;

import com.roland.identityv.core.IdentityV;
import com.roland.identityv.gameobjects.Gate;
import com.roland.identityv.gameobjects.RocketChair;
import com.roland.identityv.utils.Console;
import org.bukkit.Location;

import java.util.HashSet;

/**
 * Manages all gate objects
 */
public class GateManager {
    public static IdentityV plugin;

    public static HashSet<Gate> gates;

    public GateManager(final IdentityV plugin) {
        gates = new HashSet<Gate>();
    }

    public static Gate getGate(Location loc) {
        for (Gate gate : gates) {
            if (gate.getLocation().getX() == loc.getX() &&
                    gate.getLocation().getY() == loc.getY() &&
                    gate.getLocation().getZ() == loc.getZ()) {
                return gate;
            }
        }
        // If not found, setup new gate
        Console.log("Created new gate");
        Gate newGate = new Gate(plugin,loc);
        gates.add(newGate);
        return newGate;
    }
}