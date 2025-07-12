/*
 * Copyright (c) Linstar 2025.
 */

package me.linstar.itembarriers.item;

import com.mojang.blaze3d.vertex.PoseStack;
import me.linstar.itembarriers.BannedItemsData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class BannedItem extends Item {
    public static final String NAME = "banned_item";
    public static final String DATA_KEY = "SourceData";

    public BannedItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }

        var sourceStack = ItemStack.of(stack.getOrCreateTagElement(DATA_KEY));
        if (sourceStack.isEmpty()) {
            player.sendSystemMessage(Component.translatable("info.itembarriers.item.remove"));
            return InteractionResultHolder.success(sourceStack);
        }

        if (player.hasPermissions(4)){
            player.sendSystemMessage(Component.translatable("info.itembarriers.item.pardon_by_operator"));
            stack.setCount(0);
            return InteractionResultHolder.success(sourceStack);
        }

        if (BannedItemsData.get().contains(sourceStack.getItem())) {
            player.sendSystemMessage(Component.translatable("info.itembarriers.item.steal_banned"));
            player.playNotifySound(SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResultHolder.success(stack);
        }else {
            player.sendSystemMessage(Component.translatable("info.itembarriers.item.pardon"));
            player.playNotifySound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.BLOCKS, 1.0F, 1.0F);
            stack.setCount(0);
            return InteractionResultHolder.success(sourceStack);
        }
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new BannedItemCustomRenderer();
            }
        });
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level p_41422_, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
        var sourceStack = ItemStack.of(stack.getOrCreateTagElement(DATA_KEY));
        components.add(Component.translatable("info.itembarriers.item.info"));
        sourceStack.getTooltipLines(null, flag).forEach(line -> components.add(Component.literal("  ").append(line)));
    }

    static class BannedItemCustomRenderer extends BlockEntityWithoutLevelRenderer {

        public BannedItemCustomRenderer() {
            super(null, null);
        }

        @Override
        public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext context, @NotNull PoseStack poseStack, @NotNull MultiBufferSource source, int light, int overlay) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            ItemStack sourceStack = getSourceItem(stack);
            BakedModel model = itemRenderer.getModel(sourceStack,null,null,1);
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F, 0.5F);
            itemRenderer.render(getSourceItem(stack), context, false, poseStack, source, light, overlay, model);
            poseStack.popPose();
        }

        private ItemStack getSourceItem(ItemStack stack) {
            var data = stack.getOrCreateTagElement(DATA_KEY);
            return ItemStack.of(data);
        }
    }
}
