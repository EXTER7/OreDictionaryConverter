package exter.fodc.gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import exter.fodc.container.ContainerAutomaticOreConverter;
import exter.fodc.network.ODCPacketHandler;
import exter.fodc.registry.OreNameRegistry;
import exter.fodc.tileentity.TileEntityAutomaticOreConverter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class GuiAutomaticOreConverter extends GuiContainer
{
  private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("fodc:textures/gui/aoc_gui.png");

  private TileEntityAutomaticOreConverter te_autoconverter;
  private IInventory player_inventory;
  private TargetSlot[] target_slots;

  public class TargetSlot
  {

    final public int x, y;
    final public int position;

    public TargetSlot(int xx, int yy, int pos)
    {
      x = xx;
      y = yy;
      position = pos;
    }
    
    public void onClick()
    {
      ItemStack player_stack = mc.thePlayer.inventory.getItemStack();

      ItemStack target_stack;
      if(player_stack != null && !OreNameRegistry.findAllOreNames(player_stack).isEmpty())
      {
        target_stack = player_stack.copy();
        target_stack.stackSize = 1;
      } else
      {
        target_stack = null;
      }
      te_autoconverter.setTarget(position, target_stack);
      ODCPacketHandler.sendAutoOreConverterTarget(te_autoconverter, position, target_stack);
      
    }

    public void drawSlot()
    {

      ItemStack item = te_autoconverter.getTarget(position);
      if(item != null)
      {
        int window_x = (width - xSize) / 2;
        int window_y = (height - ySize) / 2;

        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        zLevel = 200.0F;
        itemRender.zLevel = 200.0F;
        FontRenderer font = null;
        if (item != null) font = item.getItem().getFontRenderer(item);
        if (font == null) font = fontRendererObj;
        itemRender.renderItemAndEffectIntoGUI(item, window_x + x, window_y + y);
        itemRender.renderItemOverlayIntoGUI(font, item, window_x + x, window_y + y, null);
        zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
      }
    }
  }

  public TargetSlot getTargetSlotAt(int x, int y)
  {
    for(TargetSlot s:target_slots)
    {
      if(x >= s.x + 1 && x <= s.x + 17 && y >= s.y + 1 && y <= s.y + 17)
      {
        return s;
      }
    }
    return null;
  }

  public GuiAutomaticOreConverter(TileEntityAutomaticOreConverter aoc, EntityPlayer player)
  {
    super(new ContainerAutomaticOreConverter(aoc, player));
    player_inventory = player.inventory;
    allowUserInput = false;
    ySize = 210;
    te_autoconverter = aoc;
    target_slots = new TargetSlot[18];
    int i,j;
    for(i = 0; i < 2; i++)
    {
      for(j = 0; j < 9; j++)
      {
        int s = i * 9 + j;
        target_slots[s] = new TargetSlot(j * 18 + 8, i * 18 + 76, s);
      }
    }
  }

  @Override
  public void initGui()
  {
    super.initGui();
  }

  @Override
  protected void mouseClicked(int x, int y, int par3) throws IOException
  {
    super.mouseClicked(x, y, par3);
    int window_x = (width - xSize) / 2;
    int window_y = (height - ySize) / 2;


    TargetSlot slot = getTargetSlotAt(x - window_x, y - window_y);

    if(slot != null)
    {
      slot.onClick();
    }
  }

  /**
   * Draw the foreground layer for the GuiContainer (everything in front of the
   * items)
   */
  @Override
  protected void drawGuiContainerForegroundLayer(int par1, int par2)
  {
    fontRendererObj.drawString(I18n.translateToLocal("Ore Autoconverter"), 8, 6, 4210752);
    fontRendererObj.drawString(I18n.translateToLocal("Targets"), 8, 65, 4210752);
    fontRendererObj.drawString(player_inventory.getDisplayName().getFormattedText(), 8, this.ySize - 96 + 2, 4210752);
  }

  /**
   * Draw the background layer for the GuiContainer (everything behind the
   * items)
   */
  @Override
  protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
  {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    
    mc.getTextureManager().bindTexture(GUI_TEXTURE);
    int window_x = (this.width - this.xSize) / 2;
    int window_y = (this.height - this.ySize) / 2;
    
    drawTexturedModalRect(window_x, window_y, 0, 0, xSize, ySize);
    RenderHelper.enableGUIStandardItemLighting();
    

    GL11.glPushMatrix();
    RenderHelper.enableGUIStandardItemLighting();
    GL11.glDisable(GL11.GL_LIGHTING);
    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    GL11.glEnable(GL11.GL_COLOR_MATERIAL);
    GL11.glEnable(GL11.GL_LIGHTING);
    int i;
    for(i = 0; i < 18; i++)
    {
      target_slots[i].drawSlot();
    }
    GL11.glPopMatrix();
    GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glEnable(GL11.GL_DEPTH_TEST);
    RenderHelper.enableStandardItemLighting();
  }
}
