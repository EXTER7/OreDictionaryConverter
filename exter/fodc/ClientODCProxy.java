package exter.fodc;

import net.minecraftforge.client.MinecraftForgeClient;

public class ClientODCProxy extends CommonODCProxy
{
  public void registerRenderers()
  {
    MinecraftForgeClient.preloadTexture(BLOCKS_PNG);
    MinecraftForgeClient.preloadTexture(ITEMS_PNG);
  }
  
  
}
