package exter.fodc.container;

import exter.fodc.slot.SlotAutomaticOreConverter;
import exter.fodc.tileentity.TileEntityAutomaticOreConverter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAutomaticOreConverter extends Container
{
  private TileEntityAutomaticOreConverter te_aoc;

  // Slot numbers
  private static final int SLOTS_INPUT = 0;
  private static final int SLOTS_OUTPUT = 8;
  private static final int SLOTS_INVENTORY = 14;
  private static final int SLOTS_HOTBAR = 14 + 3 * 9;

  public ContainerAutomaticOreConverter(TileEntityAutomaticOreConverter aoc, EntityPlayer player)
  {
    te_aoc = aoc;
    aoc.openInventory(player);
    int i,j;

    //Input
    for(i = 0; i < 2; ++i)
    {
      for(j = 0; j < 4; ++j)
      {
        addSlotToContainer(new Slot(aoc, j + i * 4, 8 + j * 18, 25 + i * 18));
      }
    }

    //Output
    for(i = 0; i < 2; ++i)
    {
      for(j = 0; j < 3; ++j)
      {
        addSlotToContainer(new SlotAutomaticOreConverter(aoc,8 + j + i * 3, 116 + j * 18, 25 + i * 18));
      }
    }

    //Player Inventory
    for(i = 0; i < 3; ++i)
    {
      for(j = 0; j < 9; ++j)
      {
        addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 128 + i * 18));
      }
    }
    for(i = 0; i < 9; ++i)
    {
      addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 186));
    }
  }

  @Override
  public boolean canInteractWith(EntityPlayer par1EntityPlayer)
  {
    return te_aoc.isUseableByPlayer(par1EntityPlayer);
  }

  public ItemStack transferStackInSlot(EntityPlayer player, int slot_index)
  {
    ItemStack slot_stack = null;
    Slot slot = (Slot) inventorySlots.get(slot_index);

    if (slot != null && slot.getHasStack())
    {
      ItemStack stack = slot.getStack();
      slot_stack = stack.copy();

      if (slot_index >= SLOTS_OUTPUT && slot_index < SLOTS_OUTPUT + 6)
      {
        if (!mergeItemStack(stack, SLOTS_INVENTORY, SLOTS_HOTBAR + 9, true))
        {
          return null;
        }

        slot.onSlotChange(stack, slot_stack);
      } else if (slot_index >= SLOTS_INVENTORY && slot_index < SLOTS_HOTBAR)
      {
        if (!mergeItemStack(stack, SLOTS_INPUT, SLOTS_INPUT + 9, false))
        {
          return null;
        }
      } else if (slot_index >= SLOTS_HOTBAR && slot_index < SLOTS_HOTBAR + 9)
      {
        if (!mergeItemStack(stack, SLOTS_INVENTORY, SLOTS_INVENTORY + 3 * 9, false))
        {
          return null;
        }
      } else if (!mergeItemStack(stack, SLOTS_INVENTORY, SLOTS_HOTBAR + 9, false))
      {
        return null;
      }

      if (stack.stackSize == 0)
      {
        slot.putStack((ItemStack) null);
      } else
      {
        slot.onSlotChanged();
      }

      if (stack.stackSize == slot_stack.stackSize)
      {
        return null;
      }

      slot.onPickupFromSlot(player, stack);
    }

    return slot_stack;
  }

  @Override
  public void onContainerClosed(EntityPlayer par1EntityPlayer)
  {
    super.onContainerClosed(par1EntityPlayer);
    this.te_aoc.closeInventory(par1EntityPlayer);
  }
}
