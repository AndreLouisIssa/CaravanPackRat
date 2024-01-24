package net.caravanpackrat;

// based on: https://fabricmc.net/wiki/tutorial:persistent_states

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PlayerStateManager extends PersistentState {

    public HashMap<UUID, PlayerState> players = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerState) -> {
            NbtCompound playerNbt = new NbtCompound();

            if (playerState.depot != null) {
                playerNbt.putIntArray("depot", new int[] {playerState.depot.getX(), playerState.depot.getY(), playerState.depot.getZ()});
            }

            if (playerState.items != null) {
                Inventories.writeNbt(playerNbt, playerState.items);
            }

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);

        return nbt;
    }

    public static PlayerStateManager createFromNbt(NbtCompound tag) {
        PlayerStateManager state = new PlayerStateManager();

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerState playerState = new PlayerState();
            NbtCompound playerNbt = playersNbt.getCompound(key);

            if (playerNbt.contains("depot")) {
                int[] pos = playerNbt.getIntArray("depot");
                playerState.depot = new BlockPos(pos[0], pos[1], pos[2]);
            }

            if (playerNbt.contains("Items")) {
                int n = ((NbtList) Objects.requireNonNull(playerNbt.get("Items"))).size();
                DefaultedList<ItemStack> items = DefaultedList.ofSize(n, ItemStack.EMPTY);
                Inventories.readNbt(playerNbt, items);
                playerState.items.clear();
                playerState.items.addAll(items);
            }

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerState);
        });

        return state;
    }

    public static PlayerStateManager getServerState(MinecraftServer server) {
        // (Note: arbitrary choice to use 'World.OVERWORLD' instead of 'World.END' or 'World.NETHER'.  Any work)
        PersistentStateManager persistentStateManager = Objects.requireNonNull(server.getWorld(World.OVERWORLD)).getPersistentStateManager();

        // The first time the following 'getOrCreate' function is called, it creates a brand new 'StateSaverAndLoader' and
        // stores it inside the 'PersistentStateManager'. The subsequent calls to 'getOrCreate' pass in the saved
        // 'StateSaverAndLoader' NBT on disk to our function 'StateSaverAndLoader::createFromNbt'.
        PlayerStateManager state = persistentStateManager.getOrCreate(
                PlayerStateManager::createFromNbt,
                PlayerStateManager::new,
                "caravanpackrat.data"
        );

        // If state is not marked dirty, when Minecraft closes, 'writeNbt' won't be called and therefore nothing will be saved.
        // Technically it's 'cleaner' if you only mark state as dirty when there was actually a change, but the vast majority
        // of mod writers are just going to be confused when their data isn't being saved, and so it's best just to 'markDirty' for them.
        // Besides, it's literally just setting a bool to true, and the only time there's a 'cost' is when the file is written to disk when
        // there were no actual change to any of the mods state (INCREDIBLY RARE).
        state.markDirty();

        return state;
    }

    public static PlayerState getPlayerState(LivingEntity player) {
        MinecraftServer server = player.getWorld().getServer();
        if (server == null) return null;
        PlayerStateManager serverState = getServerState(server);

        // Either get the player by the uuid, or we don't have data for him yet, make a new player state
        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerState());
    }

    public static PlayerState getPlayerState(World world, UUID uuid) {
        MinecraftServer server = world.getServer();
        if (server == null) return null;
        PlayerStateManager serverState = getServerState(server);

        // Either get the player by the uuid, or we don't have data for him yet, make a new player state
        return serverState.players.computeIfAbsent(uuid, _uuid -> new PlayerState());
    }
}