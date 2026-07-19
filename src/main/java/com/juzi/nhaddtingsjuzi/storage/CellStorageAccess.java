package com.juzi.nhaddtingsjuzi.storage;

import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.security.IActionHost;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.UUID;

/** Resolves cell UUIDs and keeps lightweight client-visible statistics on the item. */
public final class CellStorageAccess {
    public static final String UUID_TAG = "nh_cell_uuid";
    public static final String USED_COUNT_TAG = "nh_used_amount";
    public static final String USED_TYPES_TAG = "nh_used_types";
    public static final String USED_BYTES_TAG = "nh_used_bytes";
    private static final String LEGACY_CONTENTS = "NHContents";

    private CellStorageAccess() {}

    public static CellDataStorage get(ItemStack cell, ISaveProvider provider) {
        World world = resolveWorld(provider);
        if (world == null || world.isRemote || cell == null) return null;

        NBTTagCompound tag = cell.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            cell.setTagCompound(tag);
        }
        UUID uuid = readUuid(tag);
        if (uuid == null) {
            uuid = UUID.randomUUID();
            tag.setString(UUID_TAG, uuid.toString());
        }

        CellStorageManager manager = CellStorageManager.get(world);
        if (manager == null) return null;
        CellDataStorage storage = manager.getOrCreate(uuid);
        migrateLegacyContents(tag, storage, manager);
        updateStats(tag, storage);
        return storage;
    }

    public static void save(ItemStack cell, ISaveProvider provider, CellDataStorage storage) {
        save(cell, provider, storage, null);
    }

    public static void save(ItemStack cell, ISaveProvider provider, CellDataStorage storage, BaseActionSource source) {
        if (cell == null || storage == null) return;
        NBTTagCompound tag = cell.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            cell.setTagCompound(tag);
        }
        updateStats(tag, storage);
        World world = resolveWorld(provider, source);
        CellStorageManager manager = CellStorageManager.get(world);
        if (manager != null) manager.markDirty();
    }

    /**
     * IO ports pass a null ISaveProvider and identify themselves through the action source.
     * Mark that host dirty after changing the cell stack so its container can sync the new NBT.
     */
    public static void notifyMachineSave(BaseActionSource source) {
        if (!(source instanceof MachineSource)) return;
        IActionHost host = ((MachineSource) source).via;
        if (host instanceof TileEntity) ((TileEntity) host).markDirty();
    }

    public static long getStoredItemCount(ItemStack cell) {
        return getStat(cell, USED_COUNT_TAG);
    }

    /** Returns true only after the server-side storage has confirmed that no items are stored. */
    public static boolean isEmpty(ItemStack cell, ISaveProvider provider) {
        CellDataStorage storage = get(cell, provider);
        return storage != null && storage.getStoredItemCount() <= 0;
    }

    public static long getStoredItemTypes(ItemStack cell) {
        return getStat(cell, USED_TYPES_TAG);
    }

    public static long getUsedBytes(ItemStack cell) {
        return getStat(cell, USED_BYTES_TAG);
    }

    private static void migrateLegacyContents(NBTTagCompound tag, CellDataStorage storage, CellStorageManager manager) {
        if (!tag.hasKey(LEGACY_CONTENTS, 9)) return;
        if (storage.getStoredItemCount() <= 0) {
            NBTTagList legacy = tag.getTagList(LEGACY_CONTENTS, 10);
            for (int i = 0; i < legacy.tagCount(); i++) {
                IAEItemStack stack = AEItemStack.loadItemStackFromNBT(legacy.getCompoundTagAt(i));
                storage.addImported(stack);
            }
        }
        tag.removeTag(LEGACY_CONTENTS);
        manager.markDirty();
    }

    private static void updateStats(NBTTagCompound tag, CellDataStorage storage) {
        tag.setLong(USED_COUNT_TAG, storage.getStoredItemCount());
        tag.setLong(USED_TYPES_TAG, storage.getStoredItemTypes());
        tag.setLong(USED_BYTES_TAG, storage.getUsedBytes());
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
        if (source instanceof MachineSource && ((MachineSource) source).via instanceof TileEntity) {
            return ((TileEntity) ((MachineSource) source).via).getWorldObj();
        }
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return null;
        World world = DimensionManager.getWorld(0);
        if (world != null) return world;
        MinecraftServer server = MinecraftServer.getServer();
        return server == null || server.worldServers == null || server.worldServers.length == 0
            ? null : server.worldServers[0];
    }
}
