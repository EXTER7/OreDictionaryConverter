package exter.fodc.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import exter.fodc.ModOreDicConvert;
import exter.fodc.proxy.CommonODCProxy;

public class ItemOreConverter extends Item
{
  public ItemOreConverter()
  {
    super();
    maxStackSize = 1;
    setCreativeTab(CreativeTabs.tabMisc);
    setUnlocalizedName("oreConverter");
  }
  
  @Override
  @SideOnly(Side.CLIENT)
  public void registerIcons(IIconRegister iconRegister)
  {
    itemIcon = iconRegister.registerIcon("fodc:ore_converter");
  }
  
  @Override
  public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
  {
    player.openGui(ModOreDicConvert.instance, CommonODCProxy.GUI_ORECONVERTER, world, 0, 0, 0);
    return stack;
  }
}
