package mineopoly_three.strategy;

import mineopoly_three.action.TurnAction;
import mineopoly_three.game.Economy;
import mineopoly_three.item.InventoryItem;
import mineopoly_three.tiles.TileType;
import mineopoly_three.util.DistanceUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MineoplyStrategy implements MinePlayerStrategy {
    private int boardSize;
    private int maxInventorySize;
    private int numItems;
    private int maxCharge;
    private List<Point> marketTiles = new ArrayList<>();
    private List<Point> chargeTiles = new ArrayList<>();

    /**
     * Called at the start of every round
     *
     * @param boardSize The length and width of the square game board
     * @param maxInventorySize The maximum number of items that your player can carry at one time
     * @param maxCharge The amount of charge your robot starts with (number of tile moves before needing to recharge)
     * @param winningScore The first player to reach this score wins the round
     * @param startingBoard A view of the GameBoard at the start of the game. You can use this to pre-compute fixed
     *                       information, like the locations of market or recharge tiles
     * @param startTileLocation A Point representing your starting location in (x, y) coordinates
     *                              (0, 0) is the bottom left and (boardSize - 1, boardSize - 1) is the top right
     * @param isRedPlayer True if this strategy is the red player, false otherwise
     * @param random A random number generator, if your strategy needs random numbers you should use this.
     */
    @Override
    public void initialize(int boardSize, int maxInventorySize, int maxCharge, int winningScore, PlayerBoardView startingBoard, Point startTileLocation, boolean isRedPlayer, Random random) {
        this.boardSize = boardSize;
        this.maxInventorySize = maxInventorySize;
        this.maxCharge = maxCharge;
        //fill market and charge tiles-constant throughout game
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (startingBoard.getTileTypeAtLocation(x, y).equals(TileType.RED_MARKET)) {
                    marketTiles.add(new Point(x,y));
                }
                else if (startingBoard.getTileTypeAtLocation(x, y).equals(TileType.RECHARGE)) {
                    chargeTiles.add(new Point(x,y));
                }
            }
        }
    }

    /**
     * The main part of your strategy, this method returns what action your player should do on this turn
     *
     * @param boardView A PlayerBoardView object representing all the information about the board and the other player
     *                   that your strategy is allowed to access
     * @param economy The GameEngine's economy object which holds current prices for resources
     * @param currentCharge The amount of charge your robot has (number of tile moves before needing to recharge)
     * @param isRedTurn For use when two players attempt to move to the same spot on the same turn
     *                   If true: The red player will move to the spot, and the blue player will do nothing
     *                   If false: The blue player will move to the spot, and the red player will do nothing
     * @return The TurnAction enum for the action that this strategy wants to perform on this game turn
     */
    @Override
    public TurnAction getTurnAction(PlayerBoardView boardView, Economy economy, int currentCharge, boolean isRedTurn) {
        Point currentLocation = boardView.getYourLocation();

        //check if inventory is full and need to sell
        if (numItems == maxInventorySize) {
            return goToMarketTile(currentLocation);
        }

        //check if robot is on mined item and need to pick up
        if (isOnMinedItem(boardView, currentLocation)) {
            numItems++;
            return TurnAction.PICK_UP_RESOURCE;
        }

        //find closest recharge station and check if charge is necessary
        Point closestRecharge = getClosestRecharge(currentLocation);
        if (closestRecharge.equals(currentLocation) && currentCharge != maxCharge) {
            //stay on charge until full
            return null;
        } else if (currentCharge <= (maxCharge / 4)) {
            //move towards charge station if necessary
            return moveToPoint(currentLocation, getClosestRecharge(currentLocation));
        }

        //if none of above are necessary, find next item to mine
        return findResource(boardView, currentLocation);
    }

    /**
     * Mines gem if on resource tile, otherwise moves towards nearest item
     * @param boardView current state of board
     * @param currentLocation current location of robot
     * @return mine turn action if on gem, or move action towards resource tile
     */
    private TurnAction findResource(PlayerBoardView boardView, Point currentLocation) {
        List<Point> resourceTiles = getResourceLocations(boardView); //gets all resource points
        if (getResourceLocations(boardView) == null) {
            return null;
        }
        Point closestResource = resourceTiles.get(0);
        int minDistance = DistanceUtil.getManhattanDistance(currentLocation, closestResource);

        //iterate through points in list and find closest one
        for (Point point : resourceTiles) {
            int thisDistance = DistanceUtil.getManhattanDistance(currentLocation, point);
            if (thisDistance < minDistance) {
                closestResource = point;
                minDistance = thisDistance;
            }
        }

        //if robot is on resource, mine
        if (minDistance == 0) {
            return TurnAction.MINE;
        }
        //Move to next closest point
        return moveToPoint(currentLocation, closestResource);
    }

    /**
     * Finds nearest market tile and moves towards it
     * @param currentLocation current location of robot
     * @return move action towards market tile
     */
    private TurnAction goToMarketTile(Point currentLocation) {
        if (marketTiles == null || marketTiles.isEmpty()) {
            throw new IllegalStateException();
        }
        Point closestMarket = marketTiles.get(0);
        int firstDistance = DistanceUtil.getManhattanDistance(currentLocation, marketTiles.get(0));
        int secondDistance = DistanceUtil.getManhattanDistance(currentLocation, marketTiles.get(1));

        //find which market tile is closer
        if (secondDistance < firstDistance) {
            closestMarket = marketTiles.get(1);
        }
        //if robot is on market, reset inventory count
        if (closestMarket.equals(currentLocation)) {
            numItems = 0;
        }
        //Move to closer market found
        return moveToPoint(currentLocation, closestMarket);
    }

    /**
     * Moves towards destination from current location
     * @param currentLocation current location of robot
     * @param destination point to move toward
     * @return direction move action
     */
    private TurnAction moveToPoint(Point currentLocation, Point destination) {
        if (currentLocation.x > destination.x) {
            return TurnAction.MOVE_LEFT;
        } else if (currentLocation.x < destination.x) {
            return TurnAction.MOVE_RIGHT;
        } else if (currentLocation.y > destination.y) {
            return TurnAction.MOVE_DOWN;
        } else {
            return TurnAction.MOVE_UP;
        }
    }

    /**
     * Checks if robot is on mined item
     * @param boardView current state of the board
     * @param currentLocation current location of board
     * @return true if on mined item, false otherwise
     */
    private boolean isOnMinedItem(PlayerBoardView boardView, Point currentLocation) {
        if (boardView.getItemsOnGround().get(currentLocation) != null) {
            List<InventoryItem> items = boardView.getItemsOnGround().get(currentLocation);
            //checks if items list at location contains an item, and there is inventory space
            if (!items.isEmpty() && items.get(0) != null && numItems < maxInventorySize) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds nearest recharge station
     * @param currentLocation current location of robot
     * @return point of closest recharge station
     */
    private Point getClosestRecharge(Point currentLocation) {
        if (chargeTiles == null || chargeTiles.isEmpty()) {
            throw new IllegalStateException();
        }
        Point closestRecharge = chargeTiles.get(0);
        int minDistance = DistanceUtil.getManhattanDistance(currentLocation, closestRecharge);

        //iterate through points in charge tiles and find closest point
        for (Point point : chargeTiles) {
            int thisDistance = DistanceUtil.getManhattanDistance(currentLocation, point);
            if (thisDistance < minDistance) {
                closestRecharge = point;
            }
        }
        return closestRecharge;
    }

    /**
     * creates a list of all resource locations on map
     * @param boardView current state of board
     * @return list of all resource points
     */
    private List<Point> getResourceLocations(PlayerBoardView boardView) {
        List<Point> resourceTiles = new ArrayList<>();
        //iterate through points in getItemsOnGround and add all locations with resources
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (boardView.getTileTypeAtLocation(x, y).equals(TileType.RESOURCE_DIAMOND)
                        || boardView.getTileTypeAtLocation(x, y).equals(TileType.RESOURCE_EMERALD)
                        || boardView.getTileTypeAtLocation(x, y).equals(TileType.RESOURCE_RUBY)) {
                    resourceTiles.add(new Point(x, y));
                }
            }
        }
        if (resourceTiles.isEmpty()) {
            return null;
        }
        return resourceTiles;
    }

    /**
     * No code- written from MinePlayerStrategyInterface
     * @param itemReceived The item received from the player's TurnAction on their last turn
     */
    @Override
    public void onReceiveItem(InventoryItem itemReceived) {

    }

    /**
     * No code- written from MinePlayerStrategyInterface
     * @param totalSellPrice The combined sell price for all items in your strategy's inventory
     */
    @Override
    public void onSoldInventory(int totalSellPrice) {

    }

    /**
     * Gets the name of this strategy. The amount of characters that can actually be displayed on a screen varies,
     *  although by default at screen size 750 it's about 16-20 characters depending on character size
     *
     * @return The name of your strategy for use in the competition and rendering the scoreboard on the GUI
     */
    @Override
    public String getName() {
        return "Saad's Strategy";
    }

    /**
     * Called at the end of every round to let players reset, and tell them how they did if the strategy does not
     *  track that for itself
     *
     * @param pointsScored The total number of points this strategy scored
     * @param opponentPointsScored The total number of points the opponent's strategy scored
     */
    @Override
    public void endRound(int pointsScored, int opponentPointsScored) {
        //reset items and location lists
        numItems = 0;
        marketTiles.clear();
        chargeTiles.clear();
    }

    public void setNumItems(int testItems) {
        numItems = testItems;
    }

}
