package vswe.stevescarts.old.Modules.Engines;

import vswe.stevescarts.vehicles.entities.EntityModularCart;

public class ModuleCoalStandard extends ModuleCoalBase {
	public ModuleCoalStandard(EntityModularCart cart) {
		super(cart);
	}

	@Override
	public double getFuelMultiplier() {
		return 2.25;
	}
	
}