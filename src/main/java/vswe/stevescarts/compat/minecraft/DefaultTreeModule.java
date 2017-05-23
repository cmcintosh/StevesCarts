package vswe.stevescarts.compat.minecraft;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.oredict.OreDictionary;
import vswe.stevescarts.api.farms.EnumHarvestResult;
import vswe.stevescarts.api.farms.ITreeModule;
import vswe.stevescarts.entitys.EntityMinecartModular;

public class DefaultTreeModule implements ITreeModule {

	@Override
	public EnumHarvestResult isLeaves(IBlockState blockState, BlockPos pos, EntityMinecartModular cart) {
		return blockState.getBlock().isLeaves(blockState, cart.world, pos) ? EnumHarvestResult.ALLOW : EnumHarvestResult.SKIP;
	}

	@Override
	public EnumHarvestResult isWood(IBlockState blockState, BlockPos pos, EntityMinecartModular cart) {
		return blockState.getBlock().isWood(cart.world, pos) ? EnumHarvestResult.ALLOW : EnumHarvestResult.SKIP;
	}

	@Override
	public boolean isSapling(final ItemStack sapling) {
		if (sapling != null) {
			if (this.isStackSapling(sapling)) {
				return true;
			}
			if (sapling.getItem() instanceof ItemBlock) {
				final Block b = Block.getBlockFromItem(sapling.getItem());
				return b instanceof BlockSapling || (b != null && this.isStackSapling(new ItemStack(b, 1, 32767)));
			}
		}
		return false;
	}

	@Override
	public boolean plantSapling(World world, BlockPos pos, ItemStack stack, FakePlayer fakePlayer) {
		return stack.getItem().onItemUse(stack, fakePlayer, world, pos, EnumHand.MAIN_HAND, EnumFacing.UP, 0.0f, 0.0f, 0.0f) == EnumActionResult.SUCCESS;
	}

	private boolean isStackSapling(final ItemStack sapling) {
		final int[] ids = OreDictionary.getOreIDs(sapling);
		for (int id : ids) {
			final String name = OreDictionary.getOreName(id);
			if (name != null && name.startsWith("treeSapling")) {
				return true;
			}
		}
		return false;
	}
}
