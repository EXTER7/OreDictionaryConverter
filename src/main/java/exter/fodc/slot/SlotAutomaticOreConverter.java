package exter.fodc.slot;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.MathHelper;
import cpw.mods.fml.common.registry.GameRegistry;

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
