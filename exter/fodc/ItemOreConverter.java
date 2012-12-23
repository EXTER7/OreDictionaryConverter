package exter.fodc;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLLog;

public class ItemOreConverter extends Item
{

  public ItemOreConverter(int id)
  {
    super(id);
    maxStackSize = 1;
    setCreativeTab(CreativeTabs.tabMisc);
    setIconIndex(0);
    setItemName("oreConverter");
  }
  
  public String getTextureFile ()
  {
    return CommonODCProxy.ITEMS_PNG;
  }
  
  @Override
  public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
  {
    player.openGui(ModOreDicConvert.instance, 0, world, 0, 0, 0);
    return stack;
  }

}
