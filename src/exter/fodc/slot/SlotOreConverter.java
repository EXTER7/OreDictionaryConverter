package exter.fodc.slot;

import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import exter.fodc.ModOreDicConvert;

public class SlotOreConverter extends Slot
{
  /** The ore matrix inventory linked to this result slot. */
  private final IInventory ore_matrix;

  /** The player that is using the GUI where this slot resides. */
  private EntityPlayer player_obj;

  private int input_slot;
  
  /**
   * The number of items that have been crafted so far. Gets passed to
   * ItemStack.onCrafting before being reset.
   */
  private int amountCrafted;

  public SlotOreConverter(EntityPlayer player, IInventory inv_matrix, IInventory inv, int slot, int par5, int par6)
  {
    super(inv, slot, par5, par6);
    player_obj = player;
    ore_matrix = inv_matrix;
    input_slot = -1;
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
    return input_slot >= 0;
  }

  /**
   * Decrease the size of the stack in slot (first int arg) by the amount of the
   * second int arg. Returns the new stack.
   */
  public ItemStack decrStackSize(int amount)
  {
    if (getHasStack())
    {
      amountCrafted += Math.min(amount, getStack().stackSize);
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


    if(input_slot < 0)
    {
      //Something went wrong.
      Set<String> res_names = ModOreDicConvert.instance.FindAllOreNames(stack);
      String message = "Ore converter atempted to convert without consuming an input. Ore names:";
      for(String n:res_names)
      {
        message += " " + n;
      }
      throw new RuntimeException(message);
    }
    
    ore_matrix.decrStackSize(input_slot, 1);
  }
  
  public void SetInputSlot(int slot)
  {
    input_slot = slot;
  }
}
