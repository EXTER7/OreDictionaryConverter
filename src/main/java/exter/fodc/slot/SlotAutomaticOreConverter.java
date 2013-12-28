package exter.fodc.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotAutomaticOreConverter extends Slot
{
  public SlotAutomaticOreConverter(IInventory inventory, int par3, int par4, int par5)
  {
    super(inventory, par3, par4, par5);
  }

  public boolean isItemValid(ItemStack par1ItemStack)
  {
    return false;
  }
}
