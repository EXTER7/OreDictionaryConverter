package exter.fodc;

import java.util.ArrayList;
import java.util.List;

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
  public IInventory[] results = new InventoryCraftResult[16];
  protected World worldObj;
  private int pos_x;
  private int pos_y;
  private int pos_z;

  //Slot numbers
  private static final int SLOTS_RESULT = 0;
  private static final int SLOTS_MATERIALS = 16;
  private static final int SLOTS_INVENTORY = SLOTS_MATERIALS + 9;
  private static final int SLOTS_HOTBAR = SLOTS_INVENTORY + 3*9;
  
  public ContainerOreConverter(InventoryPlayer par1InventoryPlayer, World par2World)
  {
    this(par1InventoryPlayer,par2World,0,9001,0);
  }
  
  public ContainerOreConverter(InventoryPlayer par1InventoryPlayer, World par2World, int x, int y, int z)
  {
    worldObj = par2World;
    pos_x = x;
    pos_y = y;
    pos_z = z;

    //Result slots
    int i;
    for (i = 0; i < 16; i++)
    {
      results[i] = new InventoryCraftResult();

      this.addSlotToContainer(new SlotOreConverter(par1InventoryPlayer.player, ore_matrix, results[i], i, 94 + (i % 4) * 18, 16 + (i / 4) * 18));
    }

    //Ore matrix slots
    int j;
    for (i = 0; i < 3; ++i)
    {
      for (j = 0; j < 3; ++j)
      {
        this.addSlotToContainer(new Slot(ore_matrix, j + i * 3, 12 + j * 18, 25 + i * 18));
      }
    }

    //Player inventory
    for (i = 0; i < 3; ++i)
    {
      for (j = 0; j < 9; ++j)
      {
        this.addSlotToContainer(new Slot(par1InventoryPlayer, j + i * 9 + 9, 8 + j * 18, 98 + i * 18));
      }
    }

    //Player hotbar
    for (i = 0; i < 9; ++i)
    {
      this.addSlotToContainer(new Slot(par1InventoryPlayer, i, 8 + i * 18, 156));
    }

    this.onCraftMatrixChanged(ore_matrix);
  }


  /**
   * Callback for when the crafting matrix is changed.
   */
  public void onCraftMatrixChanged(IInventory par1IInventory)
  {
    int i;
    
    //Create a unique list of names of items in the ore matrix slots
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

    //Create a list of place all possible results
    ArrayList<ItemStack> results = new ArrayList<ItemStack>();
    for(String n:ore_names)
    {
      results.addAll(OreDictionary.getOres(n));
    }
    
    //Place all possible resulting ores in the result slots
    for (i = 0; i < 16; i++)
    {
      ItemStack it = null;
      if (i < results.size())
      {
        ItemStack ore = results.get(i);
        it = new ItemStack(ore.itemID, 1, ore.getItemDamage());
      }
      this.results[i].setInventorySlotContents(i, it);
    }
    
  }

  /**
   * Callback for when the crafting gui is closed.
   */
  public void onCraftGuiClosed(EntityPlayer par1EntityPlayer)
  {
    super.onCraftGuiClosed(par1EntityPlayer);

    if (!worldObj.isRemote)
    {
      for(int i = 0; i < 9; ++i)
      {
        ItemStack stack = this.ore_matrix.getStackInSlotOnClosing(i);

        if (stack != null)
        {
          par1EntityPlayer.dropPlayerItem(stack);
        }
      }
    }
  }

  /**
   * Called when a player shift-clicks on a slot. You must override this or you
   * will crash when someone does that.
   */
  public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
  {
    ItemStack var3 = null;
    Slot var4 = (Slot) this.inventorySlots.get(par2);

    if (var4 != null && var4.getHasStack())
    {
      ItemStack var5 = var4.getStack();
      var3 = var5.copy();

      if (par2 < SLOTS_MATERIALS)
      {
        if (!this.mergeItemStack(var5, SLOTS_INVENTORY, SLOTS_HOTBAR + 9, true))
        {
          return null;
        }

        var4.onSlotChange(var5, var3);
      } else if (par2 >= SLOTS_INVENTORY && par2 < SLOTS_HOTBAR)
      {
        if (!mergeItemStack(var5, SLOTS_MATERIALS, SLOTS_MATERIALS + 9, false))
        {
          return null;
        }
      } else if (par2 >= SLOTS_HOTBAR && par2 < SLOTS_HOTBAR + 9)
      {
        if (!mergeItemStack(var5, SLOTS_INVENTORY, SLOTS_INVENTORY + 3*9, false))
        {
          return null;
        }
      } else if (!mergeItemStack(var5, SLOTS_INVENTORY, SLOTS_HOTBAR + 9, false))
      {
        return null;
      }

      if (var5.stackSize == 0)
      {
        var4.putStack((ItemStack) null);
      } else
      {
        var4.onSlotChanged();
      }

      if (var5.stackSize == var3.stackSize)
      {
        return null;
      }

      var4.onPickupFromSlot(par1EntityPlayer, var5);
    }

    return var3;
  }

  @Override
  public boolean canInteractWith(EntityPlayer var1)
  {
    if(pos_y <= 9000)
    {
      return this.worldObj.getBlockId(pos_x, pos_y, pos_z) != ModOreDicConvert.block_oreconvtable.blockID ? false : var1.getDistanceSq((double) pos_x + 0.5D, (double) pos_y + 0.5D, (double) pos_z + 0.5D) <= 64.0D;
    }
    return var1.inventory.hasItemStack(new ItemStack(ModOreDicConvert.item_oreconverter,1));// true;// this.worldObj.getBlockId(this.posX, this.posY, this.posZ) !=
  }

}
