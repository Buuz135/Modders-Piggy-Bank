package com.buuz135.modderspiggybank;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(Dist.CLIENT)
public class NeoforgeEvent {

    @SubscribeEvent
    public static void onGui(ScreenEvent.Init.Post event) {
        if (Constants.ALLOWED_SCREEN_CLASSES.contains(event.getScreen().getClass())) {
            event.addListener(CommonClass.getRandomWidget());
        }
    }
}
