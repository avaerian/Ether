package org.minerift.ether.panel;

import org.minerift.ether.nms.world.ItemStack;
import org.minerift.ether.user.EtherUser;

import java.util.function.Consumer;

public record PanelIcon(ItemStack icon, Consumer<EtherUser> onClick) {

    public static PanelIcon of(ItemStack icon) {
        return new PanelIcon(icon, null);
    }

    public static PanelIcon of(ItemStack icon, Consumer<EtherUser> onClick) {
        return new PanelIcon(icon, onClick);
    }


}
