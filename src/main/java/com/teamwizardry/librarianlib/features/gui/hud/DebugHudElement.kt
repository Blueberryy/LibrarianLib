package com.teamwizardry.librarianlib.features.gui.hud

import net.minecraftforge.client.event.RenderGameOverlayEvent

class DebugHudElement(type: RenderGameOverlayEvent.ElementType): HudElement(type) {
    override fun layoutChildren() {
        super.layoutChildren()
        this.frame = root.bounds
    }
}