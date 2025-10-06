package com.buuz135.modderspiggybank;

import com.buuz135.modderspiggybank.client.PiggyBankWidget;
import com.buuz135.modderspiggybank.platform.Services;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommonClass {

    public static HashMap<String, AuthorPiggyBank> PIGGY_BANKS = new HashMap<>();
    public static HashMap<String, AuthorInformation> AUTHOR_INFORMATION = new HashMap<>();
    public static List<AuthorInformation> INFORMATION = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger("Modder's Piggy Bank");

    public static void init(HashMap<String, AuthorInformation> authors) {
        AUTHOR_INFORMATION = authors;
        INFORMATION = AUTHOR_INFORMATION.values().stream().toList();
        loadMinified();

        Constants.ALLOWED_SCREEN_CLASSES.add("TitleScreen");
        Constants.ALLOWED_SCREEN_CLASSES.add("OptionsScreen");
        Constants.ALLOWED_SCREEN_CLASSES.add("ExtendedMenuScreen");

        Constants.ALLOWED_LINKS.put("ko-fi", "modders_piggy_bank.link.ko-fi");
        Constants.ALLOWED_LINKS.put("github-sponsor", "modders_piggy_bank.link.github-sponsor");
        Constants.ALLOWED_LINKS.put("patreon", "modders_piggy_bank.link.patreon");
        Constants.ALLOWED_LINKS.put("buymeacoffee", "modders_piggy_bank.link.buymeacoffee");
        Constants.ALLOWED_LINKS.put("paypal", "modders_piggy_bank.link.paypal");
        Constants.ALLOWED_LINKS.put("custom", "modders_piggy_bank.link.custom");
    }

    public static AuthorPiggyBank getPiggyBankOrDefault(String authorName) {
        if (PIGGY_BANKS.containsKey(authorName)) {
            return PIGGY_BANKS.get(authorName);
        }
        return new AuthorPiggyBank(authorName, 0x55FFFF, 0x55FFFF, new ArrayList<>(), new ArrayList<>());
    }

    public static PiggyBankWidget getRandomWidget() {
        //SELECT A RANDOM
        var selected = CommonClass.INFORMATION.get(Constants.RANDOM.nextInt(CommonClass.INFORMATION.size()));
        //FIND THEIR PIGGY BANK
        var piggy = CommonClass.getPiggyBankOrDefault(selected.name());
        //COLLECT ALTERNATE ATTRIBUTIONS
        List<String> alternateMods = new ArrayList<>();
        if (!piggy.alternate().isEmpty()){
            for (String alternateMod : piggy.alternate()) {
                if (CommonClass.AUTHOR_INFORMATION.containsKey(alternateMod)) {
                    alternateMods.addAll(CommonClass.AUTHOR_INFORMATION.get(alternateMod).modNames());
                }
            }
        }
        return new PiggyBankWidget( 2,3, selected, piggy, alternateMods);
    }

    private static void loadMinified() {
        //noinspection resource
        var httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
            .executor(Util.nonCriticalIoPool()).build();
        httpClient.sendAsync(HttpRequest.newBuilder().GET()
                    .header("user-agent", "Modder's Piggy Bank")
                    .uri(URI.create(Constants.MINIFIED_URL)).build(),
                HttpResponse.BodyHandlers.ofInputStream())
            .thenApply(response -> {
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new RuntimeException("Erroneous response with code " + response.statusCode());
                }
                return response.body();
            })
            .thenAccept(body -> {
                try (var reader = new BufferedReader(new InputStreamReader(body))) {
                    AuthorPiggyBank.CODEC.codec().listOf()
                        .decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .result().ifPresent(pair -> {
                            var list = pair.getFirst();
                            PIGGY_BANKS.clear();
                            list.forEach(author -> {
                                if (AUTHOR_INFORMATION.containsKey(author.author())) {
                                    PIGGY_BANKS.put(author.author(), author);
                                }
                            });
                            LOGGER.info("Found piggy banks of {} author(s) with mods in this pack!", PIGGY_BANKS.size());
                        });
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }).exceptionally(t -> {
                LOGGER.error("Failed to load data for Modder's Piggy Bank!", t);
                return null;
            }).thenRun(httpClient::close);
    }
}
