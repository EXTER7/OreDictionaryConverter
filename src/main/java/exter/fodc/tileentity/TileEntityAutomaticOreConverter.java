package exter.fodc.tileentity;

import java.util.Set;

import exter.fodc.ModOreDicConvert;
import exter.fodc.network.MessageODC;
import exter.fodc.registry.OreNameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityAutomaticOreConverter extends TileEntity implements ITickable,ISidedInventory
{
  private ItemStack[] inventory;
  private ItemStack[] targets;

  private ItemStack last_input;
  private ItemStack last_target;

  static public int SIZE_INVENTORY = 14;
  static public int SIZE_TARGETS = 18;

  private int process_tick;

  public TileEntityAutomaticOreConverter()
  {
    inventory = new ItemStack[SIZE_INVENTORY];
    targets = new ItemStack[SIZE_TARGETS];
    process_tick = 0;
  }

  private void writeItemToNBT(NBTTagCompound compound, int slot)
  {
    ItemStack is = getStackInSlot(slot);
    NBTTagCompound tag = new NBTTagCompound();
    if(is != null)
    {
      tag.setBoolean("empty", false);
      is.writeToNBT(tag);
    } else
    {
      tag.setBoolean("empty", true);
    }
    compound.setTag("Item_" + String.valueOf(slot), tag);
  }

  private void writeTargetToNBT(NBTTagCompound compound, int slot)
  {
    ItemStack is = targets[slot];
    NBTTagCompound tag = new NBTTagCompound();
    if(is != null)
    {
      tag.setBoolean("empty", false);
      is.writeToNBT(tag);
    } else
    {
      tag.setBoolean("empty", true);
    }
    compound.setTag("Target_" + String.valueOf(slot), tag);
  }

  @Override
  public void readFromNBT(NBTTagCompound compound)
  {
    super.readFromNBT(compound);
    int i;

    for(i = 0; i < inventory.length; i++)
    {
      NBTTagCompound tag = (NBTTagCompound) compound.getTag("Item_" + String.valueOf(i));
      if(tag != null)
      {
        ItemStack stack = null;
        if(!tag.getBoolean("empty"))
        {
          stack = ItemStack.loadItemStackFromNBT(tag);
        }
        setInventorySlotContents(i, stack);
      }
    }


    // Read the old Targets tag.
    // TODO: Remove in future versions.
    NBTTagList targets_tag = (NBTTagList) compound.getTag("Targets");
    if(targets_tag != null)
    {
      targets = new ItemStack[SIZE_TARGETS];
      for(i = 0; i < targets_tag.tagCount(); i++)
      {
        NBTTagCompound tag = targets_tag.getCompoundTagAt(i);
        int slot = tag.getByte("Slot") & 255;

        ItemStack is = ItemStack.loadItemStackFromNBT(tag);
        is.stackSize = 1;
        setTarget(slot, is);
      }
    }

    for(i = 0; i < targets.length; i++)
    {
      NBTTagCompound tag = (NBTTagCompound) compound.getTag("Target_" + String.valueOf(i));
      if(tag != null)
      {
        ItemStack stack = null;
        if(!tag.getBoolean("empty"))
        {
          stack = ItemStack.loadItemStackFromNBT(tag);
        }
        targets[i] = stack;
      }
    }

  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound)
  {
    if(compound == null)
    {
      compound = new NBTTagCompound();
    }
    super.writeToNBT(compound);

    int i;
    for(i = 0; i < inventory.length; i++)
    {
      writeItemToNBT(compound, i);
    }

    for(i = 0; i < targets.length; i++)
    {
      writeTargetToNBT(compound, i);
    }
    
    return compound;
  }

  @Override
  public int getSizeInventory()
  {
    return SIZE_INVENTORY;
  }

  @Override
  public ItemStack getStackInSlot(int par1)
  {
    return inventory[par1];
  }

  @Override
  public ItemStack decrStackSize(int slot, int amount)
  {
    if(inventory[slot] != null)
    {
      ItemStack var3;

      if(inventory[slot].stackSize <= amount)
      {
        var3 = inventory[slot];
        inventory[slot] = null;
        markDirty();
        return var3;
      } else
      {
        var3 = inventory[slot].splitStack(amount);

        if(inventory[slot].stackSize == 0)
        {
          inventory[slot] = null;
        }

        markDirty();
        return var3;
      }
    } else
    {
      return null;
    }
  }

  @Override
  public ItemStack removeStackFromSlot(int index)
  {
    if(inventory[index] != null)
    {
      ItemStack var2 = inventory[index];
      inventory[index] = null;
      return var2;
    } else
    {
      return null;
    }
  }

  @Override
  public void setInventorySlotContents(int slot, ItemStack item)
  {
    inventory[slot] = item;

    if(item != null && item.stackSize > this.getInventoryStackLimit())
    {
      item.stackSize = this.getInventoryStackLimit();
    }
  }

  @Override
  public int getInventoryStackLimit()
  {
    return 64;
  }

  @Override
  public boolean isUseableByPlayer(EntityPlayer player)
  {
    return worldObj.getTileEntity(pos) != this ? false : getDistanceSq(player.posX, player.posY, player.posZ) <= 64.0D;
  }

  @Override
  public void openInventory(EntityPlayer playerIn)
  {
    if(!worldObj.isRemote)
    {
      NBTTagCompound tag = new NBTTagCompound();
      super.writeToNBT(tag);
      for(int i = 0; i < targets.length; i++)
      {
        writeTargetToNBT(tag, i);
      }
      sendPacketToPlayers(tag);
    }
  }

  @Override
  public void closeInventory(EntityPlayer playerIn)
  {
    if(!worldObj.isRemote)
    {
      NBTTagCompound tag = new NBTTagCompound();
      super.writeToNBT(tag);
      for(int i = 0; i < targets.length; i++)
      {
        writeTargetToNBT(tag, i);
      }
      sendPacketToPlayers(tag);
    }
  }

  protected void sendPacketToPlayers(NBTTagCompound data)
  {
    data.setInteger("dim", worldObj.provider.getDimension());
    ModOreDicConvert.network_channel.sendToAllAround(new MessageODC(data),
        new TargetPoint(worldObj.provider.getDimension(),pos.getX(),pos.getY(),pos.getZ(),192));
  }
  
  protected void sendToServer(NBTTagCompound tag)
  {
    if(worldObj.isRemote)
    {
      super.writeToNBT(tag);
      tag.setInteger("dim", worldObj.provider.getDimension());
      ModOreDicConvert.network_channel.sendToServer(new MessageODC(tag));
    }
  }

  // Returns the version of the item to be converted,
  // or the item itself if its not registered in the ore dictionary.
  private ItemStack findConversionTarget(ItemStack item)
  {
    if(last_input != null && last_target != null && item.isItemEqual(last_input) && ItemStack.areItemStackTagsEqual(item, last_input))
    {
      return last_target;
    }
    Set<String> names = OreNameRegistry.findAllOreNames(item);
    if(names.isEmpty())
    {
      last_input = item.copy();
      last_target = last_input;
      return item;
    }

    for(ItemStack t : targets)
    {
      if(t != null)
      {
        Set<String> target_names = OreNameRegistry.findAllOreNames(t);
        if(names.containsAll(target_names))
        {
          last_input = item.copy();
          last_target = t;
          return t;
        }
      }
    }
    ItemStack target = item;
    int target_diff = Integer.MAX_VALUE;
    for(String name : names)
    {
      for(ItemStack stack : OreDictionary.getOres(name))
      {
        Set<String> target_names = OreNameRegistry.findAllOreNames(stack);
        if(names.containsAll(target_names))
        {
          int diff = names.size() - target_names.size();
          if(diff < target_diff)
          {
            target_diff = diff;
            target = stack;
            if(diff == 0)
            {
              last_input = item.copy();
              last_target = target;
              return target;
            }
          }
        }
      }
    }
    last_input = item.copy();
    last_target = target;
    return target;
  }

  private void processItems()
  {
    int i;
    for(i = 7; i >= 0; i--)
    {
      ItemStack input = inventory[i];
      if(input != null)
      {
        ItemStack target = findConversionTarget(input);
        int j;
        for(j = 8; j < 14; j++)
        {
          ItemStack output = inventory[j];
          if(output == null)
          {
            inventory[j] = target.copy();
            inventory[j].stackSize = input.stackSize;
            inventory[i] = null;
            markDirty();
            return;
          } else if(output.stackSize < output.getMaxStackSize() && output.isItemEqual(target) && ItemStack.areItemStackTagsEqual(target, output))
          {
            int transfer = output.getMaxStackSize() - output.stackSize;
            if(transfer >= input.stackSize)
            {
              inventory[i] = null;
              output.stackSize += input.stackSize;
            } else
            {
              input.stackSize -= transfer;
              output.stackSize += transfer;
            }
            markDirty();
            return;
          }
        }
      }
    }
  }

  @Override
  public void update()
  {
    if(worldObj.isRemote)
    {
      return;
    }
    process_tick = (process_tick + 1) % 5;
    if(process_tick == 0)
    {
      processItems();
    }
  }

  public void setTarget(int slot, ItemStack target)
  {
    if(slot >= 0 && slot < SIZE_TARGETS && (target == null || !OreNameRegistry.findAllOreNames(target).isEmpty()))
    {
      targets[slot] = target;
      last_input = null;
      last_target = null;
      if(worldObj.isRemote)
      {
        NBTTagCompound tag = new NBTTagCompound();
        writeTargetToNBT(tag,slot);
        sendToServer(tag);
      }
    }
  }

  public ItemStack getTarget(int slot)
  {
    if(slot < 0 || slot >= SIZE_TARGETS)
    {
      return null;
    }
    return targets[slot];
  }

  static private final int[] ALL_SLOTS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 };

  @Override
  public boolean isItemValidForSlot(int i, ItemStack itemstack)
  {
    return i >= 0 && i < 8;
  }

  @Override
  public int[] getSlotsForFace(EnumFacing side)
  {
    return ALL_SLOTS;
  }

  @Override
  public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing direction)
  {
    return isItemValidForSlot(i, itemstack);
  }

  @Override
  public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing direction)
  {
    return i >= 8 && i < 14;
  }


  @Override
  public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
  {
    super.onDataPacket(net, pkt);
    if(FMLCommonHandler.instance().getEffectiveSide().isClient())
    {
      readFromNBT(pkt.getNbtCompound());
    }
  }

  @Override
  public SPacketUpdateTileEntity getUpdatePacket()
  {
    return new SPacketUpdateTileEntity(pos, 0, writeToNBT(null));
  }

  @Override
  public int getField(int id)
  {
    return 0;
  }

  @Override
  public void setField(int id, int value)
  {
   
  }

  @Override
  public int getFieldCount()
  {
    return 0;
  }

  @Override
  public void clear()
  {
    
  }

  @Override
  public boolean hasCustomName()
  {
    return true;
  }

  @Override
  public ITextComponent getDisplayName()
  {
    return new TextComponentString("Ore Autoconverter");
  }

  @Override
  public String getName()
  {
    return "Ore Autoconverter";
  }
}
