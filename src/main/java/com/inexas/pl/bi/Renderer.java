package com.inexas.pl.bi;

abstract class Renderer {
	protected final DataView plugin;
	
	Renderer(DataView plugin) {
		this.plugin = plugin;
    }

	public abstract void render();
}