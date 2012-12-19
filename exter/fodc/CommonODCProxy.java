package exter.fodc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class CommonODCProxy implements IGuiHandler
{
  public static String ITEMS_PNG = "/exter/fodc/items.png";
  public static String BLOCKS_PNG = "/exter/fodc/blocks.png";

  public void registerRenderers()
  {
    // Nothing here as this is the server side proxy
  }
//returns an instance of the Container you made earlier

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
  {
    if(ID == 0)
    {
      return new ContainerOreConverter(player.inventory,world);
    } else if(ID == 1)
    {
      return new ContainerOreConverter(player.inventory,world,x,y,z);
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
    }
    return null;
  }

}
