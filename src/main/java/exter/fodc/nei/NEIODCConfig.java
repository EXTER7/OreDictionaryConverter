package exter.fodc.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import exter.fodc.ModOreDicConvert;

public class NEIODCConfig implements IConfigureNEI
{

  @Override
  public void loadConfig()
  {
    ODCRecipeHandler handler = new ODCRecipeHandler();
    API.registerRecipeHandler(handler);
    API.registerUsageHandler(handler);
  }

  @Override
  public String getName()
  {
    return ModOreDicConvert.MODNAME;
  }

  @Override
  public String getVersion()
  {
    return ModOreDicConvert.MODVERSION;
  }
}