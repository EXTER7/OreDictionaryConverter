package exter.fodc.gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import exter.fodc.ModOreDicConvert;
import exter.fodc.container.ContainerAutomaticOreConverter;
import exter.fodc.network.ODCPacketHandler;
import exter.fodc.tileentity.TileEntityAutomaticOreConverter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonMerchant;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.oredict.OreDictionary;

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
      if(player_stack != null && ModOreDicConvert.instance.FindOreName(player_stack) != null)
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

        itemRenderer.zLevel = 200F;
        itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.renderEngine, item, window_x + x, window_y + y);
        itemRenderer.renderItemOverlayIntoGUI(fontRenderer, mc.renderEngine, item, window_x + x, window_y + y);
        itemRenderer.zLevel = 0.0F;
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

  public GuiAutomaticOreConverter(TileEntityAutomaticOreConverter aoc, IInventory player_inv)
  {
    super(new ContainerAutomaticOreConverter(aoc, player_inv));
    player_inventory = player_inv;
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

    int window_x = (this.width - this.xSize) / 2;
    int window_y = (this.height - this.ySize) / 2;

  }

  @Override
  protected void mouseClicked(int x, int y, int par3)
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
  protected void drawGuiContainerForegroundLayer(int par1, int par2)
  {
    int window_x = (this.width - this.xSize) / 2;
    int window_y = (this.height - this.ySize) / 2;
    fontRenderer.drawString(StatCollector.translateToLocal(te_autoconverter.getInvName()), 8, 6, 4210752);
    fontRenderer.drawString(StatCollector.translateToLocal("Targets"), 8, 65, 4210752);
    fontRenderer.drawString(StatCollector.translateToLocal(player_inventory.getInvName()), 8, this.ySize - 96 + 2, 4210752);
  }

  /**
   * Draw the background layer for the GuiContainer (everything behind the
   * items)
   */
  protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
  {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    
    //mc.renderEngine.bindTexture("aoc_gui");
    mc.func_110434_K().func_110577_a(GUI_TEXTURE);
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
