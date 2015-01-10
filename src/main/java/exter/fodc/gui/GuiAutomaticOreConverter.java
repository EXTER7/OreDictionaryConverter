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
import net.minecraft.util.StatCollector;
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
    
    public void OnClick()
    {
      ItemStack player_stack = mc.thePlayer.inventory.getItemStack();

      ItemStack target_stack;
      if(player_stack != null && !OreNameRegistry.FindAllOreNames(player_stack).isEmpty())
      {
        target_stack = player_stack.copy();
        target_stack.stackSize = 1;
      } else
      {
        target_stack = null;
      }
      te_autoconverter.SetTarget(position, target_stack);
      ODCPacketHandler.SendAutoOreConverterTarget(te_autoconverter, position, target_stack);
      
    }

    public void DrawSlot()
    {

      ItemStack item = te_autoconverter.GetTarget(position);
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
        itemRender.func_180450_b(item, window_x + x, window_y + y);
        itemRender.func_180453_a(font, item, window_x + x, window_y + y, null);
        zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
      }
    }
  }

  public TargetSlot GetTargetSlotAt(int x, int y)
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


    TargetSlot slot = GetTargetSlotAt(x - window_x, y - window_y);

    if(slot != null)
    {
      slot.OnClick();
    }
  }

  /**
   * Draw the foreground layer for the GuiContainer (everything in front of the
   * items)
   */
  @Override
  protected void drawGuiContainerForegroundLayer(int par1, int par2)
  {
    fontRendererObj.drawString(StatCollector.translateToLocal(te_autoconverter.getName()), 8, 6, 4210752);
    fontRendererObj.drawString(StatCollector.translateToLocal("Targets"), 8, 65, 4210752);
    fontRendererObj.drawString(StatCollector.translateToLocal(player_inventory.getName()), 8, this.ySize - 96 + 2, 4210752);
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
      target_slots[i].DrawSlot();
    }
    GL11.glPopMatrix();
    GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glEnable(GL11.GL_DEPTH_TEST);
    RenderHelper.enableStandardItemLighting();
  }
}
