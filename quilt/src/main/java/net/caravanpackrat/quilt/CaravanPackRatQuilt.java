package net.caravanpackrat.quilt;

import net.caravanpackrat.fabriclike.CaravanPackRatFabricLike;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class CaravanPackRatQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        CaravanPackRatFabricLike.init();
    }
}
