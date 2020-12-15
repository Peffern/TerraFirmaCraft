/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.common.types.managers;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

import net.dries007.tfc.common.types.Metal;
import net.dries007.tfc.util.data.DataManager;

public class MetalManager extends DataManager<Metal>
{
    public static final MetalManager INSTANCE = new MetalManager();

    private MetalManager()
    {
        super(new GsonBuilder().create(), "metals", "metal", false);
    }

    @Override
    protected Metal read(ResourceLocation id, JsonObject obj)
    {
        return new Metal(id, obj);
    }
}