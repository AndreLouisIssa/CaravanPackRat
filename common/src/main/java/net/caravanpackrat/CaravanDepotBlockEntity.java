package net.caravanpackrat;

// based on: https://github.com/enjarai/OmniHopper/blob/1.20.2/dev/src/main/java/nl/enjarai/omnihopper/blocks/entity/hopper/HopperBlockEntity.java

import net.minecraft.block.entity.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

import java.util.*;

import static net.caravanpackrat.CaravanPackRatMod.CARAVAN_DEPOT_BLOCK_ENTITY_TYPE;

public class CaravanDepotBlockEntity extends LootableContainerBlockEntity {

    @SuppressWarnings("unused")
    private class MutableInventory implements Inventory {

        @Override
        public int size() {
            return CaravanDepotBlockEntity.this.size();
        }

        @Override
        public boolean isEmpty() {
            return CaravanDepotBlockEntity.this.isEmpty();
        }

        @Override
        public ItemStack getStack(int slot) {
            return stacks.get(slot);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            ItemStack stack = stacks.get(slot);
            int n = stack.getCount();
            stack.decrement(amount);
            if (stack.isEmpty()) {
                return removeStack(slot);
            }
            return stack.copyWithCount(Math.min(n,amount));
        }

        public void addStack(ItemStack stack) {
            stacks.add(stack);
        }

        public void addStack(int slot, ItemStack stack) {
            stacks.add(slot, stack);
        }

        @Override
        public ItemStack removeStack(int slot) {
            return stacks.remove(slot);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            stacks.set(slot,stack);
        }

        @Override
        public void markDirty() {
            CaravanDepotBlockEntity.this.markDirty();
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return CaravanDepotBlockEntity.this.canPlayerUse(player);
        }

        @Override
        public void clear() {
            markDirty();
            for (ItemStack stack: stacks) {
                stack.setCount(0);
            }
            stacks.clear();
        }
    }

