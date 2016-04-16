package exter.fodc.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import exter.fodc.ModOreDicConvert;
import exter.fodc.tileentity.TileEntityAutomaticOreConverter;

public class ODCPacketHandler
{

  private static void writeItem(ByteBufOutputStream data, ItemStack item) throws IOException
  {
    NBTTagCompound tag = new NBTTagCompound();
    item.writeToNBT(tag);
    ByteArrayOutputStream s = new ByteArrayOutputStream();
    CompressedStreamTools.writeCompressed(tag,s);
    byte[] bytes = s.toByteArray();
    data.writeInt(bytes.length);
    data.write(bytes);
  }

  static public void sendAutoOreConverterTarget(TileEntityAutomaticOreConverter sender, int slot, ItemStack target)
  {
    ByteBuf bytes = Unpooled.buffer();
    ByteBufOutputStream data = new ByteBufOutputStream(bytes);
    try
    {
      // Position
      BlockPos p = sender.getPos();
      data.writeInt(p.getX());
      data.writeInt(p.getY());
      data.writeInt(p.getZ());
      data.writeInt(sender.getWorld().provider.getDimension());

      data.writeByte(slot);

      if(target == null)
      {
        data.writeBoolean(false);
      } else
      {
        data.writeBoolean(true);
        writeItem(data, target);
      }

    } catch(IOException e)
    {
      throw new RuntimeException(e);
    }

    FMLProxyPacket packet = new FMLProxyPacket(new PacketBuffer(bytes), "EXTER.FODC");
    ModOreDicConvert.network_channel.sendToServer(packet);
  }

  private void onTEPacketData(ByteBufInputStream data, World world, int x, int y, int z)
  {
    if(world != null)
    {
      TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

      if(tileEntity != null)
      {
        if(tileEntity instanceof TileEntityAutomaticOreConverter)
        {
          ((TileEntityAutomaticOreConverter) tileEntity).receivePacketData(data);
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
      ByteBufInputStream data = new ByteBufInputStream(event.getPacket().payload());
      int x = data.readInt();
      int y = data.readInt();
      int z = data.readInt();
      int d = data.readInt();
      World world = Minecraft.getMinecraft().theWorld;
      if(d == world.provider.getDimension())
      {
        onTEPacketData(data, world, x, y, z);
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
      ByteBufInputStream data = new ByteBufInputStream(event.getPacket().payload());
      int x = data.readInt();
      int y = data.readInt();
      int z = data.readInt();
      int d = data.readInt();
      World world = DimensionManager.getWorld(d);
      onTEPacketData(data, world, x, y, z);
    } catch(Exception e)
    {
      new RuntimeException(e);
    }
  }
}
