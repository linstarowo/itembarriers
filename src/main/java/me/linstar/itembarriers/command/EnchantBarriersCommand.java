/*
 * Copyright (c) Linstar 2025.
 */

package me.linstar.itembarriers.command;

import com.mojang.brigadier.CommandDispatcher;
import me.linstar.itembarriers.BannedEnchantmentsData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;
import java.util.Map;

public class EnchantBarriersCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("enchantbarriers")
                .then(Commands.literal("add").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ItemStack stack = player.getMainHandItem();
                    if (stack.isEmpty()){return 0;}

                    Map<Enchantment, Integer> enchantments = stack.getAllEnchantments();
                    if (stack.getItem().equals(Items.ENCHANTED_BOOK)){
                        enchantments = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(stack));
                    }

                    enchantments.keySet().forEach(enchantment ->  {
                        if (BannedEnchantmentsData.get().contains(enchantment)){
                            player.sendSystemMessage(Component.translatable(enchantment.getDescriptionId()).withStyle(ChatFormatting.AQUA).append("附魔已经被封禁了"));
                            return;
                        }
                        BannedEnchantmentsData.get().put(enchantment);
                    });

                    player.sendSystemMessage(Component.literal("已封禁此物品包含的附魔"));
                    return 0;
                }))
                .then(Commands.literal("remove").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ItemStack stack = player.getMainHandItem();
                    if (stack.isEmpty()){return 0;}

                    Map<Enchantment, Integer> enchantments = stack.getAllEnchantments();
                    if (stack.getItem().equals(Items.ENCHANTED_BOOK)){
                        enchantments = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(stack));
                    }

                    enchantments.keySet().forEach(enchantment ->  BannedEnchantmentsData.get().remove(enchantment));

                    player.sendSystemMessage(Component.literal("已解禁此物品包含的附魔"));
                    return 0;
                }))
                .then(Commands.literal("list").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();

                    List<String> enchants = BannedEnchantmentsData.get().getBannedEnchants();

                    if (enchants.size() == 0){
                        player.sendSystemMessage(Component.literal("目前没有被封禁的附魔").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    player.sendSystemMessage(Component.literal("目前被封禁的附魔:").withStyle(ChatFormatting.GRAY));

                    MutableComponent component = null;
                    int count = 0;
                    for (String enchant : enchants){
                        if (count == 0){
                            component = Component.translatable(enchant);
                            count ++;
                            continue;
                        }

                        component.append(Component.translatable(enchant));
                        count ++;

                        if (count == 6){
                            player.sendSystemMessage(component);
                            count = 0;
                        }
                    }
                    if (component != null) {
                        player.sendSystemMessage(component);
                    }
                    return 0;
                }))

        );
    }
}