    private final MutableInventory inventory = new MutableInventory();

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return stacks.get(slot).copy();
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
    }

    @Override
    public void clear() {
    }

    private final LinkedHashSet<UUID> playerUUIDs = new LinkedHashSet<>();
    private final DefaultedList<ItemStack> stacks = DefaultedList.of();

    public CaravanDepotBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(CARAVAN_DEPOT_BLOCK_ENTITY_TYPE.get(), blockPos, blockState);
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        resolveItems(null);
        int n = stacks.size();
        DefaultedList<ItemStack> list = DefaultedList.ofSize(n, ItemStack.EMPTY);
        for (int i = 0; i < n; i++ ){
            list.set(i,stacks.get(i));
        }
        return list;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> list) {
        //stacks = list;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return null;//new HopperScreenHandler(syncId, playerInventory, inventory);
    }

    private ItemEntity dropItemIntoWorld(World world){
        // based on: https://github.com/rikka0w0/librikka/blob/382f6f79847ae666c22b9c2d6cd591207dfb4303/src/main/java/rikka/librikka/Utils.java#L51
        Random rand = new Random();
        BlockPos pos = getPos().offset(Direction.DOWN);
        BlockState state = world.getBlockState(pos);
        float dy;
        float oy;
        if (state.getBlock().canMobSpawnInside(state)) {
            dy = -0.35F;
            oy = 0.2F;
        } else {
            dy = 0.35F;
            oy = 1.2F;
        }
        ItemStack item = inventory.removeStack(0);
        if (item != null && item.getCount() > 0) {
            float rx = rand.nextFloat() * 0.8F;
            float ry = rand.nextFloat() * 0.8F + oy;
            float rz = rand.nextFloat() * 0.8F;

            ItemEntity entityItem = new ItemEntity(world,
                    pos.getX() + rx, pos.getY() + ry, pos.getZ() + rz,
                    item.copy());

            float factor = 0.05F;
            entityItem.setVelocity(
                    rand.nextGaussian() * factor,
                    rand.nextGaussian() * factor + dy,
                    rand.nextGaussian() * factor);
            world.spawnEntity(entityItem);
            return entityItem;
        }
        return null;
    }

    public void onBreak(World world, BlockPos ignoredPos, BlockState ignoredState, PlayerEntity player) {
        resolveItems(player);
        markDirty();
        while (!isEmpty()) {
            BlockPos pos = Objects.requireNonNull(dropItemIntoWorld(world)).getBlockPos();
            world.emitGameEvent(null, GameEvent.ENTITY_PLACE, pos);
        }
    }

    public ActionResult onUse(PlayerEntity player, Hand ignoredHand, BlockHitResult hit) {
        resolveItems(player);
        markDirty();
        if (player.isSneaking()) {
            BlockPos pos = hit.getBlockPos();
            PlayerState playerState = PlayerStateManager.getPlayerState(player);
            if (pos.equals(playerState.depot)) {
                return ActionResult.SUCCESS;
            }
            if (playerState.items != null) {
                if (!playerState.items.isEmpty()) {
                    player.sendMessage(Text.of("You still have pending items at your previous depot! At "
                            + playerState.depot.toShortString()));
                    return ActionResult.FAIL;
                }
            }
            playerState.depot = pos;
            playerUUIDs.add(player.getUuid());
            player.sendMessage(Text.of("You have set a new caravan depot!"));
            return ActionResult.SUCCESS;
        }
        else if (!isEmpty()) {
            World world = player.getWorld();
            ItemEntity entity = dropItemIntoWorld(world);
            if (entity != null) {
                BlockPos pos = entity.getBlockPos();
                world.emitGameEvent(null, GameEvent.ENTITY_PLACE, pos);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.FAIL;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        if (nbt.contains("Items")) {
            int n = ((NbtList)Objects.requireNonNull(nbt.get("Items"))).size();
            stacks.clear();
            DefaultedList<ItemStack> items = DefaultedList.ofSize(n,ItemStack.EMPTY);
            Inventories.readNbt(nbt, items);
            stacks.addAll(items);
        }

        if (nbt.contains("caravanpackrat:players", NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList("caravanpackrat:players", NbtElement.STRING_TYPE);
            for (NbtElement e: list){
                playerUUIDs.add(UUID.fromString(e.asString()));
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        resolveItems(null);
        super.writeNbt(nbt);

        Inventories.writeNbt(nbt, stacks);

        NbtList list = new NbtList();
        for (UUID uuid: playerUUIDs){
            list.add(NbtString.of(uuid.toString()));
        }
        nbt.put("caravanpackrat:players", list);
    }

    public void resolveItems(@Nullable LivingEntity player) {
        assert world != null;
        if (player != null) {
            PlayerState playerState = PlayerStateManager.getPlayerState(player);
            if (getPos().equals(playerState.depot)) {
                playerUUIDs.add(player.getUuid());
            } else {
                playerUUIDs.remove(player.getUuid());
            }
        }
        LinkedHashSet<UUID> toRemove = new LinkedHashSet<>();
        for (UUID uuid : playerUUIDs) {
            PlayerState playerState = PlayerStateManager.getPlayerState(world, uuid);
            BlockPos pos = getPos();
            if (playerState.depot == null || !playerState.depot.equals(pos)) {
                toRemove.add(uuid);
                continue;
            }
            DefaultedList<ItemStack> items = playerState.items;
            if (items != null) {
                for (ItemStack item : items) {
                    playerState.items = DefaultedList.of();
                    inventory.addStack(item);
                }
            }
        }
        for (UUID uuid : toRemove) {
            playerUUIDs.remove(uuid);
        }
    }

    @Nullable
    @Override
    public Text getCustomName() {
        return null;
    }

    @Override
    protected Text getContainerName() {
        return getDisplayName();
    }

    @Nullable
    @Override
    public Text getName() {
        return Text.translatable("container.caravanpackrat.caravan_depot");
    }

    public Text getDisplayName() {
        return getCustomName() != null ? getCustomName() : getName();
    }

}