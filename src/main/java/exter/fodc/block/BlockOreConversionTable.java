package exter.fodc.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import exter.fodc.ModOreDicConvert;
import exter.fodc.proxy.CommonODCProxy;

public class BlockOreConversionTable extends Block
{
  public BlockOreConversionTable()
  {
    super(Material.wood);
    this.setUnlocalizedName("oreConvTable");
    setCreativeTab(CreativeTabs.tabDecorations);
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
      player.openGui(ModOreDicConvert.instance, CommonODCProxy.GUI_ORECONVERTIONTABLE, world, x, y, z);
      return true;
    }
  }
}