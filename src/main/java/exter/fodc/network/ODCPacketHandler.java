package exter.fodc.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import exter.fodc.ModOreDicConvert;
import exter.fodc.tileentity.TileEntityAutomaticOreConverter;

public class ODCPacketHandler
{

  private static void WriteItem(ByteBufOutputStream data, ItemStack item) throws IOException
  {
    NBTTagCompound tag = new NBTTagCompound();
    item.writeToNBT(tag);
    byte[] bytes = CompressedStreamTools.compress(tag);
    data.writeInt(bytes.length);
    data.write(bytes);
  }

  static public void SendAutoOreConverterTarget(TileEntityAutomaticOreConverter sender, int slot, ItemStack target)
  {
    ByteBuf bytes = Unpooled.buffer();
    ByteBufOutputStream data = new ByteBufOutputStream(bytes);
    try
    {
      // Position
      data.writeInt(sender.xCoord);
      data.writeInt(sender.yCoord);
      data.writeInt(sender.zCoord);
      data.writeInt(sender.getWorldObj().provider.dimensionId);

      data.writeByte(slot);

      if(target == null)
      {
        data.writeBoolean(false);
      } else
      {
        data.writeBoolean(true);
        WriteItem(data, target);
      }

    } catch(IOException e)
    {
      throw new RuntimeException(e);
    }

    FMLProxyPacket packet = new FMLProxyPacket(bytes, "EXTER.FODC");
    ModOreDicConvert.network_channel.sendToServer(packet);
  }

  private void OnTEPacketData(ByteBufInputStream data, World world, int x, int y, int z)
  {
    if(world != null)
    {
      TileEntity tileEntity = world.getTileEntity(x, y, z);

      if(tileEntity != null)
      {
        if(tileEntity instanceof TileEntityAutomaticOreConverter)
        {
          ((TileEntityAutomaticOreConverter) tileEntity).ReceivePacketData(data);
        }
      }
    }
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  public void onClientPacketData(ClientCustomPacketEvent event)
  {
    try
    {
      ByteBufInputStream data = new ByteBufInputStream(event.packet.payload());
      int x = data.readInt();
      int y = data.readInt();
      int z = data.readInt();
      int d = data.readInt();
      World world = Minecraft.getMinecraft().theWorld;
      if(d == world.provider.dimensionId)
      {
        OnTEPacketData(data, world, x, y, z);
      }
    } catch(Exception e)
    {
      new RuntimeException(e);
    }
  }

  @SubscribeEvent
  public void onServerPacketData(ServerCustomPacketEvent event)
  {
    try
    {
      ByteBufInputStream data = new ByteBufInputStream(event.packet.payload());
      int x = data.readInt();
      int y = data.readInt();
      int z = data.readInt();
      int d = data.readInt();
      World world = DimensionManager.getWorld(d);
      OnTEPacketData(data, world, x, y, z);
    } catch(Exception e)
    {
      new RuntimeException(e);
    }
  }
}
