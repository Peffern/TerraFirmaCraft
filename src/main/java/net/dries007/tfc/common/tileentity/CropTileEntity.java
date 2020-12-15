package net.dries007.tfc.common.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CropTileEntity extends TickCounterTileEntity
{
    private float growthVariation;
    private float growth;
    private float yield;

    public CropTileEntity()
    {
        super(TFCTileEntities.CROP.get());
    }

    protected CropTileEntity(TileEntityType<?> type)
    {
        super(type);

        growthVariation = 1;
        growth = 0;
        yield = 0;
    }

    public float getGrowth()
    {
        return growth;
    }

    public float getGrowthVariation()
    {
        return growthVariation;
    }

    public float getYield()
    {
        return yield;
    }

    public void setGrowth(float growth)
    {
        this.growth = growth;
    }

    public void setGrowthVariation(float growthVariation)
    {
        this.growthVariation = growthVariation;
    }

    public void setYield(float yield)
    {
        this.yield = yield;
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt)
    {
        nbt.putFloat("growthVariation", growthVariation);
        nbt.putFloat("growth", growth);
        nbt.putFloat("yield", yield);

        return super.save(nbt);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt)
    {
        growthVariation = nbt.getFloat("growthVariation");
        growth = nbt.getFloat("growth");
        yield = nbt.getFloat("yield");

        super.load(state, nbt);
    }

    @Override
    public void onLoad()
    {
        if (level != null && !level.isClientSide())
        {
            level.getBlockTicks().scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        }
    }
}
