package exter.fodc.item;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import exter.fodc.ModOreDicConvert;

public class ItemOreConverter extends Item
{
  public ItemOreConverter(int id)
  {
    super(id);
    maxStackSize = 1;
    setCreativeTab(CreativeTabs.tabMisc);
    setUnlocalizedName("oreConverter");
  }
  
  @Override
  @SideOnly(Side.CLIENT)
  public void registerIcons(IconRegister iconRegister)
  {
    itemIcon = iconRegister.registerIcon("fodc:ore_converter");
  }
  
  @Override
  public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
  {
    player.openGui(ModOreDicConvert.instance, 0, world, 0, 0, 0);
    return stack;
  }
}
