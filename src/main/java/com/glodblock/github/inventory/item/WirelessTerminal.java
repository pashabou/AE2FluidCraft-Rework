package com.glodblock.github.inventory.item;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.*;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGridNode;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.items.contents.WirelessTerminalViewCells;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.Platform;

import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;

public class WirelessTerminal extends MEMonitorHandler<IAEFluidStack> implements IWirelessTerminal {

    private final ItemStack target;
    private final IAEItemPowerStorage ips;
    private final EntityPlayer p;
    private final int inventorySlot;
    private final AppEngInternalInventory viewCell;
    private final BlockPos pos;
    private final StorageChannel channel;

    @SuppressWarnings("unchecked")
    public WirelessTerminal(final ItemStack is, BlockPos pos, EntityPlayer player) {
        super(
                (IMEInventoryHandler<IAEFluidStack>) Objects
                        .requireNonNull(Util.getWirelessInv(is, pos, StorageChannel.FLUIDS)));
        this.ips = (ToolWirelessTerminal) is.getItem();
        this.target = is;
        this.p = player;
        this.pos = pos;
        this.inventorySlot = p.inventory.currentItem;
        this.viewCell = new WirelessTerminalViewCells(is);
        this.channel = StorageChannel.FLUIDS;
    }

    @Override
    public ItemStack getItemStack() {
        return this.target;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        amt = usePowerMultiplier.multiply(amt);
        if (mode == Actionable.SIMULATE) {
            return usePowerMultiplier.divide(Math.min(amt, this.ips.getAECurrentPower(this.target)));
        }
        return usePowerMultiplier.divide(this.ips.extractAEPower(this.target, amt));
    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {
        return null;
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        return this;
    }

    @Override
    public IConfigManager getConfigManager() {
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
            final NBTTagCompound data = Platform.openNbtData(this.target);
            manager.writeToNBT(data);
        });
        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        out.readFromNBT((NBTTagCompound) Platform.openNbtData(this.target).copy());
        return out;
    }

    @Override
    public int getInventorySlot() {
        return this.inventorySlot;
    }

    @Override
    public IInventory getViewCellStorage() {
        return this.viewCell;
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return Util.getWirelessGrid(this.target, this.pos, this.channel);
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.NONE;
    }

    @Override
    public void securityBreak() {
        this.getGridNode(ForgeDirection.UNKNOWN).getMachine().securityBreak();
    }

    @Override
    public void saveChanges() {

    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack,
            ItemStack newStack) {}
}
