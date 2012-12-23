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

  public GuiOreConverter(InventoryPlayer player, World world)
  {
      super(new ContainerOreConverter(player, world));
      ySize = 180;
  }
  
  public GuiOreConverter(InventoryPlayer player, World world, int x, int y, int z)
  {
      super(new ContainerOreConverter(player, world, x, y, z));
      ySize = 180;
  }

  /**
   * Draw the foreground layer for the GuiContainer (everything in front of the items)
   */
  protected void drawGuiContainerForegroundLayer(int par1, int par2)
  {
      fontRenderer.drawString("Ore Converter", 23, 6, 4210752);
      fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 4210752);
  }

  /**
   * Draw the background layer for the GuiContainer (everything behind the items)
   */
  protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
  {
      int texture = mc.renderEngine.getTexture("/exter/fodc/guioreconverter.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      mc.renderEngine.bindTexture(texture);
      int center_x = (width - xSize) / 2;
      int center_y = (height - ySize) / 2;
      drawTexturedModalRect(center_x, center_y, 0, 0, xSize, ySize);
  }

}
