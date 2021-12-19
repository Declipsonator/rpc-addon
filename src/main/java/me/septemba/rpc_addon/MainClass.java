package me.septemba.rpc_addon;

import me.septemba.rpc_addon.modules.LogoHUD;
import me.septemba.rpc_addon.modules.RPC;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.invoke.MethodHandles;

public class MainClass extends MeteorAddon {
	public static final Logger LOG = LogManager.getLogger();
	public static final Category CATEGORY = new Category("RPC");

	@Override
	public void onInitialize() {
		LOG.info("Initializing RPC Addon");

		// Required when using @EventHandler
		MeteorClient.EVENT_BUS.registerLambdaFactory("me.septemba.rpc_addon", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

		// Modules
		Modules.get().add(new RPC());

        // HUD

        HUD hud = Modules.get().get(HUD.class);
        hud.elements.add(new LogoHUD(hud));

    }

	@Override
	public void onRegisterCategories() {
		Modules.registerCategory(CATEGORY);
	}
}
