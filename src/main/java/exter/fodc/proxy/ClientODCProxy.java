package exter.fodc.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import exter.fodc.ModOreDicConvert;

public class ClientODCProxy extends CommonODCProxy
{

  @Override
  public void init()
  {
    RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();    
    renderItem.getItemModelMesher().register(ModOreDicConvert.item_oreconverter, 0, new ModelResourceLocation(ModOreDicConvert.MODID + ":" + "oreConverter", "inventory"));
    renderItem.getItemModelMesher().register(Item.getItemFromBlock(ModOreDicConvert.block_oreconvtable), 0, new ModelResourceLocation(ModOreDicConvert.MODID + ":" + "oreConvTable", "inventory"));
    renderItem.getItemModelMesher().register(Item.getItemFromBlock(ModOreDicConvert.block_oreautoconv), 0, new ModelResourceLocation(ModOreDicConvert.MODID + ":" + "oreAutoconverter", "inventory"));
  }
  
}
