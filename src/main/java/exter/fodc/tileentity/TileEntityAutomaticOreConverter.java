package exter.fodc.tileentity;

import java.util.Set;

import exter.fodc.ModOreDicConvert;
import exter.fodc.network.MessageODC;
import exter.fodc.registry.OreNameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityAutomaticOreConverter extends TileEntity implements ITickable,IInventory
{
  public class ItemHandler implements IItemHandler
  {
    protected boolean canInsert(int slot,ItemStack stack)
    {
      return isItemValidForSlot(slot, stack);
    }

    protected boolean canExtract(int slot)
    {
      return slot >= 8 && slot < 14;
    }
    
    @Override
    public int getSlots()
    {
      return 14;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
      return inventory[slot];
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
      if(!canInsert(slot,stack))
      {
        return stack;
      }
      ItemStack is = inventory[slot];
      if(is.isEmpty())
      {
        if(!simulate)
        {
          inventory[slot] = stack.copy();
          markDirty();
        }
        return ItemStack.EMPTY;
      } else if(is.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(is, stack))
      {
        if(stack.getCount() + is.getCount() > is.getMaxStackSize())
        {
          stack = stack.copy();
          stack.setCount(stack.getCount() - is.getMaxStackSize() + is.getCount());
          if(!simulate)
          {
            is.setCount(is.getMaxStackSize());
          }
        } else
        {
          if(!simulate)
          {
            is.grow(stack.getCount());
          }
          stack = ItemStack.EMPTY;
        }
        if(!simulate)
        {
          markDirty();
        }
        return stack;
      }
      return stack;
    }

    @Override
    public final ItemStack extractItem(int slot, int amount, boolean simulate)
    {
      if(!canExtract(slot))
      {
        return ItemStack.EMPTY;
      }
      ItemStack is = inventory[slot];
      if(is.isEmpty())
      {
        return ItemStack.EMPTY;
      }
      if(amount > is.getCount())
      {
        amount = is.getCount();
      }
      ItemStack result = is.copy();
      result.setCount(amount);
      if(!simulate)
      {
        is.shrink(amount);
        markDirty();
      }
      return result;
    }

    @Override
    public int getSlotLimit(int slot)
    {
      return 64;
    }
  }

  private ItemStack[] inventory;
  private ItemStack[] targets;

  private ItemStack last_input;
  private ItemStack last_target;

  static public int SIZE_INVENTORY = 14;
  static public int SIZE_TARGETS = 18;

  private int process_tick;
  
  IItemHandler item_handler;

  public TileEntityAutomaticOreConverter()
  {
    inventory = newItemStackArray(SIZE_INVENTORY);
    targets = newItemStackArray(SIZE_TARGETS);
    last_input = ItemStack.EMPTY;
    last_target = ItemStack.EMPTY;
    process_tick = 0;
    item_handler = new ItemHandler();
  }

  static private ItemStack[] newItemStackArray(int size)
  {
    ItemStack[] res = new ItemStack[size];
    for(int i = 0; i < size; i++)
    {
      res[i] = ItemStack.EMPTY;
    }
    return res;
  }
  
  private void writeItemToNBT(NBTTagCompound compound, int slot)
  {
    ItemStack is = getStackInSlot(slot);
    NBTTagCompound tag = new NBTTagCompound();
    is.writeToNBT(tag);
    compound.setTag("Item_" + String.valueOf(slot), tag);
  }

  private void writeTargetToNBT(NBTTagCompound compound, int slot)
  {
    ItemStack is = targets[slot];
    NBTTagCompound tag = new NBTTagCompound();
    is.writeToNBT(tag);
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
        ItemStack stack = ItemStack.EMPTY;
        if(!tag.getBoolean("empty"))
        {
          stack = new ItemStack(tag);
        }
        setInventorySlotContents(i, stack);
      }
    }

    for(i = 0; i < targets.length; i++)
    {
      NBTTagCompound tag = (NBTTagCompound) compound.getTag("Target_" + String.valueOf(i));
      if(tag != null)
      {
        ItemStack stack;
        if(tag.getBoolean("empty")) //Empty tag no longer used. Need this for backward compatibility for now.
        {
          stack = ItemStack.EMPTY;
        } else
        {
          stack = new ItemStack(tag);
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
    if(!inventory[slot].isEmpty())
    {
      ItemStack var3;

      if(inventory[slot].getCount() <= amount)
      {
        var3 = inventory[slot];
        inventory[slot] = ItemStack.EMPTY;
        markDirty();
        return var3;
      } else
      {
        var3 = inventory[slot].splitStack(amount);

        if(inventory[slot].getCount() == 0)
        {
          inventory[slot] = ItemStack.EMPTY;
        }

        markDirty();
        return var3;
      }
    } else
    {
      return ItemStack.EMPTY;
    }
  }

  @Override
  public ItemStack removeStackFromSlot(int index)
  {
    if(!inventory[index].isEmpty())
    {
      ItemStack var2 = inventory[index];
      inventory[index] = ItemStack.EMPTY;
      return var2;
    } else
    {
      return ItemStack.EMPTY;
    }
  }

  @Override
  public void setInventorySlotContents(int slot, ItemStack item)
  {
    inventory[slot] = item;

    if(!item.isEmpty() && item.getCount() > getInventoryStackLimit())
    {
      item.setCount(getInventoryStackLimit());
    }
  }

  @Override
  public int getInventoryStackLimit()
  {
    return 64;
  }

  @Override
  public boolean isUsableByPlayer(EntityPlayer player)
  {
    return world.getTileEntity(pos) != this ? false : getDistanceSq(player.posX, player.posY, player.posZ) <= 64.0D;
  }

  @Override
  public void openInventory(EntityPlayer playerIn)
  {
    if(!world.isRemote)
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
    if(!world.isRemote)
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
    data.setInteger("dim", world.provider.getDimension());
    ModOreDicConvert.network_channel.sendToAllAround(new MessageODC(data),
        new TargetPoint(world.provider.getDimension(),pos.getX(),pos.getY(),pos.getZ(),192));
  }
  
  protected void sendToServer(NBTTagCompound tag)
  {
    if(world.isRemote)
    {
      super.writeToNBT(tag);
      tag.setInteger("dim", world.provider.getDimension());
      ModOreDicConvert.network_channel.sendToServer(new MessageODC(tag));
    }
  }

  // Returns the version of the item to be converted,
  // or the item itself if its not registered in the ore dictionary.
  private ItemStack findConversionTarget(ItemStack item)
  {
    if(!last_input.isEmpty() && !last_target.isEmpty() && item.isItemEqual(last_input) && ItemStack.areItemStackTagsEqual(item, last_input))
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
      if(!t.isEmpty())
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
      if(!input.isEmpty())
      {
        ItemStack target = findConversionTarget(input);
        int j;
        for(j = 8; j < 14; j++)
        {
          ItemStack output = inventory[j];
          if(output.isEmpty())
          {
            inventory[j] = target.copy();
            inventory[j].setCount(input.getCount());
            inventory[i] = ItemStack.EMPTY;
            markDirty();
            return;
          } else if(output.getCount() < output.getMaxStackSize() && output.isItemEqual(target) && ItemStack.areItemStackTagsEqual(target, output))
          {
            int transfer = output.getMaxStackSize() - output.getCount();
            if(transfer >= input.getCount())
            {
              inventory[i] = ItemStack.EMPTY;
              output.grow(input.getCount());
            } else
            {
              input.shrink(transfer);
              output.grow(transfer);
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
    if(world.isRemote)
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
    if(slot >= 0 && slot < SIZE_TARGETS && (target.isEmpty() || !OreNameRegistry.findAllOreNames(target).isEmpty()))
    {
      targets[slot] = target;
      last_input = ItemStack.EMPTY;
      last_target = ItemStack.EMPTY;
      if(world.isRemote)
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
      return ItemStack.EMPTY;
    }
    return targets[slot];
  }

  @Override
  public boolean isItemValidForSlot(int i, ItemStack itemstack)
  {
    return i >= 0 && i < 8;
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
  public NBTTagCompound getUpdateTag()
  {
    return writeToNBT(null);
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

  @Override
  public boolean isEmpty()
  {
    return false;
  }
  

  @Override
  public boolean hasCapability(Capability<?> cap,EnumFacing facing)
  {
    if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
    {
      return true;
    } else
    {
      return super.hasCapability(cap, facing);
    }
  }
  
  @Override
  public <T> T getCapability(Capability<T> cap, EnumFacing facing)
  {
    if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
    {
      return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(item_handler);
    } else
    {
      return super.getCapability(cap, facing);
    }
  }
}
