package stevesvehicles.common.core.tabs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stevesvehicles.client.localization.ILocalizedText;
import stevesvehicles.client.localization.LocalizedTextAdvanced;

public class CreativeTabCustom extends CreativeTabs {
	private static final ILocalizedText title = new LocalizedTextAdvanced("steves_vehicles:gui.tab:base");
	private ILocalizedText localizedText;
	private ItemStack item;

	public CreativeTabCustom(ILocalizedText localizedText) {
		super("steves_vehicles:gui.tab:ignored");
		this.localizedText = localizedText;
	}

	public void setIcon(ItemStack item) {
		this.item = item;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTranslatedTabLabel() {
		return title.translate(getTabLabel());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTabLabel() {
		return localizedText.translate();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ItemStack getTabIconItem() {
		return item;
	}
}
