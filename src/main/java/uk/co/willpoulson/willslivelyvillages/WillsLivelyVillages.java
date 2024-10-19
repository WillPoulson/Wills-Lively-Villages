package uk.co.willpoulson.willslivelyvillages;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.willpoulson.willslivelyvillages.events.PlayerEventsListener;
import uk.co.willpoulson.willslivelyvillages.events.VillagerEventsListener;
import uk.co.willpoulson.willslivelyvillages.managers.VillageNameManager;
import uk.co.willpoulson.willslivelyvillages.managers.VillagerNameManager;

public class WillsLivelyVillages implements ModInitializer {
	public static final String MOD_ID = "wills-lively-villages";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		VillagerNameManager.loadVillagerNames();
		VillageNameManager.loadVillageNames();
		VillagerEventsListener.listenForVillagerSpawns();
		PlayerEventsListener.listenForPlayerEnterVillage();
		LOGGER.info("Will's: Lively Villages Initialised");
	}
}