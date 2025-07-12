/*
 * Copyright (c) Linstar 2025.
 */

package me.linstar.itembarriers.command;

import com.mojang.brigadier.CommandDispatcher;
import me.linstar.itembarriers.BannedItemsData;
import me.linstar.itembarriers.ItemBarriers;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ItemBarriersCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("itembarriers")
                .then(Commands.literal("add_with_block").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ItemBarriersCommand.addItem(player, BannedItemsData.OperateType.NORMAL);
                    return 0;
                })).then(Commands.literal("add").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ItemBarriersCommand.addItem(player, BannedItemsData.OperateType.ITEM_ONLY);
                    return 0;
                }))
                .then(Commands.literal("remove").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ItemStack stack = player.getMainHandItem();
                    if (stack.isEmpty()){return 0;}

                    if (!BannedItemsData.get().contains(stack.getItem())){
                        player.sendSystemMessage(Component.translatable("info.itembarriers.command.item.not_banned").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    BannedItemsData.get().remove(stack.getItem());
                    player.sendSystemMessage(Component.translatable("info.itembarriers.command.item.removed").withStyle(ChatFormatting.RED));
                    return 0;
                }))
                .then(Commands.literal("list").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    List<String> items = BannedItemsData.get().getBannedItems();

                    if (items.isEmpty()){
                        player.sendSystemMessage(Component.translatable("info.itembarriers.command.item.list_empty").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    player.sendSystemMessage(Component.translatable("info.itembarriers.command.item.list").withStyle(ChatFormatting.GRAY));

                    MutableComponent component = Component.literal("");

                    int count = 0;
                    for (String id : items){
                        var splitId = id.split("\\.");
                        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(splitId[splitId.length - 2], splitId[splitId.length - 1]));

                        var flag = false;
                        if (item instanceof BlockItem blockItem){
                            flag = BannedItemsData.get().contains(blockItem.getBlock());
                        }

                        component.append(Component.translatable(id).withStyle(flag ? ChatFormatting.YELLOW : ChatFormatting.WHITE).append(" "));
                        count++;

                        if (count >= 6){
                            player.sendSystemMessage(component);
                        }
                    }
                    player.sendSystemMessage(component);

                    return 0;
                }))

        );
    }

    private static void addItem(ServerPlayer player ,BannedItemsData.OperateType type) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || stack.getItem().equals(ItemBarriers.BANNED_ITEM.get())) return;

        if (BannedItemsData.get().contains(stack.getItem())){
            player.sendSystemMessage(Component.translatable("info.itembarriers.command.item.already_added").withStyle(ChatFormatting.RED));
            return;
        }

        BannedItemsData.get().put(stack.getItem(), type);
        player.sendSystemMessage(Component.translatable("info.itembarriers.command.item.added").withStyle(ChatFormatting.RED));
    }
}
