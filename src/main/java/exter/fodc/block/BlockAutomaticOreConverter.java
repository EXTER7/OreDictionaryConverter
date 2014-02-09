package exter.fodc.block;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import exter.fodc.ModOreDicConvert;
import exter.fodc.tileentity.TileEntityAutomaticOreConverter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;


public class BlockAutomaticOreConverter extends BlockContainer
{
  private Random rand = new Random();
  
  private IIcon icon_top;

  public BlockAutomaticOreConverter()
  {
    super(Material.rock);
    setHardness(1.0F);
    setResistance(8.0F);
    setStepSound(Block.soundTypeStone);
    //setUnlocalizedName("autoOreConverter");
    setCreativeTab(CreativeTabs.tabDecorations);
  }

  @Override
  public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
  {
    TileEntityAutomaticOreConverter te_aoc = (TileEntityAutomaticOreConverter)world.getTileEntity(x, y, z);

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


  @Override
  @SideOnly(Side.CLIENT)
  public void registerBlockIcons(IIconRegister register)
  {
      blockIcon = register.registerIcon("fodc:auto_ore_converter_sides");
      icon_top = register.registerIcon("fodc:auto_ore_converter_top");
  }
  
  @Override 
  public IIcon getIcon(int side, int meta)
  {
     switch(side)
	 {
	   case 1:
       return icon_top;
	   default:
       return blockIcon;
	  }
  }

  /**
   * Called upon block activation (right click on the block.)
   */
  @Override
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

  @Override
  public TileEntity createNewTileEntity(World var1, int var2)
  {
    return new TileEntityAutomaticOreConverter();
  }
}
