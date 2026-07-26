package org.minerift.ether.panel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.minerift.ether.Ether;
import org.minerift.ether.user.EtherUser;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class PanelView {

    public static PanelView of(Panel panel) {
        Inventory view = panel.getTypeOrRows().isLeft()
                ? Bukkit.createInventory(null, panel.getTypeOrRows().getLeft(), panel.getTitle())
                : Bukkit.createInventory(null, panel.getTypeOrRows().getRight(), panel.getTitle());
        return new PanelView(panel, view);
    }

    @Getter private final Panel panel;
    @Getter private final Inventory view;

    // TODO: handle onOpen (whenever each PanelIcon is loaded, parse for placeholders?), onClick

    public List<EtherUser> getViewers() {
        return view.getViewers().stream()
                .map((he) -> Ether.inst().getUserManager().getUser(he.getUniqueId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public int getViewerCount() {
        return view.getViewers().size();
    }

}
