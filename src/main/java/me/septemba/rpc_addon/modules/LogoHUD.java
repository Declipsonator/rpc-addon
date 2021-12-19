package me.septemba.rpc_addon.modules;

import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.HudElement;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.Identifier;

public class LogoHUD extends HudElement {
    private static final Identifier LOGO = new Identifier("addon", "logo.png");
    private static final Identifier LOGO_FLAT = new Identifier("addon", "logo_grayscale.png");

    private static final RainbowColor RAINBOW = new RainbowColor();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    //scale

    private final Setting<Double> scale = sgGeneral.add(
        new DoubleSetting.Builder()
            .name("scale")
            .description("The scale.")
            .defaultValue(1)
            .min(1)
            .sliderMin(1)
            .sliderMax(5)
            .build());

    //chroma

    public final Setting<Boolean> chroma = sgGeneral.add(
        new BoolSetting.Builder()
            .name("chroma")
            .description("Chroma logo animation.")
            .defaultValue(false)
            .build()
    );

    //chroma sped

    private final Setting<Double> chromaSped = sgGeneral.add(
        new DoubleSetting.Builder()
            .name("chroma-sped")
            .description("Sped of the chroma animation.")
            .defaultValue(0.09)
            .min(0.01)
            .sliderMax(5)
            .decimalPlaces(2)
            .build()
    );

    //coloure

    private final Setting<SettingColor> colour = sgGeneral.add(
        new ColorSetting.Builder().name("background-colour")
            .description("Color of the background.")
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
    );

    public LogoHUD(HUD hud) {
        super(hud, "logo", "Displays the logo in your texture pack assets/addon/logo.png or assets/addon/logo_grayscale.png if u want with chroma.");
    }

    @Override
    public void update(HudRenderer renderer) {
        box.setSize(96 * scale.get(), 96 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        if (!Utils.canUpdate()) return;
        double x = box.getX();
        double y = box.getY();
        int w = (int) box.width;
        int h = (int) box.height;
        if (chroma.get()) {
            GL.bindTexture(LOGO_FLAT);
        } else {
            GL.bindTexture(LOGO);
        }
        Renderer2D.TEXTURE.begin();
        if (chroma.get()) {
            RAINBOW.setSpeed(chromaSped.get() / 100);
            Renderer2D.TEXTURE.texQuad(x, y, w, h, RAINBOW.getNext());
        } else {
            Renderer2D.TEXTURE.texQuad(x, y, w, h, colour.get());
        }
        Renderer2D.TEXTURE.render(null);
    }
}
