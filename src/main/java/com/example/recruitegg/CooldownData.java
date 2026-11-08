package com.example.recruitegg;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownData extends SavedData {
    private static final String NAME = "recruitegg_cooldowns";
    private final Map<UUID, Long> lastUse = new HashMap<>();

    public static CooldownData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(CooldownData::new, CooldownData::load, null), NAME);
    }

    public static CooldownData load(CompoundTag tag, HolderLookup.Provider provider) {
        CooldownData d = new CooldownData();
        CompoundTag t = tag.getCompound("times");
        for (String k : t.getAllKeys()) {
            try { d.lastUse.put(UUID.fromString(k), t.getLong(k)); } catch (IllegalArgumentException ignored) {}
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag t = new CompoundTag();
        lastUse.forEach((u, v) -> t.putLong(u.toString(), v));
        tag.put("times", t);
        return tag;
    }

    public long remaining(UUID id, long now) {
        Long last = lastUse.get(id);
        return last == null ? 0 : Math.max(0, last + RecruitEggMod.COOLDOWN_MS - now);
    }

    public void set(UUID id, long now) {
        lastUse.put(id, now);
        setDirty();
    }
}
