package exter.fodc;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class BlockOreConversionTable extends Block
{
  protected BlockOreConversionTable(int par1)
  {
    super(par1, Material.wood);
    this.blockIndexInTexture = 0;
    this.setCreativeTab(CreativeTabs.tabDecorations);
  }

  /**
   * Returns the block texture based on the side being looked at. Args: side
   */
  public int getBlockTextureFromSide(int side)
  {
    switch(side)
    {
      case 0:
        return 2;
      case 1:
        return 0;
      default:
        return 1;
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

  @Override
  public String getTextureFile()
  {
    return CommonODCProxy.BLOCKS_PNG;
  }
}