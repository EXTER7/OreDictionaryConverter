package exter.fodc.jei;

import exter.fodc.container.ContainerOreConverter;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;

@JEIPlugin
public class ODCJEIPlugin implements IModPlugin
{
  @Override
  public void register(IModRegistry registry)
  {
    IJeiHelpers helpers = registry.getJeiHelpers();
    registry.addRecipeCategories(new OreConverterJEI.Category(helpers));
    registry.addRecipeHandlers(new OreConverterJEI.Handler());    
    registry.addRecipes(OreConverterJEI.getRecipes());    
    registry.getRecipeTransferRegistry().addRecipeTransferHandler(
        ContainerOreConverter.class, "fodc.oreconverter", 0, 25, 25, 36);
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime runtime)
  {

  }

  @Override
  public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry)
  {
    
  }

  @Override
  public void registerIngredients(IModIngredientRegistration registry)
  {
    
  }
}
