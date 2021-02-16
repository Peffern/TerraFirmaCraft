package net.dries007.tfc.common.blocks.crop;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;

import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class DoubleDeadCropBlock extends DeadCropBlock
{
    public static final EnumProperty<DoubleCropBlock.Part> PART = TFCBlockStateProperties.DOUBLE_CROP_PART;

    public DoubleDeadCropBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder.add(PART));
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos)
    {
        if (state.getValue(PART) == DoubleCropBlock.Part.BOTTOM)
        {
            return super.canSurvive(state, worldIn, pos);
        }
        else
        {
            final BlockState belowState = worldIn.getBlockState(pos.below());
            return belowState.is(this) && belowState.getValue(PART) == DoubleCropBlock.Part.BOTTOM;
        }
    }
}
