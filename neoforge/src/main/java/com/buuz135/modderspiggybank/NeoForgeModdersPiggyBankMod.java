package com.buuz135.modderspiggybank;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Mod(Constants.MOD_ID)
public class NeoForgeModdersPiggyBankMod {

    public NeoForgeModdersPiggyBankMod(IEventBus eventBus) {
        var authorList = new HashMap<String, AuthorInformation>();
        for (IModInfo mod : ModList.get().getMods()) {
            mod.getConfig().getConfigElement("authors").ifPresent(obj -> {
                Consumer<String> adder = s -> authorList.computeIfAbsent(s.trim(),
                    name -> new AuthorInformation(name, new ArrayList<>())).modNames().add(mod.getDisplayName());
                if (obj instanceof List<?> list) {
                    list.forEach(author -> {
                        if (author instanceof String s) adder.accept(s);
                    });
                } else if (obj instanceof String names) {
                    Arrays.asList(names.split(",")).forEach(adder);
                }
            });
        }
        CommonClass.init(authorList);
    }
}
