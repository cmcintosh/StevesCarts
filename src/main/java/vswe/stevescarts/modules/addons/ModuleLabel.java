package vswe.stevescarts.modules.addons;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vswe.stevescarts.containers.slots.SlotBase;
import vswe.stevescarts.containers.slots.SlotChest;
import vswe.stevescarts.entitys.EntityMinecartModular;
import vswe.stevescarts.guis.GuiMinecart;
import vswe.stevescarts.helpers.LabelInformation;
import vswe.stevescarts.helpers.Localization;
import vswe.stevescarts.helpers.ResourceHelper;
import vswe.stevescarts.modules.ModuleBase;
import vswe.stevescarts.modules.engines.ModuleEngine;
import vswe.stevescarts.modules.workers.tools.ModuleTool;

import java.util.ArrayList;

public class ModuleLabel extends ModuleAddon {
	private ArrayList<LabelInformation> labels;
	private int delay;
	private ArrayList<SlotBase> storageSlots;
	private ModuleTool tool;
	private DataParameter<Integer> SECONDS;
	private DataParameter<Byte> USED;
	private DataParameter<Integer> DATA;
	private DataParameter<Byte> ACTIVE;

	public ModuleLabel(final EntityMinecartModular cart) {
		super(cart);
		delay = 0;
		(labels = new ArrayList<>()).add(new LabelInformation(Localization.MODULES.ADDONS.NAME) {
			@Override
			public String getLabel() {
				return getCart().getCartName();
			}
		});
		labels.add(new LabelInformation(Localization.MODULES.ADDONS.DISTANCE) {
			@Override
			public String getLabel() {
				return Localization.MODULES.ADDONS.DISTANCE_LONG.translate(String.valueOf((int) getCart().getDistance(getClientPlayer())));
			}
		});
		labels.add(new LabelInformation(Localization.MODULES.ADDONS.POSITION) {
			@Override
			public String getLabel() {
				return Localization.MODULES.ADDONS.POSITION_LONG.translate(String.valueOf(getCart().x()), String.valueOf(getCart().y()), String.valueOf(getCart().z()));
			}
		});
		labels.add(new LabelInformation(Localization.MODULES.ADDONS.FUEL) {
			@Override
			public String getLabel() {
				int seconds = getDw(SECONDS);
				if (seconds == -1) {
					return Localization.MODULES.ADDONS.FUEL_NO_CONSUMPTION.translate();
				}
				int minutes = seconds / 60;
				seconds -= minutes * 60;
				final int hours = minutes / 60;
				minutes -= hours * 60;
				return String.format(Localization.MODULES.ADDONS.FUEL_LONG.translate() + ": %02d:%02d:%02d", hours, minutes, seconds);
			}
		});
		labels.add(new LabelInformation(Localization.MODULES.ADDONS.STORAGE) {
			@Override
			public String getLabel() {
				int used = getDw(USED);
				if (used < 0) {
					used += 256;
				}
				return (storageSlots == null) ? "" : (Localization.MODULES.ADDONS.STORAGE.translate() + ": " + used + "/" + storageSlots.size() + (
					(storageSlots.size() == 0) ? "" : ("[" + (int) (100.0f * used / storageSlots.size()) + "%]")));
			}
		});
	}

	@Override
	public void preInit() {
		if (getCart().getModules() != null) {
			for (final ModuleBase moduleBase : getCart().getModules()) {
				if (moduleBase instanceof ModuleTool) {
					tool = (ModuleTool) moduleBase;
					labels.add(new LabelInformation(Localization.MODULES.ADDONS.DURABILITY) {
						@Override
						public String getLabel() {
							if (!tool.useDurability()) {
								return Localization.MODULES.ADDONS.UNBREAKABLE.translate();
							}
							final int data = getDw(DATA);
							if (data == 0) {
								return Localization.MODULES.ADDONS.BROKEN.translate();
							}
							if (data > 0) {
								return Localization.MODULES.ADDONS.DURABILITY.translate() + ": " + data + " / " + tool.getMaxDurability() + " [" + 100 * data / tool.getMaxDurability() + "%]";
							}
							if (data == -1) {
								return "";
							}
							if (data == -2) {
								return Localization.MODULES.ADDONS.NOT_BROKEN.translate();
							}
							return Localization.MODULES.ADDONS.REPAIR.translate() + " [" + -(data + 3) + "%]";
						}
					});
					break;
				}
			}
		}
	}

	@Override
	public void init() {
		storageSlots = new ArrayList<>();
		for (final ModuleBase module : getCart().getModules()) {
			if (module.getSlots() != null) {
				for (final SlotBase slot : module.getSlots()) {
					if (slot instanceof SlotChest) {
						storageSlots.add(slot);
					}
				}
			}
		}
	}

