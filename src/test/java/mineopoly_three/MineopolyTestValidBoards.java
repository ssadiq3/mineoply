package mineopoly_three;

import mineopoly_three.action.TurnAction;
import mineopoly_three.strategy.MineoplyStrategy;
import mineopoly_three.strategy.PlayerBoardView;
import mineopoly_three.tiles.TileType;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class MineopolyTestValidBoards {
    MineoplyStrategy strategy;
    PlayerBoardView boardView;

    @Before
    public void setUp() {
        // This is run before every test
        TileType[][] boardTileTypes = new TileType[][]{
                {TileType.RED_MARKET, TileType.RESOURCE_DIAMOND, TileType.BLUE_MARKET},
                {TileType.RECHARGE, TileType.EMPTY, TileType.RESOURCE_RUBY},
                {TileType.RESOURCE_EMERALD, TileType.RED_MARKET, TileType.BLUE_MARKET}
        };
        boardView = new PlayerBoardView(boardTileTypes, new HashMap<>(), new Point(), new Point(), 0);
        strategy = new MineoplyStrategy();
        strategy.initialize(3, 5, 80, 200, boardView, boardView.getYourLocation(), true, new Random());
    }

    @Test
    public void testValidGoToRecharge() {
        TurnAction action = strategy.getTurnAction(boardView, null, 10, true);
        assertEquals(TurnAction.MOVE_UP, action);
    }

    @Test
    public void testValidMiningOnResource() {
        TurnAction action = strategy.getTurnAction(boardView, null, 80, true);
        assertEquals(TurnAction.MINE, action);
    }

    @Test
    public void testSellItemsAtFullInventory() {
        strategy.setNumItems(5);
        TurnAction action = strategy.getTurnAction(boardView, null, 80, true);
        assertEquals(TurnAction.MOVE_RIGHT, action);
    }
}
