package com.pokewing.aggregator.fabric;

import com.pokewing.aggregator.AggregatorMod;
import net.fabricmc.api.ModInitializer;

public final class AggregatorModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        AggregatorMod.init();
    }
}
