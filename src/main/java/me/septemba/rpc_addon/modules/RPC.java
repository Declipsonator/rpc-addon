/*
 * This file was part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 * ...until i pasted it
 */

package me.septemba.rpc_addon.modules;

//Pasted from squidoodly

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import me.septemba.rpc_addon.MainClass;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.utils.StarscriptError;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.*;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.util.Util;
import java.util.ArrayList;
import java.util.List;

public class RPC extends Module {
    public enum SelectMode {
        Random,
        Sequential
    }

    public enum ClientMode {
        BababaPlus, Ion, HivHack, Phosphoros, Future, ImpactPlus, PornHub
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgLine1 = settings.createGroup("Line 1");
    private final SettingGroup sgLine2 = settings.createGroup("Line 2");

    // Line 1

    private final Setting<List<String>> line1Strings = sgLine1.add(new StringListSetting.Builder()
        .name("line-1-messages")
        .description("Messages used for the first line.")
        .defaultValue("{player}", "{server}")
        .onChanged(strings -> recompileLine1())
        .build()
    );

    private final Setting<Integer> line1UpdateDelay = sgLine1.add(new IntSetting.Builder()
        .name("line-1-update-delay")
        .description("How fast to update the first line in ticks.")
        .defaultValue(200)
        .min(10)
        .sliderRange(10, 200)
        .build()
    );

    private final Setting<SelectMode> line1SelectMode = sgLine1.add(new EnumSetting.Builder<SelectMode>()
        .name("line-1-select-mode")
        .description("How to select messages for the first line.")
        .defaultValue(SelectMode.Sequential)
        .build()
    );

    // Line 2

    private final Setting<List<String>> line2Strings = sgLine2.add(new StringListSetting.Builder()
        .name("line-2-messages")
        .description("Messages used for the second line.")
        .defaultValue("Meteor on Crack!", "{round({server.tps}, 1)} TPS", "Playing on {server.difficulty} difficulty.", "{server.player_count} Players online")
        .onChanged(strings -> recompileLine2())
        .build()
    );

    private final Setting<Integer> line2UpdateDelay = sgLine2.add(new IntSetting.Builder()
        .name("line-2-update-delay")
        .description("How fast to update the second line in ticks.")
        .defaultValue(60)
        .min(10)
        .sliderRange(10, 200)
        .build()
    );

    private final Setting<SelectMode> line2SelectMode = sgLine2.add(new EnumSetting.Builder<SelectMode>()
        .name("line-2-select-mode")
        .description("How to select messages for the second line.")
        .defaultValue(SelectMode.Sequential)
        .build()
    );

    private final Setting<ClientMode> rpcMode = sgGeneral.add(new EnumSetting.Builder<ClientMode>()
        .name("rpc-mode")
        .description("What do display in RPC")
        .defaultValue(ClientMode.BababaPlus)
        .build()
    );

    private static final DiscordRichPresence rpc = new DiscordRichPresence();
    private static final DiscordRPC instance = DiscordRPC.INSTANCE;
    private SmallImage currentSmallImage;
    private int ticks;
    private boolean forceUpdate, lastWasInMainMenu;

    private final List<Script> line1Scripts = new ArrayList<>();
    private int line1Ticks, line1I;

    private final List<Script> line2Scripts = new ArrayList<>();
    private int line2Ticks, line2I;

    private String ID_That_We_Will_Use;
    private String Large_Image_Key_That_We_Will_Use;
    private String Large_Image_Text_That_We_Will_Use;

    private String Bababa_ID = "915375686288539670";
    private String Ion_ID = "915376082507685998";
    private String HivHack_ID = "906942096932499497";
    private String PhosPhoros_ID = "878489254194454540";
    private String Future_ID = "902354107996733520";
    private String ImpactPlus_ID = "901768051471560735";
    private String PornHub_ID = "899259417227304960";


    private String Bababa_LIK = "circle";
    private String Ion_LIK = "ion";
    private String HivHack_LIK = "hiv1";
    private String PhosPhoros_LIK = "phosphoros";
    private String Future_LIK = "future";
    private String ImpactPlus_LIK = "impactplus_2x";
    private String PornHub_LIK = "logooooooooo";

