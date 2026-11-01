package com.mestisexo.economymod.economy;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyData extends SavedData {
    // El 3er parámetro (DataFixTypes) puede ser null en mods
    public static final SavedData.Factory<EconomyData> FACTORY =
            new SavedData.Factory<>(EconomyData::new, EconomyData::load, null);

    private final Map<UUID, Long> balances = new HashMap<>();

    public static EconomyData load(CompoundTag tag, HolderLookup.Provider registries) {
        EconomyData data = new EconomyData();
        CompoundTag saved = tag.getCompound("balances");
        for (String key : saved.getAllKeys()) {
            try {
                data.balances.put(UUID.fromString(key), saved.getLong(key));
            } catch (IllegalArgumentException ignored) {
                // clave corrupta: se omite
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag saved = new CompoundTag();
        balances.forEach((uuid, bal) -> saved.putLong(uuid.toString(), bal));
        tag.put("balances", saved);
        return tag;
    }

    Long get(UUID id) {
        return balances.get(id);
    }

    void set(UUID id, long amount) {
        balances.put(id, amount);
        setDirty();
    }
}
