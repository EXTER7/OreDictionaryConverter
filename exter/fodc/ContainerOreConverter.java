package exter.fodc;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.FMLLog;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class ContainerOreConverter extends Container
{
  public InventoryCrafting ore_matrix = new InventoryCrafting(this, 3, 3);
  public IInventory[] result_slots = new InventoryCraftResult[16];
  protected World world_obj;
  private int pos_x;
  private int pos_y;
  private int pos_z;

  // Slot numbers
  private static final int SLOTS_RESULT = 0;
  private static final int SLOTS_MATERIALS = 16;
  private static final int SLOTS_INVENTORY = SLOTS_MATERIALS + 9;
  private static final int SLOTS_HOTBAR = SLOTS_INVENTORY + 3 * 9;

  public ContainerOreConverter(InventoryPlayer inventory_player, World world)
  {
    this(inventory_player, world, 0, 9001, 0);
  }

  public ContainerOreConverter(InventoryPlayer inventory_player, World world, int x, int y, int z)
  {
    world_obj = world;
    pos_x = x;
    pos_y = y;
    pos_z = z;

    // Result slots
    int i;
    for (i = 0; i < 16; i++)
    {
      result_slots[i] = new InventoryCraftResult();

      addSlotToContainer(new SlotOreConverter(inventory_player.player, ore_matrix, result_slots[i], i, 94 + (i % 4) * 18, 16 + (i / 4) * 18));
    }

    // Ore matrix slots
    int j;
    for (i = 0; i < 3; ++i)
    {
      for (j = 0; j < 3; ++j)
      {
        addSlotToContainer(new Slot(ore_matrix, j + i * 3, 12 + j * 18, 25 + i * 18));
      }
    }

    // Player inventory
    for (i = 0; i < 3; ++i)
    {
      for (j = 0; j < 9; ++j)
      {
        addSlotToContainer(new Slot(inventory_player, j + i * 9 + 9, 8 + j * 18, 98 + i * 18));
      }
    }

    // Player hotbar
    for (i = 0; i < 9; ++i)
    {
      addSlotToContainer(new Slot(inventory_player, i, 8 + i * 18, 156));
    }

    onCraftMatrixChanged(ore_matrix);
  }

  //Workaround for shift clicking converting more than one type of ore
  @Override
  public ItemStack slotClick(int par1, int par2, int par3, EntityPlayer player)
  {
    ItemStack var5 = null;
    InventoryPlayer inv_player = player.inventory;
    Slot slot;
    ItemStack var8;
    int var10;
    ItemStack var11;

    if (par3 == 1 && (par2 == 0 || par2 == 1) && par1 != -999)
    {
      slot = (Slot) inventorySlots.get(par1);
      if (slot != null && slot.canTakeStack(player))
      {
        var8 = transferStackInSlot(player, par1);
        if (var8 != null)
        {
          int id = var8.itemID;
          int dv = var8.getItemDamage();
          var5 = var8.copy();

          ItemStack is = slot.getStack();
          if (slot != null && is != null && is.itemID == id && (!is.getHasSubtypes() || is.getItemDamage() == dv))
          {
            retrySlotClick(par1, par2, true, player);
          }
        }
      }
    } else
    {
      var5 = super.slotClick(par1, par2, par3, player);
    }
    return var5;
  }

  /**
   * Callback for when the crafting matrix is changed.
   */
  public void onCraftMatrixChanged(IInventory par1IInventory)
  {
    int i;

    // Create a unique list of names of items in the ore matrix slots
    ArrayList<String> ore_names = new ArrayList<String>();
    for (i = 0; i < ore_matrix.getSizeInventory(); i++)
    {
      ItemStack in = ore_matrix.getStackInSlot(i);
      if (in != null)
      {
        String n = ModOreDicConvert.instance.FindOreName(in);
        if (n != null && !ore_names.contains(n))
        {
          ore_names.add(n);
        }
      }
    }

    // Create a list of place all possible results
    ArrayList<ItemStack> results = new ArrayList<ItemStack>();
    for (String n : ore_names)
    {
      results.addAll(OreDictionary.getOres(n));
    }

    // Place all possible resulting ores in the result slots
    for (i = 0; i < 16; i++)
    {
      ItemStack it = null;
      if (i < results.size())
      {
        ItemStack ore = results.get(i);
        it = new ItemStack(ore.itemID, 1, ore.getItemDamage());
      }
      result_slots[i].setInventorySlotContents(i, it);
    }

  }

  /**
   * Callback for when the crafting gui is closed.
   */
  public void onCraftGuiClosed(EntityPlayer player)
  {
    super.onCraftGuiClosed(player);

    if (!world_obj.isRemote)
    {
      for (int i = 0; i < 9; ++i)
      {
        ItemStack stack = ore_matrix.getStackInSlotOnClosing(i);

        if (stack != null)
        {
          player.dropPlayerItem(stack);
        }
      }
    }
  }

  /**
   * Called when a player shift-clicks on a slot. You must override this or you
   * will crash when someone does that.
   */
  public ItemStack transferStackInSlot(EntityPlayer player, int slot_index)
  {
    ItemStack slot_stack = null;
    Slot slot = (Slot) inventorySlots.get(slot_index);

    if (slot != null && slot.getHasStack())
    {
      ItemStack stack = slot.getStack();
      slot_stack = stack.copy();

      if (slot_index < SLOTS_MATERIALS)
      {
        FMLLog.info("[ODC] onPickupFromSlot " + slot_index + ", " + slot_stack.stackSize + ", " + stack.stackSize);

        if (!mergeItemStack(stack, SLOTS_INVENTORY, SLOTS_HOTBAR + 9, true))
        {
          return null;
        }

        slot.onSlotChange(stack, slot_stack);
      } else if (slot_index >= SLOTS_INVENTORY && slot_index < SLOTS_HOTBAR)
      {
        if (!mergeItemStack(stack, SLOTS_MATERIALS, SLOTS_MATERIALS + 9, false))
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
  public boolean canInteractWith(EntityPlayer playet)
  {
    if (pos_y <= 9000)
    {
      return this.world_obj.getBlockId(pos_x, pos_y, pos_z) != ModOreDicConvert.block_oreconvtable.blockID ? false : playet.getDistanceSq((double) pos_x + 0.5D, (double) pos_y + 0.5D, (double) pos_z + 0.5D) <= 64.0D;
    }
    return playet.inventory.hasItemStack(new ItemStack(ModOreDicConvert.item_oreconverter, 1));
  }

}
