package com.juzi.nhaddtingsjuzi.storage;

import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.networking.security.BaseActionSource;
import appeng.util.item.AEFluidStack;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.UUID;

/** Resolves fluid cell UUIDs and keeps lightweight client-visible statistics on the item. */
public final class CellFluidStorageAccess {
    public static final String UUID_TAG = CellStorageAccess.UUID_TAG;
    public static final String USED_COUNT_TAG = CellStorageAccess.USED_COUNT_TAG;
    public static final String USED_TYPES_TAG = CellStorageAccess.USED_TYPES_TAG;
    public static final String USED_BYTES_TAG = CellStorageAccess.USED_BYTES_TAG;
    private static final String LEGACY_CONTENTS = "NHContents";

    private CellFluidStorageAccess() {}

    public static CellFluidDataStorage get(ItemStack cell, ISaveProvider provider) {
        World world = resolveWorld(provider);
        if (world == null || world.isRemote || cell == null) return null;

        NBTTagCompound tag = getOrCreateTag(cell);
        UUID uuid = readUuid(tag);
        if (uuid == null) {
            uuid = UUID.randomUUID();
            tag.setString(UUID_TAG, uuid.toString());
        }

        CellStorageManager manager = CellStorageManager.get(world);
        if (manager == null) return null;
        CellFluidDataStorage storage = manager.getOrCreateFluid(uuid);
        migrateLegacyContents(tag, storage, manager);
        updateStats(tag, storage);
        return storage;
    }

    public static void save(ItemStack cell, ISaveProvider provider, CellFluidDataStorage storage) {
        save(cell, provider, storage, null);
    }

    public static void save(ItemStack cell, ISaveProvider provider, CellFluidDataStorage storage, BaseActionSource source) {
        if (cell == null || storage == null) return;
        updateStats(getOrCreateTag(cell), storage);
        CellStorageManager manager = CellStorageManager.get(resolveWorld(provider, source));
        if (manager != null) manager.markDirty();
    }

    public static void notifyMachineSave(BaseActionSource source) {
        CellStorageAccess.notifyMachineSave(source);
    }

    public static long getStoredFluidCount(ItemStack cell) {
        return getStat(cell, USED_COUNT_TAG);
    }

    public static long getStoredFluidTypes(ItemStack cell) {
        return getStat(cell, USED_TYPES_TAG);
    }

    public static long getUsedBytes(ItemStack cell) {
        return getStat(cell, USED_BYTES_TAG);
    }

    public static boolean isEmpty(ItemStack cell, ISaveProvider provider) {
        CellFluidDataStorage storage = get(cell, provider);
        return storage != null && storage.getStoredFluidCount() <= 0;
    }

    private static void migrateLegacyContents(NBTTagCompound tag, CellFluidDataStorage storage, CellStorageManager manager) {
        if (!tag.hasKey(LEGACY_CONTENTS, 9)) return;
        if (storage.getStoredFluidCount() <= 0) {
            NBTTagList legacy = tag.getTagList(LEGACY_CONTENTS, 10);
            for (int i = 0; i < legacy.tagCount(); i++) {
                IAEFluidStack stack = AEFluidStack.loadFluidStackFromNBT(legacy.getCompoundTagAt(i));
                storage.addImported(stack);
            }
        }
        tag.removeTag(LEGACY_CONTENTS);
        manager.markDirty();
    }

    private static void updateStats(NBTTagCompound tag, CellFluidDataStorage storage) {
        tag.setLong(USED_COUNT_TAG, storage.getStoredFluidCount());
        tag.setLong(USED_TYPES_TAG, storage.getStoredFluidTypes());
        tag.setLong(USED_BYTES_TAG, storage.getUsedBytes());
    }

    private static NBTTagCompound getOrCreateTag(ItemStack cell) {
        NBTTagCompound tag = cell.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            cell.setTagCompound(tag);
        }
        return tag;
    }

    private static long getStat(ItemStack cell, String key) {
        return cell != null && cell.hasTagCompound() ? cell.getTagCompound().getLong(key) : 0L;
    }

    private static UUID readUuid(NBTTagCompound tag) {
        if (!tag.hasKey(UUID_TAG, 8)) return null;
        try {
            return UUID.fromString(tag.getString(UUID_TAG));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static World resolveWorld(ISaveProvider provider) {
        return resolveWorld(provider, null);
    }

    private static World resolveWorld(ISaveProvider provider, BaseActionSource source) {
        if (provider instanceof TileEntity) return ((TileEntity) provider).getWorldObj();
        if (source instanceof appeng.api.networking.security.MachineSource
            && ((appeng.api.networking.security.MachineSource) source).via instanceof TileEntity) {
            return ((TileEntity) ((appeng.api.networking.security.MachineSource) source).via).getWorldObj();
        }
        if (provider == null && source == null) return null;
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return null;
        World world = DimensionManager.getWorld(0);
        if (world != null) return world;
        MinecraftServer server = MinecraftServer.getServer();
        return server == null || server.worldServers == null || server.worldServers.length == 0
            ? null : server.worldServers[0];
    }
}
