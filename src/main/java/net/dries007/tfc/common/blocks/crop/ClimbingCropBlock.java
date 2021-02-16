package net.dries007.tfc.common.blocks.crop;

import java.util.function.Supplier;

import net.dries007.tfc.common.tileentity.CropTileEntity;
import net.dries007.tfc.util.Helpers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import net.minecraftforge.common.Tags;

import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.common.tileentity.FarmlandTileEntity;

public abstract class ClimbingCropBlock extends DoubleCropBlock
{
    public static final BooleanProperty STICK = TFCBlockStateProperties.STICK;

    public static ClimbingCropBlock create(Properties properties, int singleStages, int doubleStages, Crop crop)
    {
        IntegerProperty property = TFCBlockStateProperties.getAgeProperty(singleStages + doubleStages - 1);
        return new ClimbingCropBlock(properties, singleStages - 1, singleStages + doubleStages - 1, TFCBlocks.DEAD_CROPS.get(crop), TFCItems.CROP_SEEDS.get(crop), crop.getPrimaryNutrient()) {
            @Override
            public IntegerProperty getAgeProperty()
            {
                return property;
            }
        };
    }

    protected ClimbingCropBlock(Properties properties, int maxSingleAge, int maxAge, Supplier<? extends Block> dead, Supplier<? extends Item> seeds, FarmlandTileEntity.NutrientType primaryNutrient)
    {
        super(properties, maxSingleAge, maxAge, dead, seeds, primaryNutrient);
        registerDefaultState(defaultBlockState().setValue(STICK, false));
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        final ItemStack heldStack = player.getItemInHand(handIn);
        if (!worldIn.isClientSide() && heldStack.getItem().is(Tags.Items.RODS_WOODEN) && !state.getValue(STICK) && pos.getY() < 255)
        {
            // Add a stick
            worldIn.setBlock(pos, state.setValue(STICK, true), 3);
            worldIn.setBlock(pos.above(), state.setValue(STICK, true).setValue(PART, Part.TOP), 3);
            heldStack.shrink(1);

            // Since crops track growth after maxSingleAge (for death purposes), they need to reset to maxSingleAge
            // when the stick is added, so they don't jump ahead
            final CropTileEntity crop = Helpers.getTileEntityOrThrow(worldIn, pos, CropTileEntity.class);
            float targetAge = crop.getGrowth() * getMaxAge();
            if(targetAge > maxSingleAge)
                crop.setGrowth((float)maxSingleAge / (float)getMaxAge());
            return ActionResultType.CONSUME;
        }
        return ActionResultType.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder.add(STICK));
    }

    @Override
    protected void onGrow(World worldIn, BlockPos pos, BlockState state, int age)
    {
        if(age > maxSingleAge && !state.getValue(STICK))
        {
            // Necessary to recompute (unclamped) age since it's not given that age >= targetAge + 2
            // e.g. a crop that only needed a stick for its final stage still needs to die if it
            // the stick is missing, but age would only be 1 more than maxSingleAge
            final CropTileEntity crop = Helpers.getTileEntityOrThrow(worldIn, pos, CropTileEntity.class);
            float targetAge = crop.getGrowth() * getMaxAge();
            if(targetAge - maxSingleAge > 2)
            {
                // Death due to too long without a stick to grow on = not mature
                onDeath(worldIn, pos, state, false);
            }
            else if(state.getValue(getAgeProperty()) < maxSingleAge)
            {
                // If not there yet, grow to max age without a stick
                super.onGrow(worldIn, pos, state, maxSingleAge);
            }
        }
        else
        {
            super.onGrow(worldIn, pos, state, age);
        }
    }

    @Override
    protected void onDeath(World worldIn, BlockPos pos, BlockState state, boolean mature)
    {
        boolean stick = state.getValue(STICK);

        mature &= stick; // Failsafe in case it tries to mature-die with no stick

        if(stick)
        {
            worldIn.setBlockAndUpdate(pos.above(), dead.get().defaultBlockState().setValue(DeadCropBlock.MATURE, mature).setValue(PART, Part.TOP).setValue(STICK, true));
        }
        else
        {
            worldIn.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        }
        worldIn.setBlockAndUpdate(pos, dead.get().defaultBlockState().setValue(DeadCropBlock.MATURE, mature).setValue(PART, Part.BOTTOM).setValue(STICK,stick));
    }
}