    private String Bababa_LIT = "Bababa+ v0.0.69";
    private String Ion_LIT = "Ion v1.69";
    private String HivHack_LIT = "HivHack v4.69.420";
    private String PhosPhoros_LIT = "Fosforus b0.69";
    private String Future_LIT = "Future Client b1.69.420";
    private String ImpactPlus_LIT = "Impact+ v1.2";
    private String PornHub_LIT = "PornHub v69.420";

    public RPC() {
        super(MainClass.CATEGORY, "RPC", "Le Funi");
        runInMainMenu = true;
    }

    @Override
    public void onActivate() {

        if (rpcMode.get() == ClientMode.BababaPlus){
            ID_That_We_Will_Use = Bababa_ID;
            Large_Image_Key_That_We_Will_Use = Bababa_LIK;
            Large_Image_Text_That_We_Will_Use = Bababa_LIT;
        }else if (rpcMode.get() == ClientMode.Ion){
            ID_That_We_Will_Use = Ion_ID;
            Large_Image_Key_That_We_Will_Use = Ion_LIK;
            Large_Image_Text_That_We_Will_Use = Ion_LIT;
        }else if (rpcMode.get() == ClientMode.HivHack){
            ID_That_We_Will_Use = HivHack_ID;
            Large_Image_Key_That_We_Will_Use = HivHack_LIK;
            Large_Image_Text_That_We_Will_Use = HivHack_LIT;
        } else if (rpcMode.get() == ClientMode.Phosphoros){
            ID_That_We_Will_Use = PhosPhoros_ID;
            Large_Image_Key_That_We_Will_Use = PhosPhoros_LIK;
            Large_Image_Text_That_We_Will_Use = PhosPhoros_LIT;
        } else if (rpcMode.get() == ClientMode.Future){
            ID_That_We_Will_Use = Future_ID;
            Large_Image_Key_That_We_Will_Use = Future_LIK;
            Large_Image_Text_That_We_Will_Use = Future_LIT;
        }else if (rpcMode.get() == ClientMode.ImpactPlus){
            ID_That_We_Will_Use = ImpactPlus_ID;
            Large_Image_Key_That_We_Will_Use = ImpactPlus_LIK;
            Large_Image_Text_That_We_Will_Use = ImpactPlus_LIT;
        }else if (rpcMode.get() == ClientMode.PornHub){
            ID_That_We_Will_Use = PornHub_ID;
            Large_Image_Key_That_We_Will_Use = PornHub_LIK;
            Large_Image_Text_That_We_Will_Use = PornHub_LIT;
        }

        DiscordEventHandlers handlers = new DiscordEventHandlers();
        instance.Discord_Initialize(ID_That_We_Will_Use, handlers, true, null);

        rpc.startTimestamp = System.currentTimeMillis() / 1000L;

        rpc.largeImageKey = Large_Image_Key_That_We_Will_Use;
        rpc.largeImageText = Large_Image_Text_That_We_Will_Use;

        currentSmallImage = SmallImage.Snail;

        recompileLine1();
        recompileLine2();

        ticks = 0;
        line1Ticks = 0;
        line2Ticks = 0;
        lastWasInMainMenu = false;

        line1I = 0;
        line2I = 0;
    }

    @Override
    public void onDeactivate() {
        instance.Discord_ClearPresence();
        instance.Discord_Shutdown();
    }

    private void recompile(List<String> messages, List<Script> scripts) {
        scripts.clear();

        for (int i = 0; i < messages.size(); i++) {
            Parser.Result result = Parser.parse(messages.get(i));

            if (result.hasErrors()) {
                if (Utils.canUpdate()) {
                    MeteorStarscript.printChatError(i, result.errors.get(0));
                }

                continue;
            }

            scripts.add(Compiler.compile(result));
        }

        forceUpdate = true;
    }

    private void recompileLine1() {
        recompile(line1Strings.get(), line1Scripts);
    }

