package mineopoly_three;

import mineopoly_three.action.TurnAction;
import mineopoly_three.strategy.MineoplyStrategy;
import mineopoly_three.strategy.PlayerBoardView;
import mineopoly_three.tiles.TileType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class MineoplyTestEmptyBoard {
    MineoplyStrategy strategy;
    PlayerBoardView boardView;

    @Before
    public void setUp() {
        TileType[][] boardTileTypes = new TileType[][]{
                {TileType.EMPTY, TileType.EMPTY, TileType.EMPTY},
                {TileType.EMPTY, TileType.EMPTY, TileType.EMPTY},
                {TileType.EMPTY, TileType.EMPTY, TileType.EMPTY}
        };
        boardView = new PlayerBoardView(boardTileTypes, new HashMap<>(), new Point(), new Point(), 0);
        strategy = new MineoplyStrategy();
        strategy.initialize(3, 5, 80, 200, boardView, boardView.getYourLocation(), true, new Random());
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidBoardGoToMarketTile() {
        strategy.setNumItems(5);
        strategy.getTurnAction(boardView, null, 80, true);
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidBoardGoToRechargeTile() {
        strategy.getTurnAction(boardView, null, 15, true);
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidFindResource() {
        strategy.getTurnAction(boardView, null, 80, true);
    }
}
