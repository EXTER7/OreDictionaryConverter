package exter.fodc.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import exter.fodc.ModOreDicConvert;
import exter.fodc.proxy.CommonODCProxy;

public class BlockOreConversionTable extends Block
{
  public BlockOreConversionTable()
  {
    super(Material.WOOD);
    setUnlocalizedName("oreConvTable");
    setHardness(2.5F);
    setSoundType(SoundType.WOOD);
    setCreativeTab(CreativeTabs.DECORATIONS);
    setRegistryName("oreConvTable");
  }
    
  /**
   * Called upon block activation (right click on the block.)
   */
  @Override
  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
  {
    if (world.isRemote)
    {
      return true;
    } else
    {
      player.openGui(ModOreDicConvert.instance, CommonODCProxy.GUI_ORECONVERTIONTABLE, world, pos.getX(),pos.getY(),pos.getZ());
      return true;
    }
  }
}