	private boolean hasTool() {
		return tool != null;
	}

	private boolean hasToolWithDurability() {
		return hasTool() && tool.useDurability();
	}

	@Override
	public void addToLabel(final ArrayList<String> label) {
		for (int i = 0; i < labels.size(); ++i) {
			if (isActive(i)) {
				label.add(labels.get(i).getLabel());
			}
		}
	}

	private int[] getBoxArea(final int i) {
		return new int[] { 10, 17 + i * 12, 8, 8 };
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void drawBackground(final GuiMinecart gui, final int x, final int y) {
		ResourceHelper.bindResource("/gui/label.png");
		for (int i = 0; i < labels.size(); ++i) {
			final int[] rect = getBoxArea(i);
			drawImage(gui, rect, isActive(i) ? 8 : 0, 0);
			drawImage(gui, rect, inRect(x, y, rect) ? 8 : 0, 8);
		}
	}

	private boolean isActive(final int i) {
		return !isPlaceholder() && (getDw(ACTIVE) & 1 << i) != 0x0;
	}

	private void toggleActive(final int i) {
		updateDw(ACTIVE, (byte) (getDw(ACTIVE) ^ 1 << i));
	}

	@Override
	public int numberOfDataWatchers() {
		int count = 3;
		if (hasToolWithDurability()) {
			++count;
		}
		return count;
	}

	@Override
	public void initDw() {
		SECONDS = createDw(DataSerializers.VARINT);
		USED = createDw(DataSerializers.BYTE);
		DATA = createDw(DataSerializers.VARINT);
		ACTIVE = createDw(DataSerializers.BYTE);
		registerDw(ACTIVE, (byte) 0);
		registerDw(SECONDS, 0);
		registerDw(USED, (byte) 0);
		if (hasToolWithDurability()) {
			registerDw(DATA, -1);
		}
	}

	@Override
	public void update() {
		if (!isPlaceholder() && !getCart().world.isRemote) {
			if (delay <= 0) {
				if (isActive(3)) {
					int data = 0;
					for (final ModuleEngine engine : getCart().getEngines()) {
						if (engine.getPriority() != 3) {
							data += engine.getTotalFuel();
						}
					}
					if (data != 0) {
						final int consumption = getCart().getConsumption();
						if (consumption == 0) {
							data = -1;
						} else {
							data /= consumption * 20;
						}
					}
					updateDw(SECONDS, data);
				}
				if (isActive(4)) {
					int data = 0;
					for (final SlotBase slot : storageSlots) {
						if (slot.getHasStack()) {
							++data;
						}
					}
					updateDw(USED, (byte) data);
				}
				if (hasToolWithDurability()) {
					if (isActive(5)) {
						if (tool.isRepairing()) {
							if (tool.isActuallyRepairing()) {
								updateDw(DATA, -3 - tool.getRepairPercentage());
							} else {
								updateDw(DATA, -2);
							}
						} else {
							updateDw(DATA, tool.getCurrentDurability());
						}
					} else if (getDw(DATA) != -1) {
						updateDw(DATA, -1);
					}
				}
				delay = 20;
			} else if (delay > 0) {
				--delay;
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void mouseClicked(final GuiMinecart gui, final int x, final int y, final int button) {
		for (int i = 0; i < labels.size(); ++i) {
			final int[] rect = getBoxArea(i);
			if (inRect(x, y, rect)) {
				sendPacket(0, (byte) i);
				break;
			}
		}
	}

	@Override
	protected int numberOfPackets() {
		return 1;
	}

	@Override
	protected void receivePacket(final int id, final byte[] data, final EntityPlayer player) {
		if (id == 0) {
			toggleActive(data[0]);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void drawForeground(final GuiMinecart gui) {
		drawString(gui, Localization.MODULES.ADDONS.LABELS.translate(), 8, 6, 4210752);
		for (int i = 0; i < labels.size(); ++i) {
			final int[] rect = getBoxArea(i);
			drawString(gui, labels.get(i).getName(), rect[0] + 12, rect[1] + 1, 4210752);
		}
	}

	@Override
	public boolean hasGui() {
		return true;
	}

	@Override
	public boolean hasSlots() {
		return false;
	}

	@Override
	public int guiWidth() {
		return 92;
	}

	@Override
	public int guiHeight() {
		return 77;
	}

	@Override
	protected void Load(final NBTTagCompound tagCompound, final int id) {
		updateDw(ACTIVE, tagCompound.getByte(generateNBTName("Active", id)));
	}

	@Override
	protected void Save(final NBTTagCompound tagCompound, final int id) {
		tagCompound.setByte(generateNBTName("Active", id), getDw(ACTIVE));
	}
}
