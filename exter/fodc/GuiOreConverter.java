package exter.fodc;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;


public class GuiOreConverter extends GuiContainer
{
  public GuiOreConverter(Container cont)
  {
    super(cont);
  }

  public GuiOreConverter(InventoryPlayer par1InventoryPlayer, World par2World)
  {
      super(new ContainerOreConverter(par1InventoryPlayer, par2World));
      ySize = 180;
  }
  
  public GuiOreConverter(InventoryPlayer par1InventoryPlayer, World par2World, int x, int y, int z)
  {
      super(new ContainerOreConverter(par1InventoryPlayer, par2World, x, y, z));
      ySize = 180;
  }

  /**
   * Draw the foreground layer for the GuiContainer (everything in front of the items)
   */
  protected void drawGuiContainerForegroundLayer(int par1, int par2)
  {
      this.fontRenderer.drawString("Ore Converter", 23, 6, 4210752);
      this.fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
  }

  /**
   * Draw the background layer for the GuiContainer (everything behind the items)
   */
  protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
  {
      int var4 = this.mc.renderEngine.getTexture("/exter/fodc/guioreconverter.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.renderEngine.bindTexture(var4);
      int var5 = (this.width - this.xSize) / 2;
      int var6 = (this.height - this.ySize) / 2;
      this.drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.ySize);
  }

}
