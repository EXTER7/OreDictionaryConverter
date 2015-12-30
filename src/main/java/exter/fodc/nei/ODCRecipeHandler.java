package exter.fodc.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

import com.google.common.collect.ImmutableList;

import exter.fodc.gui.GuiOreConverter;
import exter.fodc.registry.OreNameRegistry;

public class ODCRecipeHandler  extends TemplateRecipeHandler
{
  public class CachedODCRecipe extends TemplateRecipeHandler.CachedRecipe
  {
    PositionedStack input;
    PositionedStack output;
    
    public CachedODCRecipe(ItemStack out)
    {
      out = out.copy();
      out.stackSize = 1;
      output = new PositionedStack(out,94 - 5,16 - 11);
      Set<String> names = OreNameRegistry.findAllOreNames(out);
      if(names.isEmpty())
      {
        output = null;
        return;
      }
      ArrayList<ItemStack> results = new ArrayList<ItemStack>();
      for(String n:names)
      {
        for(ItemStack stack : OreDictionary.getOres(n))
        {
          if(names.containsAll(OreNameRegistry.findAllOreNames(stack)))
          {
             ItemStack res = stack.copy();
             res.stackSize = 1;
             results.add(res);
          }
        }
      }
      input = new PositionedStack(results,12 - 5,25 - 11);
    }

    @Override
    public PositionedStack getIngredient()
    {
      return input;
    }

    @Override
    public PositionedStack getResult()
    {
      return output;
    }
  }

  public class CachedODCUsageRecipe extends TemplateRecipeHandler.CachedRecipe
  {

    PositionedStack input;
    List<PositionedStack> output;

    public CachedODCUsageRecipe(ItemStack in)
    {
      in = in.copy();
      in.stackSize = 1;
      input = new PositionedStack(in,12 - 5,25 - 11);

      ArrayList<ItemStack> results = new ArrayList<ItemStack>();
      Set<String> names = OreNameRegistry.findAllOreNames(in);
      if(names.isEmpty())
      {
        input = null;
        return;
      }

      res:for(String n:names)
      {
        for(ItemStack stack : OreDictionary.getOres(n))
        {
          if(names.containsAll(OreNameRegistry.findAllOreNames(stack)))
          {
             int j = results.size();
             ItemStack res = stack.copy();
             res.stackSize = 1;
             results.add(res);
             if(j == 15)
             {
               break res;
             }
          }
        }
      }
      output = new ArrayList<PositionedStack>();
      int i;
      for(i = 0; i < results.size(); i++)
      {
        if(i == 16)
        {
          break;
        }
        ItemStack res = results.get(i);
        output.add(new PositionedStack(res,18 * (i % 4) + 94 - 5,18 * (i / 4) + 16 - 11));
      }
    }

    @Override
    public PositionedStack getIngredient()
    {
      return input;
    }

    @Override
    public PositionedStack getResult()
    {
      return null;
    }
    
    @Override
    public List<PositionedStack> getOtherStacks()
    {
      return output;
    }
    
  }

  @Override
  public String getRecipeName()
  {
    return "Ore Converter";
  }

  @Override
  public String getGuiTexture()
  {
    return "fodc:textures/gui/oc_gui.png";
  }

  public void loadAllRecipes()
  {
    List<ItemStack> items = new ArrayList<ItemStack>();
    for(String name:OreNameRegistry.getOreNames())
    {
      items.addAll(OreDictionary.getOres(name));
    }
    for(ItemStack stack:items)
    {
      CachedODCUsageRecipe recipe = new CachedODCUsageRecipe(stack);
      if(recipe.input != null)
      {
        arecipes.add(recipe);
      }
    }
  }

  @Override
  public void loadUsageRecipes(String outputId, Object... results)
  {
    if(outputId.equals("fodc.converter"))
    {
      loadAllRecipes();
    }
    if(outputId.equals("item"))
    {
      CachedODCUsageRecipe recipe = new CachedODCUsageRecipe((ItemStack) results[0]);
      if(recipe.input != null)
      {
        arecipes.add(recipe);
      }
    }
  }

  @Override
  public void loadCraftingRecipes(String outputId, Object... results)
  {
    if(outputId.equals("fodc.converter"))
    {
      loadAllRecipes();
    }
    if(outputId.equals("item"))
    {
      CachedODCRecipe recipe = new CachedODCRecipe((ItemStack) results[0]);
      if(recipe.output != null)
      {
        arecipes.add(recipe);
      }
    }
  }


  @Override
  public void loadTransferRects()
  {
    transferRects.add(new RecipeTransferRect(new Rectangle(67 - 5, 44 - 11, 25, 15), "fodc.converter", new Object[0]));
  }

  @Override
  public List<Class<? extends GuiContainer>> getRecipeTransferRectGuis()
  {
    return ImmutableList.<Class<? extends GuiContainer>> of(GuiOreConverter.class);
  }
  

  @Override
  public int recipiesPerPage()
  {
    return 1;
  }

  @Override
  public void drawBackground(int recipe)
  {
    GL11.glColor4f(1, 1, 1, 1);
    GuiDraw.changeTexture(getGuiTexture());
    GuiDraw.drawTexturedModalRect(0, 0, 5, 11, 166, 86);
  }
}
