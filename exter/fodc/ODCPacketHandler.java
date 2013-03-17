package exter.fodc;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ODCPacketHandler implements IPacketHandler
{

  static public void SendAutoOreConverterTarget(TileEntityAutomaticOreConverter sender, int slot, ItemStack target)
  {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream data = new DataOutputStream(bytes);
    try
    {
      data.writeInt(sender.xCoord);
      data.writeInt(sender.yCoord);
      data.writeInt(sender.zCoord);
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
