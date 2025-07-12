/*
 * Copyright (c) Linstar 2025.
 */

package me.linstar.itembarriers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BannedEnchantmentsData extends SavedData {

    private static final String NAME = "BannedEnchantmentsData";
    private static final String KEY = "Enchantments";
    private static BannedEnchantmentsData INSTANCE = new BannedEnchantmentsData();
    private CompoundTag tag = new CompoundTag();

    public BannedEnchantmentsData(){}

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        tag.put(NAME, this.tag);
        return tag;
    }

    public BannedEnchantmentsData(CompoundTag tag){
        this.tag = tag.getCompound(NAME);
    }

    public void put(Enchantment enchantment){
        if (!tag.contains(KEY)){
            tag.put(KEY, new ListTag());
        }

        String id = enchantment.getDescriptionId();
        tag.getList(KEY, Tag.TAG_STRING).add(StringTag.valueOf(id));

        setDirty();
    }

    public void remove(Enchantment enchantment){
        if (!tag.contains(KEY)){
            tag.put(KEY, new ListTag());
            return;
        }

        String id = enchantment.getDescriptionId();
        tag.getList(KEY, Tag.TAG_STRING).remove(StringTag.valueOf(id));
        setDirty();
    }

    public boolean contains(Enchantment enchantment){
        String id = enchantment.getDescriptionId();
        ListTag listTag = tag.getList(KEY, Tag.TAG_STRING);
        return  listTag.contains(StringTag.valueOf(id));
    }

    public List<String> getBannedEnchants(){
        List<String> result = new ArrayList<>();
        ListTag listTag = tag.getList(KEY, Tag.TAG_STRING);

        listTag.forEach(tag -> result.add(tag.getAsString()));

        return result;
    }



    public static BannedEnchantmentsData get(){
        return INSTANCE;
    }

    public static void register(MinecraftServer server){
        INSTANCE = server.overworld().getDataStorage().computeIfAbsent(BannedEnchantmentsData::new, BannedEnchantmentsData::new, NAME);
    }
}
