package clashsoft.cslib.minecraft.client.gui;

import clashsoft.cslib.minecraft.lang.I18n;
import clashsoft.cslib.minecraft.update.Update;
import cpw.mods.fml.client.GuiScrollingList;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;

public class GuiModUpdatesSlot extends GuiScrollingList
{
	public GuiModUpdates	parent;
	
	protected int			selectedIndex	= 0;
	
	public GuiModUpdatesSlot(GuiModUpdates parent)
	{
		super(parent.mc, 140, parent.height, 32, parent.height - 62, 10, 35);
		this.parent = parent;
	}
	
	@Override
	protected int getSize()
	{
		return this.parent.updates.size();
	}
	
	@Override
	protected void elementClicked(int slotID, boolean doubleClick)
	{
		this.selectedIndex = slotID;
	}
	
	@Override
	protected boolean isSelected(int slotID)
	{
		return slotID == this.selectedIndex;
	}
	
	@Override
	protected void drawBackground()
	{
		this.parent.drawDefaultBackground();
	}
	
	@Override
	public void drawSlot(int slotID, int x, int y, int i, Tessellator tessellator)
	{
		x = this.left;
		
		FontRenderer font = this.parent.mc.fontRenderer;
		Update update = this.parent.updates.get(slotID);
		
		if (update != null)
		{
			int color = update.isValid() ? 0x00FF00 : update.isCurrent() ? 0xFFFFFF : 0xFF0000;
			font.drawStringWithShadow("\u00a7n" + update.getModName(), x + 2, y + 2, 0xFFFFFF);
			font.drawStringWithShadow(update.getVersionChanges(), x + 2, y + 12, color);
			font.drawStringWithShadow(I18n.getString(update.getStatus()), x + 2, y + 22, 0xFFFFFF);
		}
	}
}
