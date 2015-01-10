package exter.fodc.tileentity;

import io.netty.buffer.ByteBufInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

import exter.fodc.registry.OreNameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityAutomaticOreConverter extends TileEntity implements IUpdatePlayerListBox,ISidedInventory
{
  private ItemStack[] inventory;
  private ItemStack[] targets;

  private ItemStack last_input;
  private ItemStack last_target;

  static public int SIZE_INVENTORY = 14;
  static public int SIZE_TARGETS = 18;

  private int process_tick;

  private boolean update_targets;

  public TileEntityAutomaticOreConverter()
  {
    inventory = new ItemStack[SIZE_INVENTORY];
    targets = new ItemStack[SIZE_TARGETS];
    process_tick = 0;
    update_targets = false;
  }

  private void WriteItemToNBT(NBTTagCompound compound, int slot)
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

  @Override
  public void readFromNBT(NBTTagCompound compound)
  {
    super.readFromNBT(compound);
    NBTTagList targets_tag = (NBTTagList) compound.getTag("Targets");
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

    if(targets_tag != null)
    {
      targets = new ItemStack[SIZE_TARGETS];
      for(i = 0; i < targets_tag.tagCount(); i++)
      {
        NBTTagCompound tag = targets_tag.getCompoundTagAt(i);
        int slot = tag.getByte("Slot") & 255;

        ItemStack is = ItemStack.loadItemStackFromNBT(tag);
        is.stackSize = 1;
        SetTarget(slot, is);
      }
    }
  }

  private void WriteTargetsToNBT(NBTTagCompound compound)
  {
    NBTTagList targets_tag = new NBTTagList();

    int i;
    for(i = 0; i < targets.length; i++)
    {
      ItemStack t = targets[i];
      if(t != null)
      {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Slot", (byte) i);
        t.writeToNBT(tag);
        targets_tag.appendTag(tag);
      }
    }
    compound.setTag("Targets", targets_tag);
  }

  @Override
  public void writeToNBT(NBTTagCompound compound)
  {
    super.writeToNBT(compound);

    int i;
    for(i = 0; i < inventory.length; i++)
    {
      WriteItemToNBT(compound, i);
    }

    WriteTargetsToNBT(compound);
  }

  private static ItemStack ReadItem(ByteBufInputStream data) throws IOException
  {
    int len = data.readInt();
    byte[] bytes = new byte[len];
    if(data.read(bytes) < len)
    {
      throw new IOException();
    }
    ByteArrayInputStream s = new ByteArrayInputStream(bytes);
    ItemStack item = ItemStack.loadItemStackFromNBT(CompressedStreamTools.readCompressed(s));
    s.close();
    return item;
  }

  private void SendPacketToPlayers(Packet packet)
  {
    final int MAX_DISTANCE = 192;
    if(!worldObj.isRemote && packet != null)
    {
      int xCoord = pos.getX();
      int yCoord = pos.getY();
      int zCoord = pos.getZ();
      for(int j = 0; j < worldObj.playerEntities.size(); j++)
      {
        EntityPlayerMP player = (EntityPlayerMP) worldObj.playerEntities.get(j);

        if(Math.abs(player.posX - xCoord) <= MAX_DISTANCE && Math.abs(player.posY - yCoord) <= MAX_DISTANCE && Math.abs(player.posZ - zCoord) <= MAX_DISTANCE && player.dimension == worldObj.provider.getDimensionId())
        {
          player.playerNetServerHandler.sendPacket(packet);
        }
      }
    }
  }

  public void ReceivePacketData(ByteBufInputStream data)
  {
    try
    {
      if(FMLCommonHandler.instance().getEffectiveSide().isServer())
      {
        int slot = data.readByte() & 255;
        boolean has_target = data.readBoolean();
        ItemStack target = null;
        if(has_target)
        {
          target = ReadItem(data);
        }

        update_targets = true;
        SetTarget(slot, target);
      }
    } catch(IOException e)
    {
      throw new RuntimeException(e);
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
  public void setInventorySlotContents(int slot, ItemStack item)
  {
    inventory[slot] = item;

    if(item != null && item.stackSize > this.getInventoryStackLimit())
    {
      item.stackSize = this.getInventoryStackLimit();
    }
  }

  @Override
  public String getName()
  {
    return "container.oreAutoconverter";
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

  }

  @Override
  public void closeInventory(EntityPlayer playerIn)
  {

  }

  // Returns the version of the item to be converted,
  // or the item itself if its not registered in the ore dictionary.
  private ItemStack FindConversionTarget(ItemStack item)
  {
    if(last_input != null && last_target != null && item.isItemEqual(last_input) && ItemStack.areItemStackTagsEqual(item, last_input))
    {
      return last_target;
    }
    Set<String> names = OreNameRegistry.FindAllOreNames(item);
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
        Set<String> target_names = OreNameRegistry.FindAllOreNames(t);
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
        Set<String> target_names = OreNameRegistry.FindAllOreNames(stack);
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

  private NBTTagCompound CreateUpdatePacket()
  {
    NBTTagCompound packet = new NBTTagCompound();
    super.writeToNBT(packet);
    return packet;
  }

  private NBTTagCompound ProcessItems()
  {
    int i;
    NBTTagCompound packet = null;
    for(i = 7; i >= 0; i--)
    {
      ItemStack input = inventory[i];
      if(input != null)
      {
        ItemStack target = FindConversionTarget(input);
        int j;
        for(j = 8; j < 14; j++)
        {
          ItemStack output = inventory[j];
          if(output == null)
          {
            inventory[j] = target.copy();
            inventory[j].stackSize = input.stackSize;
            inventory[i] = null;
            if(packet == null)
            {
              packet = CreateUpdatePacket();
            }
            WriteItemToNBT(packet, i);
            WriteItemToNBT(packet, j);
            return packet;
          } else if(output.stackSize < output.getMaxStackSize() && output.isItemEqual(target) && ItemStack.areItemStackTagsEqual(target, output))
          {
            int transfer = output.getMaxStackSize() - output.stackSize;
            if(transfer >= input.stackSize)
            {
              inventory[i] = null;
              output.stackSize += input.stackSize;
              if(packet == null)
              {
                packet = CreateUpdatePacket();
              }
              WriteItemToNBT(packet, i);
              WriteItemToNBT(packet, j);
              return packet;
            } else
            {
              input.stackSize -= transfer;
              output.stackSize += transfer;
              if(packet == null)
              {
                packet = CreateUpdatePacket();
              }
              WriteItemToNBT(packet, i);
              WriteItemToNBT(packet, j);
            }
          }
        }
        if(packet != null)
        {
          break;
        }
      }
    }
    return packet;
  }

  @Override
  public void update()
  {
    if(worldObj.isRemote)
    {
      return;
    }
    process_tick = (process_tick + 1) % 5;
    NBTTagCompound packet = null;
    if(process_tick == 0)
    {
      packet = ProcessItems();
    }
    if(update_targets)
    {
      update_targets = false;
      if(packet == null)
      {
        packet = CreateUpdatePacket();
      }
      WriteTargetsToNBT(packet);
    }
    if(packet != null)
    {
      markDirty();
      SendPacketToPlayers(new S35PacketUpdateTileEntity(pos, 0, packet));
    }
  }

  public void SetTarget(int slot, ItemStack target)
  {
    if(slot >= 0 && slot < SIZE_TARGETS && (target == null || !OreNameRegistry.FindAllOreNames(target).isEmpty()))
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
  public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
  {
    super.onDataPacket(net, pkt);
    if(FMLCommonHandler.instance().getEffectiveSide().isClient())
    {
      readFromNBT(pkt.getNbtCompound());
    }
    // worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
  }

  @Override
  public Packet getDescriptionPacket()
  {
    NBTTagCompound nbt = new NBTTagCompound();
    writeToNBT(nbt);
    return new S35PacketUpdateTileEntity(pos, 0, nbt);
  }

  @Override
  public int getField(int id)
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void setField(int id, int value)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public int getFieldCount()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void clear()
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean hasCustomName()
  {
    return true;
  }

  @Override
  public IChatComponent getDisplayName()
  {
    return new ChatComponentText("Ore Autoconverter");
  }

}
