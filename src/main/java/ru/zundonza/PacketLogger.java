package ru.zundonza;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketLogger implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("packetlogger");

	@Override
	public void onInitialize() {
		LOGGER.info("PacketLogger by Zundonza initialized!");
	}
}
