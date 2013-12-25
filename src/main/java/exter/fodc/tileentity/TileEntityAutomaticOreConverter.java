package exter.fodc.tileentity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLLog;
import exter.fodc.ModOreDicConvert;
import exter.fodc.network.ODCPacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityAutomaticOreConverter extends TileEntity implements ISidedInventory
{
  private ItemStack[] inventory;
  private ItemStack[] targets;

  private ItemStack last_input;
  private ItemStack last_target;
  
  static public int SIZE_INVENTORY = 14;
  static public int SIZE_TARGETS = 18;
  
  public TileEntityAutomaticOreConverter()
  {
    inventory = new ItemStack[SIZE_INVENTORY];
    targets = new ItemStack[SIZE_TARGETS];
  }

  @Override
  public void readFromNBT(NBTTagCompound par1NBTTagCompound)
  {
    super.readFromNBT(par1NBTTagCompound);
    NBTTagList inv_tag = par1NBTTagCompound.getTagList("Items");
    NBTTagList targets_tag = par1NBTTagCompound.getTagList("Targets");
    inventory = new ItemStack[SIZE_INVENTORY];
    targets = new ItemStack[SIZE_TARGETS];
    int i;
    for(i = 0; i < inv_tag.tagCount(); i++)
    {
      NBTTagCompound tag = (NBTTagCompound)inv_tag.tagAt(i);
      int slot = tag.getByte("Slot") & 255;

      if(slot >= 0 && slot < inventory.length)
      {
        inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
      }
    }
    for(i = 0; i < targets_tag.tagCount(); i++)
    {
      NBTTagCompound tag = (NBTTagCompound)targets_tag.tagAt(i);
      int slot = tag.getByte("Slot") & 255;

      ItemStack is = ItemStack.loadItemStackFromNBT(tag);
      is.stackSize = 1;
      SetTarget(slot,is);
    }
  }

  @Override
  public void writeToNBT(NBTTagCompound par1NBTTagCompound)
  {
    super.writeToNBT(par1NBTTagCompound);
    NBTTagList inv_tag = new NBTTagList();
    NBTTagList targets_tag = new NBTTagList();

    int i;
    for(i = 0; i < inventory.length; i++)
    {
      ItemStack is = inventory[i];
      if(is != null)
      {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Slot", (byte)i);
        is.writeToNBT(tag);
        inv_tag.appendTag(tag);
      }
    }

    for(i = 0; i < targets.length; i++)
    {
      ItemStack t = targets[i]; 
      if(t != null)
      {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Slot", (byte)i);
        t.writeToNBT(tag);
        targets_tag.appendTag(tag);
      }
    }

    par1NBTTagCompound.setTag("Items", inv_tag);
    par1NBTTagCompound.setTag("Targets", targets_tag);
  }

  public void ReceivePacketData(INetworkManager manager, Packet250CustomPayload packet, EntityPlayer entityPlayer, ByteArrayDataInput data)
  {
    int type = data.readByte() & 255;
    switch(type)
    {
      case 0:
      {
        int slot = data.readByte() & 255;
        int target_id = data.readInt();
        int target_dmg = data.readInt();

        ItemStack target = null;
        if(target_id >= 0)
        {
          target = new ItemStack(target_id, 1, target_dmg);
        }

        if(!worldObj.isRemote)
        {
          SetTarget(slot, target);
        }
        break;
      }
      case 1:
      {
        int size = data.readByte() & 255;
        int i;
        for(i = 0; i < size; i++)
        {
          int slot = data.readByte() & 255;
          int target_id = data.readInt();
          int target_dmg = data.readInt();

          ItemStack target = null;
          if(target_id >= 0)
          {
            target = new ItemStack(target_id, 1, target_dmg);
          }

          if(worldObj.isRemote)
          {
            SetTarget(slot, target);
          }
        }
        break;
      }
    }
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
        onInventoryChanged();
        return var3;
      }
      else
      {
        var3 = inventory[slot].splitStack(amount);

        if(inventory[slot].stackSize == 0)
        {
          inventory[slot] = null;
        }

        onInventoryChanged();
        return var3;
      }
    }
    else
    {
      return null;
    }
  }

  @Override
  public ItemStack getStackInSlotOnClosing(int par1)
  {
    if(inventory[par1] != null)
    {
      ItemStack var2 = inventory[par1];
      inventory[par1] = null;
      return var2;
    } else
    {
      return null;
    }
  }

  @Override
  public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
  {
    inventory[par1] = par2ItemStack;

    if(par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
    {
      par2ItemStack.stackSize = this.getInventoryStackLimit();
    }

    onInventoryChanged();
  }

  @Override
  public String getInvName()
  {
    return "Ore Autoconverter";
  }

  @Override
  public int getInventoryStackLimit()
  {
    return 64;
  }

  @Override
  public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
  {
    return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this ? false : par1EntityPlayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64.0D;
  }


  @Override
  public void openChest()
  {
    if(!worldObj.isRemote)
    {
      ODCPacketHandler.SendAllAutoOreConverterTargets(this);
    }
  }

  @Override
  public void closeChest()
  {
    if(!worldObj.isRemote)
    {
      ODCPacketHandler.SendAllAutoOreConverterTargets(this);
    }
  }


  
  // Returns the version of the item to be converted,
  // or the item itself if its not registered in the ore dictionary.
  private ItemStack FindConversionTarget(ItemStack item)
  {
    if(last_input != null && last_target != null && item.isItemEqual(last_input) && ItemStack.areItemStackTagsEqual(item, last_input))
    {
      return last_target;
    }
    Set<String> names = ModOreDicConvert.instance.FindAllOreNames(item);
    if(names.isEmpty())
    {
      last_input = item.copy();
      last_target = last_input;
      return item;
    }
    
    for(ItemStack t:targets)
    {
      if(t != null)
      {
        Set<String> target_names = ModOreDicConvert.instance.FindAllOreNames(t);
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
    for(String name: names)
    {
      for(ItemStack stack:OreDictionary.getOres(name))
      {
        Set<String> target_names = ModOreDicConvert.instance.FindAllOreNames(stack);
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
  
  
  
  @Override
  public void updateEntity()
  {
    int i;
    for(i = 0; i < 8; i++)
    {
      ItemStack input = inventory[i];
      if(input != null)
      {
        ItemStack target = FindConversionTarget(input);
        int j;
        ItemStack dest = null;
        for(j = 8; j < 14; j++)
        {
          ItemStack d = inventory[j];
          if(d != null && d.stackSize < d.getMaxStackSize() && d.isItemEqual(target) && ItemStack.areItemStackTagsEqual(target,d))
          {
            dest = d;
            break;
          }
        }

        if(dest == null)
        {
          for(j = 8; j < 14; j++)
          {
            if(inventory[j] == null)
            {
              dest = target.copy();
              dest.stackSize = 0;
              inventory[j] = dest;
              break;
            }
          }
        }
        if(dest != null)
        {
          decrStackSize(i, 1);
          dest.stackSize++;
          onInventoryChanged();
          return;
        }
      }
    }
  }

  public void SetTarget(int slot,ItemStack target)
  {
    if(slot >= 0 && slot < SIZE_TARGETS && (target == null || !ModOreDicConvert.instance.FindAllOreNames(target).isEmpty()))
    {
      targets[slot] = target;
      last_input = null;
      last_target = null;
    }
  }

  public ItemStack GetTarget(int slot)
  {
    if(slot < 0 || slot >= SIZE_TARGETS)
    {
      return null;
    }
    return targets[slot];
  }

  @Override
  public boolean isInvNameLocalized()
  {
    return false;
  }

  static private final int[] ALL_SLOTS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 };

  @Override
  public boolean isItemValidForSlot(int i, ItemStack itemstack)
  {
    return i >= 0 && i < 6;
  }

  @Override
  public int[] getAccessibleSlotsFromSide(int side)
  {
    return ALL_SLOTS;
  }

  @Override
  public boolean canInsertItem(int i, ItemStack itemstack, int j)
  {
    return isItemValidForSlot(i, itemstack);
  }

  @Override
  public boolean canExtractItem(int i, ItemStack itemstack, int j)
  {
    return i >= 6 && i < 14;
  }
}
