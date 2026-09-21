package dev.velolib.radial;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

// Radial is client-only: all client classes live behind RadialClient so a
// dedicated server never loads them.
@Mod(RadialClient.MOD_ID)
public class RadialNeoForge {
    public RadialNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RadialClient.init(modEventBus, modContainer);
        }
    }
}
