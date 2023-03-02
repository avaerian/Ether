package org.minerift.ether;

import org.apache.commons.lang3.Validate;
import org.minerift.ether.island.Tile;

/**
 * Algorithm for calculating tile coordinates and tile ids.
 * @author Avaerian
 */
public class GridAlgorithm {

    /**
     * Algorithm for calculating a tile id from tile coordinates.
     * @param tile tile coordinates
     * @return integer id of the tile
     */
    public int computeTileId(Tile tile) {

        Validate.notNull(tile, "Tile cannot be null!");

        // Check if tile is a positive diagonal tile
        if(tile.getX() >= 0 && tile.getX() == tile.getZ()) {
            return getDiagonalId(tile.getX());
        }

        int shellId = Math.max(Math.abs(tile.getX()), Math.abs(tile.getZ()));
        int diagonalId = getDiagonalId(shellId);
        int previousDiagonalId = getDiagonalId(shellId - 1);

        Tile topLeft     = new Tile(-shellId, -shellId);
        Tile topRight    = new Tile( shellId, -shellId);
        Tile bottomLeft  = new Tile(-shellId,  shellId);
        Tile bottomRight = new Tile( shellId,  shellId);

        // int numColumns = topRight.getZ() - bottomRight.getZ();

        // Check if tile is in top row
        if(isInRow(tile, topLeft, topRight)) {
            // Start in top left
            int startTileId = previousDiagonalId + 1;
            startTileId += tile.getX() - topLeft.getX();
            return startTileId;
        }

        // Check if tile is in bottom row
        if(isInRow(tile, bottomLeft, bottomRight)) {
            // Start in bottom right
            int endTileId = diagonalId;
            endTileId -= bottomRight.getX() - tile.getX();
            return endTileId;
        }

        // Tile is in a column
        int startTileId;
        if(tile.getX() > 0) {
            // Right
            startTileId = previousDiagonalId + 1;
            startTileId += tile.getX() - topLeft.getX();
            startTileId += (tile.getZ() - topRight.getZ()) * 2;
        } else {
            // Left
            startTileId = previousDiagonalId;
            startTileId += topRight.getX() - topLeft.getX();
            startTileId += (tile.getZ() - topLeft.getZ()) * 2;
        }

        return startTileId;
    }

    /**
     * Algorithm for calculating tile coordinates from a tile id.
     * @param tileId id of tile on grid
     * @return Tile coordinates
     */
    public Tile computeTile(int tileId) {

        Validate.isTrue(tileId >= 0, "tileId needs to be a positive number!");

        if(tileId == 0) {
            return Tile.ZERO;
        }

        int shellId = getShellId(tileId);
        int nextDiagonalId = getDiagonalId(shellId);
        int previousDiagonalId = getDiagonalId(shellId - 1);

        int tilesInShell = nextDiagonalId - previousDiagonalId;
        int tilesInRow = 3 + (2 * (shellId - 1));
        int tilesInColumn = tilesInShell - (tilesInRow * 2);
        int numColumns = tilesInColumn / 2;

        // Start at end of shell; will work back from there
        Tile.Mutable tile = new Tile.Mutable(shellId, shellId);

        int startTopRow = previousDiagonalId + 1;
        int endTopRow = startTopRow + (tilesInRow - 1);
        int startBottomRow = nextDiagonalId - (tilesInRow - 1);
        int endBottomRow = nextDiagonalId; // redundant, but provides clarity

        // Check if tile is in bottom row
        if(inRange(tileId, startBottomRow, endBottomRow)) {
            tile.subtract(endBottomRow - tileId, 0);
            return tile;
        }

        // Check if tile is in top row
        if(inRange(tileId, startTopRow, endTopRow)) {
            tile.subtract(endTopRow - tileId, numColumns + 1);
            return tile;
        }

        // Tile is in a column
        if(tileId % 2 != 0) {
            // Odd -> right column
            int endColumn = endTopRow + (numColumns * 2);
            tile.subtract(0, 1 + ((endColumn - tileId) / 2));

        } else {
            // Even -> left column
            int endColumn = startBottomRow - 2;
            tile.subtract((tilesInRow - 1), 1 + ((endColumn - tileId) / 2));
        }

        return tile;
    }

    // Get the shell id from a tile id
    private int getShellId(int tileId) {
        double shellId = (Math.sqrt(tileId + 1) / 2) - 0.5;
        return (int) Math.ceil(shellId);
    }

    // Get diagonal id from shell id
    private int getDiagonalId(int shellId) {
        return (4 * shellId) * (shellId + 1);
    }

    private boolean isInRow(Tile tile, Tile left, Tile right) {
        // Left is negative (lower bound)
        // Right is positive (upper bound)
        return inRange(tile.getX(), left.getX(), right.getX()) && tile.getZ() == left.getZ();
    }

    // Inclusive for min and max bounds
    private boolean inRange(int i, int min, int max) {
        return i >= min && i <= max;
    }

}
