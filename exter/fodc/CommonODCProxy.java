package exter.fodc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonODCProxy implements IGuiHandler
{
  public static String ITEMS_PNG = "/exter/fodc/items.png";
  public static String BLOCKS_PNG = "/exter/fodc/blocks.png";
  public void Init()
  {
    // Nothing here as this is the server side proxy
  }

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
  {
    if(ID == 0)
    {
      return new ContainerOreConverter(player.inventory,world);
    } else if(ID == 1)
    {
      return new ContainerOreConverter(player.inventory,world,x,y,z);
    } else if(ID == 2)
    {
      return new ContainerAutomaticOreConverter((TileEntityAutomaticOreConverter)world.getBlockTileEntity(x, y, z),player.inventory);
    }
    return null;
  }

  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
  {
    if(ID == 0)
    {
      return new GuiOreConverter(player.inventory,world);
    } else if(ID == 1)
    {
      return new GuiOreConverter(player.inventory,world,x,y,z);
    } if(ID == 2)
    {
      TileEntityAutomaticOreConverter te = (TileEntityAutomaticOreConverter)world.getBlockTileEntity(x, y, z);
      return new GuiAutomaticOreConverter(te,player.inventory);
    }
    return null;
  }

}
