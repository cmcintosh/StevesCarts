package vswe.stevescarts.models;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vswe.stevescarts.modules.ModuleBase;

@SideOnly(Side.CLIENT)
public class ModelLiquidDrainer extends ModelCleaner {
	public String modelTexture(final ModuleBase module) {
		return "/models/cleanerModelLiquid.png";
	}
}