    private void recompileLine2() {
        recompile(line2Strings.get(), line2Scripts);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean update = false;

        // Image
        if (ticks >= 200 || forceUpdate) {
            currentSmallImage = currentSmallImage.next();
            currentSmallImage.apply();
            update = true;

            ticks = 0;
        }
        else ticks++;

        if (Utils.canUpdate()) {
            // Line 1
            if (line1Ticks >= line1UpdateDelay.get() || forceUpdate) {
                if (line1Scripts.size() > 0) {
                    int i = Utils.random(0, line1Scripts.size());
                    if (line1SelectMode.get() == SelectMode.Sequential) {
                        if (line1I >= line1Scripts.size()) line1I = 0;
                        i = line1I++;
                    }

                    try {
                        rpc.details = MeteorStarscript.ss.run(line1Scripts.get(i));
                    } catch (StarscriptError e) {
                        ChatUtils.error("Starscript", e.getMessage());
                    }
                }
                update = true;

                line1Ticks = 0;
            } else line1Ticks++;

            // Line 2
            if (line2Ticks >= line2UpdateDelay.get() || forceUpdate) {
                if (line2Scripts.size() > 0) {
                    int i = Utils.random(0, line2Scripts.size());
                    if (line2SelectMode.get() == SelectMode.Sequential) {
                        if (line2I >= line2Scripts.size()) line2I = 0;
                        i = line2I++;
                    }

                    try {
                        rpc.state = MeteorStarscript.ss.run(line2Scripts.get(i));
                    } catch (StarscriptError e) {
                        ChatUtils.error("Starscript", e.getMessage());
                    }
                }
                update = true;

                line2Ticks = 0;
            } else line2Ticks++;
        }
        else {
            if (!lastWasInMainMenu) {

                if (mc.currentScreen instanceof TitleScreen) rpc.state = "Looking at title screen";
                else if (mc.currentScreen instanceof SelectWorldScreen) rpc.state = "Selecting world";
                else if (mc.currentScreen instanceof CreateWorldScreen || mc.currentScreen instanceof EditGameRulesScreen) rpc.state = "Creating world";
                else if (mc.currentScreen instanceof EditWorldScreen) rpc.state = "Editing world";
                else if (mc.currentScreen instanceof LevelLoadingScreen) rpc.state = "Loading world";
                else if (mc.currentScreen instanceof SaveLevelScreen) rpc.state = "Saving world";
                else if (mc.currentScreen instanceof MultiplayerScreen) rpc.state = "Selecting server";
                else if (mc.currentScreen instanceof AddServerScreen) rpc.state = "Adding server";
                else if (mc.currentScreen instanceof ConnectScreen || mc.currentScreen instanceof DirectConnectScreen) rpc.state = "Connecting to server";
                else if (mc.currentScreen instanceof WidgetScreen) rpc.state = "Browsing GUI";
                else if (mc.currentScreen instanceof OptionsScreen || mc.currentScreen instanceof SkinOptionsScreen || mc.currentScreen instanceof SoundOptionsScreen || mc.currentScreen instanceof VideoOptionsScreen || mc.currentScreen instanceof ControlsOptionsScreen || mc.currentScreen instanceof LanguageOptionsScreen || mc.currentScreen instanceof ChatOptionsScreen || mc.currentScreen instanceof PackScreen || mc.currentScreen instanceof AccessibilityOptionsScreen) rpc.state = "Changing options";
                else if (mc.currentScreen instanceof CreditsScreen) rpc.state = "Reading credits";
                else if (mc.currentScreen instanceof RealmsScreen) rpc.state = "Browsing Realms";
                else {
                    String className = mc.currentScreen.getClass().getName();

                    if (className.startsWith("com.terraformersmc.modmenu.gui")) rpc.state = "Browsing mods";
                    else if (className.startsWith("me.jellysquid.mods.sodium.client")) rpc.state = "Changing options";
                    else rpc.state = "In main menu";
                }

                update = true;
            }
        }

        // Update
        if (update) instance.Discord_UpdatePresence(rpc);
        forceUpdate = false;
        lastWasInMainMenu = !Utils.canUpdate();

        instance.Discord_RunCallbacks();
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!Utils.canUpdate()) lastWasInMainMenu = false;
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WButton help = theme.button("Open documentation.");
        help.action = () -> Util.getOperatingSystem().open("https://github.com/MeteorDevelopment/meteor-client/wiki/Starscript");

        return help;
    }

    private enum SmallImage {
        MineGame("java", "JavaOnly loves lolis!"),
        Snail("destro", "_Destro01 own's no-one!");

        private final String key, text;

        SmallImage(String key, String text) {
            this.key = key;
            this.text = text;
        }

        void apply() {
            rpc.smallImageKey = key;
            rpc.smallImageText = text;
        }

        SmallImage next() {
            if (this == MineGame) return Snail;
            return MineGame;
        }
    }
}
