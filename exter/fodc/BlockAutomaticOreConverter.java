package exter.fodc;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;


public class BlockAutomaticOreConverter extends BlockContainer
{
  private Random rand = new Random();

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

  @Override
  public void breakBlock(World world, int x, int y, int z, int par5, int par6)
  {
    TileEntityAutomaticOreConverter te_aoc = (TileEntityAutomaticOreConverter)world.getBlockTileEntity(x, y, z);

    if(te_aoc != null && !world.isRemote)
    {
      int i;
      for(i = 0; i < te_aoc.getSizeInventory(); ++i)
      {
        ItemStack is = te_aoc.getStackInSlot(i);

        if(is != null && is.stackSize > 0)
        {
          double drop_x = (rand.nextFloat() * 0.3) + 0.35;
          double drop_y = (rand.nextFloat() * 0.3) + 0.35;
          double drop_z = (rand.nextFloat() * 0.3) + 0.35;
          EntityItem entityitem = new EntityItem(world, x + drop_x, y + drop_y, z + drop_z, is);
          entityitem.delayBeforeCanPickup = 10;

          world.spawnEntityInWorld(entityitem);
        }
      }
    }
    super.breakBlock(world, x, y, z, par5, par6);
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
