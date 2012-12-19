package exter.fodc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class SlotOreConverter extends Slot
{
  /** The craft matrix inventory linked to this result slot. */
  private final IInventory craft_matrix;

  /** The player that is using the GUI where this slot resides. */
  private EntityPlayer thePlayer;

  /**
   * The number of items that have been crafted so far. Gets passed to
   * ItemStack.onCrafting before being reset.
   */
  private int amountCrafted;

  public SlotOreConverter(EntityPlayer par1EntityPlayer, IInventory par2IInventory, IInventory par3IInventory, int par4, int par5, int par6)
  {
    super(par3IInventory, par4, par5, par6);
    thePlayer = par1EntityPlayer;
    craft_matrix = par2IInventory;
  }

  /**
   * Check if the stack is a valid item for this slot. Always true beside for
   * the armor slots.
   */
  public boolean isItemValid(ItemStack stack)
  {
    return false;
  }

  /**
   * Decrease the size of the stack in slot (first int arg) by the amount of the
   * second int arg. Returns the new stack.
   */
  public ItemStack decrStackSize(int amount)
  {
    if (this.getHasStack())
    {
      amountCrafted += Math.min(amount, this.getStack().stackSize);
    }

    return super.decrStackSize(amount);
  }

  protected void onCrafting(ItemStack par1ItemStack, int par2)
  {
    amountCrafted += par2;
    onCrafting(par1ItemStack);
  }

  protected void onCrafting(ItemStack par1ItemStack)
  {
    par1ItemStack.onCrafting(thePlayer.worldObj, thePlayer, amountCrafted);
    amountCrafted = 0;
  }

  public void onPickupFromSlot(EntityPlayer player, ItemStack stack)
  {
    GameRegistry.onItemCrafted(player, stack, craft_matrix);
    onCrafting(stack);

    String res_name = ModOreDicConvert.instance.FindOreName(stack);

    //Use the first matching ore in the ore matrix
    for (int i = 0; i < craft_matrix.getSizeInventory(); ++i)
    {
      ItemStack it = craft_matrix.getStackInSlot(i);

      if (it != null)
      {
        String name = ModOreDicConvert.instance.FindOreName(it);
        if(res_name.equals(name))
        {
          craft_matrix.decrStackSize(i, 1);
          break;
        }

      }
    }
  }
}
