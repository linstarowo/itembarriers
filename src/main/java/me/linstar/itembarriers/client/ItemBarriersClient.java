/*
 * Copyright (c) Linstar 2025.
 */

package me.linstar.itembarriers.client;

import me.linstar.itembarriers.ItemBarriers;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ItemBarriersClient {
    @SubscribeEvent
    public static void onModelBaked(ModelEvent.ModifyBakingResult event) {
        var modelRegistry = event.getModels();
        var location = new ModelResourceLocation(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(ItemBarriers.BANNED_ITEM.get())), "inventory");
        BakedModel source = modelRegistry.get(location);
        event.getModels().put(location, new WrappedBakedModel(source));
    }
}
