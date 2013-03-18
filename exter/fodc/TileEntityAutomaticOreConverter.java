package exter.fodc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLLog;

import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraftforge.common.ISidedInventory;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityAutomaticOreConverter extends TileEntity implements ISidedInventory
{
  private ItemStack[] inventory;
  private ItemStack[] targets;

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
        this.onInventoryChanged();
        return var3;
      }
      else
      {
        var3 = inventory[slot].splitStack(amount);

        if(inventory[slot].stackSize == 0)
        {
          inventory[slot] = null;
        }

        this.onInventoryChanged();
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
  public int getStartInventorySide(ForgeDirection side)
  {
    if(side == ForgeDirection.UP || side == ForgeDirection.DOWN)
    {
      return 0;
    } else
    {
      return 8;
    }
  }

  @Override
  public int getSizeInventorySide(ForgeDirection side)
  {
    if(side == ForgeDirection.UP || side == ForgeDirection.DOWN)
    {
      return 8;
    } else
    {
      return 6;
    }
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
    String name = ModOreDicConvert.instance.FindOreName(item);
    if(name == null)
    {
      return item;
    }
    for(ItemStack t:targets)
    {
      if(t != null)
      {
        String target_name = ModOreDicConvert.instance.FindOreName(t);
        if(target_name.equals(name))
        {
          return t;
        }
      }
    }
    return OreDictionary.getOres(name).get(0);
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
    if(slot >= 0 && slot < SIZE_TARGETS && (target == null || ModOreDicConvert.instance.FindOreName(target) != null))
    {
      targets[slot] = target;
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
}
