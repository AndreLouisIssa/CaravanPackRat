package net.caravanpackrat.forge;

import net.caravanpackrat.ModExpectPlatform;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ModExpectPlatformImpl {
    /**
     * This is our actual method to {@link ModExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
