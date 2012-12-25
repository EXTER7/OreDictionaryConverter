package exter.fodc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;

public class SlotOreConverter extends Slot
{
  /** The ore matrix inventory linked to this result slot. */
  private final IInventory ore_matrix;

  /** The player that is using the GUI where this slot resides. */
  private EntityPlayer player_obj;

  /**
   * The number of items that have been crafted so far. Gets passed to
   * ItemStack.onCrafting before being reset.
   */
  private int amountCrafted;

  public SlotOreConverter(EntityPlayer player, IInventory inv_matrix, IInventory inv, int par4, int par5, int par6)
  {
    super(inv, par4, par5, par6);
    player_obj = player;
    ore_matrix = inv_matrix;
  }

  /**
   * Check if the stack is a valid item for this slot. Always true beside for
   * the armor slots.
   */
  public boolean isItemValid(ItemStack stack)
  {
    return false;
  }

  public boolean canTakeStack(EntityPlayer par1EntityPlayer)
  {
    return true;
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
    par1ItemStack.onCrafting(player_obj.worldObj, player_obj, amountCrafted);
    amountCrafted = 0;
  }

  public void onPickupFromSlot(EntityPlayer player, ItemStack stack)
  {
    GameRegistry.onItemCrafted(player, stack, ore_matrix);
    onCrafting(stack);

    String res_name = ModOreDicConvert.instance.FindOreName(stack);

    // Use the first matching ore in the ore matrix
    for (int i = 0; i < ore_matrix.getSizeInventory(); ++i)
    {
      ItemStack it = ore_matrix.getStackInSlot(i);

      if (it != null)
      {
        String name = ModOreDicConvert.instance.FindOreName(it);
        if (res_name.equals(name))
        {
          ore_matrix.decrStackSize(i, 1);
          break;
        }
      }
    }
  }
}
