package exter.fodc.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import exter.fodc.registry.OreNameRegistry;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public class OreConverterJEI
{

  static public class Wrapper implements IRecipeWrapper
  {
    @Nonnull
    private final List<ItemStack> output;
    @Nonnull
    private final List<List<ItemStack>> input;
    @Nonnull
    protected final ItemStack input_item;
    
    public Wrapper(ItemStack input)
    {
      this.input = Collections.singletonList(Collections.singletonList(input));
      this.input_item = input;
      this.output = new ArrayList<ItemStack>();
      
      Set<String> names = OreNameRegistry.findAllOreNames(input);
      if(names.isEmpty())
      {
        input = null;
        return;
      }

      res:for(String n:names)
      {
        for(ItemStack stack : OreDictionary.getOres(n))
        {
          if(!(ItemStack.areItemsEqual(stack, input) && ItemStack.areItemStackTagsEqual(stack, input))
              && names.containsAll(OreNameRegistry.findAllOreNames(stack)))
          {
             int j = output.size();
             output.add(stack);
             if(j == 15)
             {
               break res;
             }
          }
        }
      }
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY)
    {
      return null;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
    {
      
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton)
    {
      return false;
    }

    @Override
    public void getIngredients(IIngredients ingredients)
    {
      ingredients.setInput(ItemStack.class, input_item);
      ingredients.setOutputs(ItemStack.class, output);
    }
  }

  static public class Category implements IRecipeCategory<Wrapper>
  {

    @Nonnull
    private final IDrawable background;
    @Nonnull
    private final String localizedName;
    
    public Category(IJeiHelpers helpers)
    {
      IGuiHelper guiHelper = helpers.getGuiHelper();

      ResourceLocation location = new ResourceLocation("fodc", "textures/gui/oc_gui.png");
      background = guiHelper.createDrawable(location, 10, 14, 156, 74);
      localizedName = Translator.translateToLocal("gui.jei.fodc.oreconverter");
    }

    @Override
    @Nonnull
    public IDrawable getBackground()
    {
      return background;
    }

    @Override
    public void drawExtras(Minecraft minecraft)
    {

    }

    @Nonnull
    @Override
    public String getTitle()
    {
      return localizedName;
    }

    @Nonnull
    @Override
    public String getUid()
    {
      return "fodc.oreconverter";
    }


    @Override
    public IDrawable getIcon()
    {
      return null;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, Wrapper recipeWrapper, IIngredients ingredients)
    {
      IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

      guiItemStacks.init(0, true, 1, 10);
      guiItemStacks.set(0, ingredients.getInputs(ItemStack.class).get(0));
      int i = 0;
      for(List<ItemStack> output:ingredients.getOutputs(ItemStack.class))
      {
        guiItemStacks.init(i + 1, false, 83 + (i % 4) * 18, 1 + (i / 4) * 18);
        guiItemStacks.set(i + 1, output);
        i++;
        if(i == 16)
        {
          break;
        }
      }
    }
  }

  static public class Handler implements IRecipeHandler<Wrapper>
  {
    @Override
    @Nonnull
    public Class<Wrapper> getRecipeClass()
    {
      return Wrapper.class;
    }

    @Override
    @Nonnull
    public IRecipeWrapper getRecipeWrapper(@Nonnull Wrapper recipe)
    {
      return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull Wrapper recipe)
    {
      return recipe.output.size() > 0;
    }

    @Override
    public String getRecipeCategoryUid(Wrapper recipe)
    {
      return "fodc.oreconverter";
    }
  }

  static public List<Wrapper> getRecipes()
  {
    List<Wrapper> recipes = new ArrayList<Wrapper>();

    for(String ore:OreNameRegistry.getOreNames())
    {
      for(ItemStack ore_stack:OreDictionary.getOres(ore))
      {
        boolean add = true;
        for(Wrapper re:recipes)
        {
          if(ItemStack.areItemsEqual(ore_stack, re.input_item) && ItemStack.areItemStackTagsEqual(ore_stack, re.input_item))
          {
            add = false;
            break;
          }
        }
        if(add)
        {
          recipes.add(new Wrapper(ore_stack));
        }
      }
    }

    return recipes;
  }
}