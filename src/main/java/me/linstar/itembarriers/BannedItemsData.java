/*
 * Copyright (c) Linstar 2025.
 */

package me.linstar.itembarriers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BannedItemsData extends SavedData {

    public enum OperateType{
        NORMAL, ITEM_ONLY
    }

    private static BannedItemsData INSTANCE = new BannedItemsData();
    private CompoundTag tag = new CompoundTag();
    private static final String NAME = "BannedItemsData";

    public BannedItemsData(){}

    public BannedItemsData(CompoundTag compoundTag){
        tag = compoundTag.getCompound(NAME);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
        compoundTag.put(NAME, tag);
        return compoundTag;
    }

    public void put(Item item, OperateType type){
        if (!tag.contains("Items")) {
            tag.put("Items", new ListTag());
        }
        if (!tag.contains("Blocks")){
            tag.put("Blocks", new ListTag());
        }

        tag.getList("Items", Tag.TAG_STRING).add(StringTag.valueOf(item.getDescriptionId()));

        if (item instanceof BlockItem blockItem && type == OperateType.NORMAL){
            tag.getList("Blocks", Tag.TAG_STRING).add(StringTag.valueOf(blockItem.getBlock().getDescriptionId()));
        }
        setDirty();
    }

    public void put(Item item){
        this.put(item, OperateType.NORMAL);
    }


    public void remove(Item item){
        if (tag.contains("Items")){
            tag.getList("Items", Tag.TAG_STRING).remove(StringTag.valueOf(item.getDescriptionId()));
        }

        if (tag.contains("Blocks") && item instanceof BlockItem blockItem){
            tag.getList("Blocks", Tag.TAG_STRING).remove(StringTag.valueOf(blockItem.getBlock().getDescriptionId()));
        }

        setDirty();
    }

    public boolean contains(Item item){
        return tag.getList("Items", Tag.TAG_STRING).contains(StringTag.valueOf(item.getDescriptionId()));
    }

    public boolean contains(Block block){
        return tag.getList("Blocks", Tag.TAG_STRING).contains(StringTag.valueOf(block.getDescriptionId()));
    }

    public List<String> getBannedItems(){
        List<String> result = new ArrayList<>();
        ListTag listTag = tag.getList("Items", Tag.TAG_STRING);

        listTag.forEach(tag -> result.add(tag.getAsString()));

        return result;
    }

    public static BannedItemsData get(){
        return INSTANCE;
    }

    public static void register(MinecraftServer server){
        INSTANCE = server.overworld().getDataStorage().computeIfAbsent(BannedItemsData::new, BannedItemsData::new, NAME);
    }
}
