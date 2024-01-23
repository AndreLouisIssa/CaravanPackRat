package net.caravanpackrat.forge;

import dev.architectury.platform.forge.EventBuses;
import net.caravanpackrat.CaravanPackRatMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CaravanPackRatMod.MOD_ID)
public class CaravanPackRatForge {
    public CaravanPackRatForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(CaravanPackRatMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        CaravanPackRatMod.init();
    }
}
