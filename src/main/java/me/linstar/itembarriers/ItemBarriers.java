/*
 * Copyright (c) Linstar 2025.
 */

package me.linstar.itembarriers;

import com.mojang.logging.LogUtils;
import me.linstar.itembarriers.command.EnchantBarriersCommand;
import me.linstar.itembarriers.command.ItemBarriersCommand;
import me.linstar.itembarriers.item.BannedItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(ItemBarriers.MOD_ID)
public class ItemBarriers {
    public static final String MOD_ID = "itembarriers";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Item> BANNED_ITEM = ITEMS.register(BannedItem.NAME, BannedItem::new);

    private int timer = 0;

    public ItemBarriers() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        ITEMS.register(bus);
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event){
        ItemBarriersCommand.register(event.getDispatcher());
        EnchantBarriersCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event){
        BannedItemsData.register(event.getServer());
        BannedEnchantmentsData.register(event.getServer());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemCraft(PlayerEvent.ItemCraftedEvent event){
        var player = event.getEntity();
        if (player.isLocalPlayer()) return;

        if (BannedItemsData.get().contains(event.getCrafting().getItem())) {
            player.sendSystemMessage(Component.translatable("info.itembarriers.crafting.banned").withStyle(ChatFormatting.RED));
            player.playNotifySound(SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 1.0F, 1.0F);
            event.getCrafting().setCount(0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onItemSpawn(EntityJoinLevelEvent event){
        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity itemEntity){
            var item = itemEntity.getItem().getItem();
            if (BannedItemsData.get().contains(item)){
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event){
        if(event.side.isClient()){
            return;
        }

        timer ++;

        if (timer >= 20) {
            Player player = event.player;
            NonNullList<ItemStack> stacks = player.getInventory().armor;
            removeEnchants(player.getMainHandItem());
            removeEnchants(player.getOffhandItem());
            for (ItemStack stack : stacks) {
                removeEnchants(stack);
            }

            timer = 0;
        }
    }

    @SubscribeEvent
    public void blockInteract(PlayerInteractEvent.RightClickBlock event){
        ItemBarriers.handleInteract(event);
    }

    @SubscribeEvent
    public void blockInteract(PlayerInteractEvent.LeftClickBlock event){
        ItemBarriers.handleInteract(event);
    }

    private static void handleInteract(PlayerInteractEvent event){
        var player = event.getEntity();
        if (player.isLocalPlayer()) return;

        var block = event.getLevel().getBlockState(event.getPos()).getBlock();
        if (BannedItemsData.get().contains(block)) {
            event.setCanceled(true);
            event.getLevel().removeBlock(event.getPos(), false);
        }
    }

    public static void removeEnchants(ItemStack stack){
        if (stack.isEmpty()){
            return;
        }

        ListTag enchantTags = stack.getEnchantmentTags();

        stack.getAllEnchantments().forEach((enchantment, level) -> {
            if (BannedEnchantmentsData.get().contains(enchantment)) {
                CompoundTag enchantTag = EnchantmentHelper.storeEnchantment(ForgeRegistries.ENCHANTMENTS.getKey(enchantment), level);
                enchantTags.remove(enchantTag);
            }
        });
    }
}
