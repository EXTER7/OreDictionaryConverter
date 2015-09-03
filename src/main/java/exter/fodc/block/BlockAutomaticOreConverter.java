package exter.fodc.block;

import java.util.Random;

import exter.fodc.ModOreDicConvert;
import exter.fodc.proxy.CommonODCProxy;
import exter.fodc.tileentity.TileEntityAutomaticOreConverter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;


public class BlockAutomaticOreConverter extends BlockContainer
{
  private Random rand = new Random();
  
  public BlockAutomaticOreConverter()
  {
    super(Material.rock);
    setHardness(1.0F);
    setResistance(8.0F);
    setStepSound(Block.soundTypeStone);
    setUnlocalizedName("oreAutoconverter");
    setCreativeTab(CreativeTabs.tabDecorations);
  }

  @Override
  public void breakBlock(World world, BlockPos pos, IBlockState state)
  {
    TileEntityAutomaticOreConverter te_aoc = (TileEntityAutomaticOreConverter)world.getTileEntity(pos);

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
          EntityItem entityitem = new EntityItem(world, pos.getX() + drop_x, pos.getY() + drop_y, pos.getZ() + drop_z, is);
          entityitem.lifespan = 10;

          world.spawnEntityInWorld(entityitem);
        }
      }
    }
    super.breakBlock(world, pos, state);
  }

  @Override
  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
  {
    if (world.isRemote)
    {
      return true;
    } else
    {
      player.openGui(ModOreDicConvert.instance, CommonODCProxy.GUI_OREAUTOCONVERTER, world, pos.getX(),pos.getY(),pos.getZ());
      return true;
    }
  }

  @Override
  public TileEntity createNewTileEntity(World var1, int var2)
  {
    return new TileEntityAutomaticOreConverter();
  }
}
