package net.caravanpackrat.fabric;

import net.caravanpackrat.fabriclike.CaravanPackRatFabricLike;
import net.fabricmc.api.ModInitializer;

public class CaravanPackRatFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CaravanPackRatFabricLike.init();
    }
}
