package org.minerift.ether;

import org.minerift.ether.island.Tile;

/**
 * Algorithm for locating a Tile
 */
public class GridAlgorithm {

    /**
     * This method should not be necessary in most cases,
     * but exists in case a tile id is forgotten and needs
     * to be calculated from a tile.
     *
     * Island deletion should not require this method.
     *
     * Method loses accuracy after id 2147441940, and needs to be fixed.
     *
     */
    public int computeTileId(Tile tile) {

        // If positive diagonal
        if(tile.getX() >= 0 && tile.getX() == tile.getZ()) {
            // This is a diagonal tile
            // tile.x is a shellId; get diagonalId and return
            return getDiagonalId(tile.getX());
        }

        int shellId = Math.max(Math.abs(tile.getX()), Math.abs(tile.getZ()));
        int diagonalId = getDiagonalId(shellId);
        int previousDiagonalId = getDiagonalId(shellId - 1);

        Tile topLeft     = new Tile(-shellId, -shellId);
        Tile topRight    = new Tile( shellId, -shellId);
        Tile bottomLeft  = new Tile(-shellId,  shellId);
        Tile bottomRight = new Tile( shellId,  shellId);

        int numColumns = topRight.getZ() - bottomRight.getZ();

        if(isInRow(tile, topLeft, topRight)) {
            // Is in top row; traverse

            // Start in top left
            int startTileId = previousDiagonalId + 1;
            startTileId += tile.getX() - topLeft.getX();
            return startTileId;

        }


        if(isInRow(tile, bottomLeft, bottomRight)) {
            // Is in bottom row; traverse

            // Start in bottom right
            int endTileId = diagonalId;
            endTileId -= bottomRight.getX() - tile.getX();
            return endTileId;
        }

        // Tile is in a column
        if(tile.getX() > 0) {
            // Right

            int startTileId = previousDiagonalId + 1;
            startTileId += tile.getX() - topLeft.getX();

            startTileId += (tile.getZ() - topRight.getZ()) * 2;
            return startTileId;

        } else {
            // Left

            int startTileId = previousDiagonalId;
            startTileId += topRight.getX() - topLeft.getX();
            startTileId += (tile.getZ() - topLeft.getZ()) * 2;
            return startTileId;

        }
    }


    /**
     * Algorithm for calculating tile on grid from a tile id
     * @param tileId id of tile on grid
     * @return Tile coordinates
     */
    public Tile computeTile(int tileId) {

        if(tileId == 0) {
            return Tile.ZERO;
        }

        int shellId = getShellId(tileId);
        int nextDiagonalId = getDiagonalId(shellId);
        int previousDiagonalId = getDiagonalId(shellId - 1);

        // TODO: review this -> diagonal check + tile math here doesn't seem correct
        if(tileId > 0 && previousDiagonalId == tileId) { // If id is a diagonal
            // This is a positive diagonal tile; don't need to calculate
            return new Tile(tileId / 8, tileId / 8);
        }

        int tilesInShell = nextDiagonalId - previousDiagonalId;
        int tilesInRow = 3 + (2 * (shellId - 1));
        int tilesInColumn = tilesInShell - (tilesInRow * 2);
        int numColumns = tilesInColumn / 2;

        // Start at end; will work back from there
        Tile.Mutable tile = new Tile.Mutable(shellId, shellId);

        // Determine whether tile is in row or column
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
        if(tileId % 2 != 0) { // odd (right)

            // endTopRow is a reference point
            int endColumn = endTopRow + (numColumns * 2);
            tile.subtract(0, 1 + ((endColumn - tileId) / 2));
            return tile;

        } else { // even (left)

            int endColumn = startBottomRow - 2;
            tile.subtract((tilesInRow - 1), 1 + ((endColumn - tileId) / 2));

        }

        // If in row, check if in top or bottom row
        // Get end of row and work backwards to get tile

        // If in column, check if number is even or odd
        // If odd, tile is on right side
        // If even, tile is on left side
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


    private boolean inRange(int i, int min, int max) {
        return i >= min && i <= max;
    }

}
