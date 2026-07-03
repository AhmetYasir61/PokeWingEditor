package com.pokewing.aggregator.forge;

import com.pokewing.aggregator.AggregatorMod;
import net.minecraftforge.fml.common.Mod;

@Mod(AggregatorMod.MOD_ID)
public final class AggregatorModForge {

    public AggregatorModForge() {
        AggregatorMod.init();
    }
}
