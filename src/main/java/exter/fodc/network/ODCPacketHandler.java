package exter.fodc.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import exter.fodc.tileentity.TileEntityAutomaticOreConverter;

public class ODCPacketHandler implements IPacketHandler
{

  static public void SendAutoOreConverterTarget(TileEntityAutomaticOreConverter sender, int slot, ItemStack target)
  {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream data = new DataOutputStream(bytes);
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
        data.writeInt(-1);
        data.writeInt(-1);
      } else
      {
        data.writeInt(target.itemID);
        data.writeInt(target.getItemDamage());
      }
    } catch(IOException e)
    {
      e.printStackTrace();
    }

    Packet250CustomPayload packet = new Packet250CustomPayload();
    packet.channel = "FODC";
    packet.data = bytes.toByteArray();
    packet.length = packet.data.length;
    packet.isChunkDataPacket = true;
    PacketDispatcher.sendPacketToServer(packet);
  }

  public static void SendAllAutoOreConverterTargets(TileEntityAutomaticOreConverter sender)
  {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream data = new DataOutputStream(bytes);

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
      
      //Packet type
      data.writeByte(1);
      
      
      //Target slots
      data.writeByte(slots.size());
      for(Integer s:slots)
      {
        ItemStack target = send_targets.get(s);
        data.writeByte(s.intValue());
        data.writeInt(target.itemID);
        data.writeInt(target.getItemDamage());
      }
    } catch(IOException e)
    {
      e.printStackTrace();
    }

    Packet250CustomPayload packet = new Packet250CustomPayload();
    packet.channel = "FODC";
    packet.data = bytes.toByteArray();
    packet.length = packet.data.length;
    packet.isChunkDataPacket = true;
    FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayers(packet);
  }
  
  @Override
  public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
  {
    try
    {
      ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);
      int x = data.readInt();
      int y = data.readInt();
      int z = data.readInt();
      World world = ((EntityPlayer)player).worldObj;

      if(world != null)
      {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if(tileEntity != null)
        {
          if(tileEntity instanceof TileEntityAutomaticOreConverter)
          {
            ((TileEntityAutomaticOreConverter)tileEntity).ReceivePacketData(manager, packet, ((EntityPlayer)player), data);
          }
        }
      }

    } catch(Exception e)
    {
      e.printStackTrace();
    }

  }

}
