package exter.fodc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;


public class BlockAutomaticOreConverter extends BlockContainer
{

  protected BlockAutomaticOreConverter(int id)
  {
    super(id, Material.rock);
    this.blockIndexInTexture = 0;
    setHardness(1.0F);
    setResistance(8.0F);
    setStepSound(Block.soundStoneFootstep);
    setBlockName("autoOreConverter");
    setCreativeTab(CreativeTabs.tabDecorations);
  }

  @Override
  public String getTextureFile()
  {
    return CommonODCProxy.BLOCKS_PNG;
  }


  /**
   * Checks to see if its valid to put this block at the specified coordinates.
   * Args: world, x, y, z
   */
  
  public int getBlockTextureFromSide(int side)
  {
    switch(side)
    {
      case 1:
        return 4;
      default:
        return 3;
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
      player.openGui(ModOreDicConvert.instance, 2, world, x, y, z);
      return true;
    }
  }

  /**
   * Returns a new instance of a block's tile entity class. Called on placing
   * the block.
   */
  public TileEntity createNewTileEntity(World par1World)
  {
    return new TileEntityAutomaticOreConverter();
  }
}
