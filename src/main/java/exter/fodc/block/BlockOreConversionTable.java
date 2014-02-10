package exter.fodc.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import exter.fodc.ModOreDicConvert;

public class BlockOreConversionTable extends Block
{
  public BlockOreConversionTable()
  {
    super(Material.wood);
    setBlockName("oreConvTable");
    setCreativeTab(CreativeTabs.tabDecorations);
  }
  
  private IIcon texture_top;
  private IIcon texture_bottom;
  private IIcon texture_sides;

  @Override
  @SideOnly(Side.CLIENT)
  public void registerBlockIcons(IIconRegister register)
  {
    texture_top = register.registerIcon("fodc:ore_conv_table_top");
    texture_bottom = register.registerIcon("fodc:ore_conv_table_bottom");
    texture_sides = register.registerIcon("fodc:ore_conv_table_sides");
  }
	
  @Override
  @SideOnly(Side.CLIENT)
  public IIcon getIcon(int side,int meta)
  {
    switch(side)
    {
      case 0:
        return texture_bottom;
      case 1:
        return texture_top;
      default:
        return texture_sides;
    }
  }
  
  /**
   * Called upon block activation (right click on the block.)
   */
  public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
  {
    if (world.isRemote)
    {
      return true;
    } else
    {
      player.openGui(ModOreDicConvert.instance, 1, world, x, y, z);
      return true;
    }
  }
}