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

public class MineoplyTestGameOver {
    MineoplyStrategy strategy;
    PlayerBoardView boardView;

    @Before
    public void setUp() {
        TileType[][] boardTileTypes = new TileType[][]{
                {TileType.RED_MARKET, TileType.EMPTY, TileType.EMPTY},
                {TileType.EMPTY, TileType.RECHARGE, TileType.EMPTY},
                {TileType.EMPTY, TileType.EMPTY, TileType.BLUE_MARKET}
        };
        boardView = new PlayerBoardView(boardTileTypes, new HashMap<>(), new Point(), new Point(), 0);
        strategy = new MineoplyStrategy();
        strategy.initialize(3, 5, 80, 200, boardView, boardView.getYourLocation(), true, new Random());
    }

    @Test
    public void testInvalidRechargeThreshold() {
        TurnAction action = strategy.getTurnAction(boardView, null, 21, true);
        assertEquals(null, action);
    }

    @Test
    public void testInvalidSearchForItems() {
        TurnAction action = strategy.getTurnAction(boardView, null, 80, true);
        assertEquals(null, action);
    }

    @Test
    public void testProperRechargeThreshold() {
        TurnAction action = strategy.getTurnAction(boardView, null, 19, true);
        assertEquals(TurnAction.MOVE_RIGHT, action);
    }
}
