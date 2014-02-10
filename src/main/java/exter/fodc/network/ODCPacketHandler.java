package exter.fodc.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import exter.fodc.ModOreDicConvert;
import exter.fodc.tileentity.TileEntityAutomaticOreConverter;

public class ODCPacketHandler
{

  static public void SendAutoOreConverterTarget(TileEntityAutomaticOreConverter sender, int slot, ItemStack target)
  {
    ByteBuf bytes = Unpooled.buffer();
    ByteBufOutputStream data = new ByteBufOutputStream(bytes);
    try
    {
      //Position
      data.writeInt(sender.xCoord);
      data.writeInt(sender.yCoord);
      data.writeInt(sender.zCoord);
      
      //Packet type
      data.writeByte(0);
      data.writeByte(slot);
      
      if(target == null)
      {
        data.writeBoolean(false);
      } else
      {
        data.writeBoolean(true);
        data.writeByte(slot);
        NBTTagCompound tag = new NBTTagCompound();
        target.writeToNBT(tag);
        CompressedStreamTools.writeCompressed(tag, data);
      }
      
    } catch(IOException e)
    {
      e.printStackTrace();
    }

    FMLProxyPacket packet = new FMLProxyPacket(bytes,"EXTER.FODC");
    ModOreDicConvert.network_channel.sendToServer(packet);
  }

  public static void SendAllAutoOreConverterTargets(TileEntityAutomaticOreConverter sender)
  {
    ByteBuf bytes = Unpooled.buffer();
    ByteBufOutputStream data = new ByteBufOutputStream(bytes);

    Map<Integer,ItemStack> send_targets = new HashMap<Integer,ItemStack>();
    int i;
    for(i = 0; i < TileEntityAutomaticOreConverter.SIZE_TARGETS; i++)
    {
      ItemStack t = sender.GetTarget(i);
      if(t != null)
      {
        send_targets.put(i, t);
      }
    }
    Set<Integer> slots = send_targets.keySet();
    try
    {
      
      //Position
      data.writeInt(sender.xCoord);
      data.writeInt(sender.yCoord);
      data.writeInt(sender.zCoord);
      data.writeInt(sender.getWorldObj().provider.dimensionId);
      
      //Packet type
      data.writeByte(1);
      
      
      //Target slots
      data.writeByte(slots.size());
      for(Integer s:slots)
      {
        ItemStack target = send_targets.get(s);
        data.writeByte(s.intValue());
        NBTTagCompound tag = new NBTTagCompound();
        target.writeToNBT(tag);
        CompressedStreamTools.writeCompressed(tag, data);
      }
    } catch(IOException e)
    {
      e.printStackTrace();
    }

    FMLProxyPacket packet = new FMLProxyPacket(bytes,"EXTER.FODC");
    ModOreDicConvert.network_channel.sendToAllAround(packet,
        new TargetPoint(
            sender.getWorldObj().provider.dimensionId,
            sender.xCoord,sender.yCoord,sender.zCoord,
            192));
  }
  
  @SubscribeEvent
  public void onPacketData(ClientCustomPacketEvent event)
  {
    try
    {
      ByteBufInputStream data = new ByteBufInputStream(event.packet.payload());
      int x = data.readInt();
      int y = data.readInt();
      int z = data.readInt();
      int d = data.readInt();
      World world = DimensionManager.getWorld(d);

      if(world != null)
      {
        TileEntity tileEntity = world.getTileEntity(x, y, z);

        if(tileEntity != null)
        {
          if(tileEntity instanceof TileEntityAutomaticOreConverter)
          {
            ((TileEntityAutomaticOreConverter)tileEntity).ReceivePacketData(data);
          }
        }
      }

    } catch(Exception e)
    {
      e.printStackTrace();
    }

  }

}
