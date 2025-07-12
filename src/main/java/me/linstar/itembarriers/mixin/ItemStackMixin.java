/*
 * Copyright (c) Linstar 2025.
 */

package me.linstar.itembarriers.mixin;

import me.linstar.itembarriers.BannedItemsData;
import me.linstar.itembarriers.item.BannedItem;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

import static me.linstar.itembarriers.ItemBarriers.BANNED_ITEM;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Mutable
    @Shadow @Final @Deprecated @Nullable private Item item;

    @Shadow public abstract void setTag(@org.jetbrains.annotations.Nullable CompoundTag p_41752_);

    @Shadow public abstract CompoundTag getOrCreateTag();

    @Shadow private CompoundTag capNBT;

    @Shadow private int count;

    @Shadow public abstract CompoundTag save(CompoundTag p_41740_);

    @Mutable
    @Shadow @Final @org.jetbrains.annotations.Nullable private Holder.@org.jetbrains.annotations.Nullable Reference<Item> delegate;

    @Inject(method = "inventoryTick", at = @At("TAIL"))
    public void inventoryTick(Level level, Entity entity, int p_41669_, boolean p_41670_, CallbackInfo ci){
        if (level.isClientSide) return;
        if ((entity instanceof Player player) && player.hasPermissions(4)) return;
        if (delegate == null) return;

        if (BannedItemsData.get().contains(delegate.get())) {
            CompoundTag data = this.save(new CompoundTag());

            this.item = BANNED_ITEM.get();
            this.delegate = ForgeRegistries.ITEMS.getDelegateOrThrow(BANNED_ITEM.get());
            this.capNBT = new CompoundTag();
            this.count = 1;

            this.setTag(new CompoundTag());
            this.getOrCreateTag().put(BannedItem.DATA_KEY, data);
        }
    }
}
