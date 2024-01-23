package net.caravanpackrat;

// based on: https://fabricmc.net/wiki/tutorial:screenhandler and https://docs.minecraftforge.net/en/1.20.x/gui/menus/

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static net.caravanpackrat.CaravanPackRatMod.*;

public class CaravanDepositScreenHandler extends ScreenHandler {

    public class DepositInventory extends SimpleInventory {
        public DepositInventory(int size) {
            super(size);
        }

        @Override
        public void setStack(int slot, ItemStack item) {
            //super.setStack(slot,item);
            PlayerEntity player = playerInventory.player;
            World world = player.getWorld();
            if (!world.isClient) {
                PlayerState playerState = PlayerStateManager.getPlayerState(player);
                if (playerState.items != null) {
                    playerState.items.add(item);
                }
            }
        }
    }

    private final PlayerInventory playerInventory;
    private final DepositInventory depositInventory;

    public CaravanDepositScreenHandler(int id, PlayerInventory playerInventory) {
        super(CARAVAN_SCREEN_HANDLER_TYPE.get(), id);
        this.playerInventory = playerInventory;
        depositInventory = new DepositInventory(1);

        //Fake inventory
        this.addSlot(new Slot(depositInventory, 0, 80, 34));
        int m;
        int l;
        //The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 83 + m * 18));
            }
        }
        //The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 141));
        }
    }

    @Override
    public boolean insertItem(ItemStack item, int startIndex, int endingIndex, boolean fromLast){
        return true;
    }

    @Override
    public @NotNull ItemStack quickMove(PlayerEntity player, int i) {
        // TODO: fix this, considering it seems to run clientside
        ItemStack item = playerInventory.getStack(i);
        if (item == null) return ItemStack.EMPTY;
        if (insertItem(item, 0, Integer.MAX_VALUE, false)){
            /*
            if (!player.getWorld().isClient){
                depositInventory.setStack(0,item);
                playerInventory.setStack(i,ItemStack.EMPTY);
                playerInventory.markDirty();
            }
            */
            return item;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        PlayerState playerState = PlayerStateManager.getPlayerState(player);
        return playerState.depot != null;
    }
}
