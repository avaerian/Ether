package org.minerift.ether.panel;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.Range;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.Either;

import static java.lang.String.format;

/**
 * Panel API for creating interactable interfaces in Bukkit/Paper.
 *
 * <p>
 * <b>NOTE:</b> this API will likely not be used in Fabric or other modules
 * outside of Bukkit/Paper, and is subject to change.
 *
 *
 * <p>
 * Because Fabric allows the custom Menu creation, this API for Bukkit/Paper
 * aims to be very simple until future notice.
 */
public class Panel {

    public static Builder builder(Component title, InventoryType type) {
        return new Builder(title, Either.left(type));
    }

    public static Builder builder(Component title, @Range(from = 1, to = 6) int rows) {
        return new Builder(title, Either.right(rows));
    }

    @Getter private final Component title;
    @Getter private final PanelIcon[] icons;
    @Getter private final Either<InventoryType, Integer> typeOrRows;

    public Panel(Component title, PanelIcon[] icons, Either<InventoryType, Integer> typeOrRows) {
        this.title = title;
        this.icons = icons;
        this.typeOrRows = typeOrRows;
    }

    public int getPanelSize() {
        return typeOrRows.map(InventoryType::getDefaultSize, i -> i);
    }

    public PanelView open(EtherUser user) {
        PanelView view = PanelView.of(this);
        user.getPlayer().orElseThrow().openInventory(view.getView());
        return view;
    }

    public PanelView open() {
        return PanelView.of(this);
    }

    @Getter @Setter
    public static class Builder {
        private Component title;
        private PanelIcon[] icons;
        private Either<InventoryType, Integer> typeOrRows;

        private Builder(Component title, Either<InventoryType, Integer> typeOrRows) {
            this.title = title;
            this.typeOrRows = typeOrRows;
        }

        public int getPanelSize() {
            return typeOrRows.map(InventoryType::getDefaultSize, i -> i);
        }

        public Builder put(int slot, PanelIcon icon) {
            if(icons == null)
                icons = new PanelIcon[getPanelSize()];

            if(slot > icons.length)
                throw new IllegalArgumentException(format("Slot (%d) exceeds panel size (%d)", slot, getPanelSize()));

            icons[slot] = icon;
            return this;
        }

        public Builder remove(int slot) {
            if(icons != null) {
                if(slot > icons.length)
                    throw new IllegalArgumentException(format("Slot (%d) exceeds panel size (%d)", slot, getPanelSize()));
                icons[slot] = null;
            }
            return this;
        }

        public Panel build() {
            return new Panel(title, icons, typeOrRows);
        }
    }

}
