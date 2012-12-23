package exter.fodc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ICraftingHandler;

public class ODCCraftingHandler implements ICraftingHandler
{
  @Override
  public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix)
  {
    int i;
    for (i = 0; i < craftMatrix.getSizeInventory(); i++)
    {
      ItemStack is = craftMatrix.getStackInSlot(i);
      if (is != null)
      {
        if (is.getItem() != null && is.getItem() == ModOreDicConvert.item_oreconverter)
        {
          craftMatrix.setInventorySlotContents(i, new ItemStack(ModOreDicConvert.item_oreconverter,2));
        }
      }
    }    
  }

  @Override
  public void onSmelting(EntityPlayer player, ItemStack item)
  {

  }

}
