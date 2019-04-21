/**
 * Created by Mathias on 27-02-2018.
 */
public class TTAIV3 implements IOthelloAI {
    int player;
    int opponent;

    private static final int MAX_DEPTH = 15;
    private static final int[][] STABILITY_BOARD = {
            {4, -3, 2, 2, 2, 2, -3, 4},
            {-3, -4, -1, -1, -1, -1, -4, -3},
            {2, -1, 2, 0, 0, 2, -1, 2},
            {2, -1, 0, 1, 1, 0, -1, 2},
            {2, -1, 0, 1, 1, 0, -1, 2},
            {2, -1, 2, 0, 0, 2, -1, 2},
            {-3, -4, -1, -1, -1, -1, -4, -3},
            {4, -3, 2, 2, 2, 2, -3, 4},
    };

    @Override
    public Position decideMove(GameState s) {
        player = s.getPlayerInTurn();
        opponent = 3-player;
        Result r = MAXValue(s, s, Integer.MIN_VALUE, Integer.MAX_VALUE, null, 0);
        return r.action;
    }

    /**
     * Maximizes minimax values in the nodes below
     * @param s This node's game state
     * @param prevS The game state of the previous turn
     * @param alpha The alpha value (for alpha-beta pruning)
     * @param beta The beta value (for alpha-beta pruning)
     * @param resultingAction The move/action taken to get here
     * @param depth The current depth
     * @return A Result object containing the best minimax value and its associated move
     */
    private Result MAXValue(GameState s, GameState prevS, int alpha, int beta, Position resultingAction, int depth) {
        if(CutOff(s, depth)) return Eval(s, prevS, resultingAction, depth);
        int v = Integer.MIN_VALUE;
        Position vPos = new Position(-1,-1);
        for (Position a : s.legalMoves()) {
            GameState gs = new GameState(s.getBoard(), s.getPlayerInTurn());
            gs.insertToken(a);
            Result min = MINValue(gs, s, alpha, beta, a, ++depth);
            if(v <= min.v) {
                v = Math.max(v, min.v);
                vPos = a;
            }
            if (v >= beta) return new Result(v, vPos);
            alpha = Math.max(alpha, v);
        }
        return new Result(v, vPos);
    }

    /**
     * Minimizes minimax values in the nodes below
     * @param s This node's game state
     * @param prevS The game state of the previous turn
     * @param alpha The alpha value (for alpha-beta pruning)
     * @param beta The beta value (for alpha-beta pruning)
     * @param resultingAction The move/action taken to get here
     * @param depth The current depth
     * @return A Result object containing the best minimax value and its associated move
     */
    private Result MINValue(GameState s, GameState prevS, int alpha, int beta, Position resultingAction, int depth) {
        if(CutOff(s, depth)) return Eval(s, prevS, resultingAction, depth);
        int v = Integer.MAX_VALUE;
        Position vPos = new Position(-1,-1);
        for (Position a : s.legalMoves()) {
            GameState gs = new GameState(s.getBoard(), s.getPlayerInTurn());
            gs.insertToken(a);
            Result max = MAXValue(gs, s, alpha, beta, a, ++depth);
            if(v >= max.v) {
                v = Math.min(v, max.v);
                vPos = a;
            }
            if (v <= alpha) return new Result(v, vPos);
            beta = Math.min(beta, v);
        }
        return new Result(v, vPos);
    }

    /**
     * The evaluation function. Evaluates the current state of the game
     * @param s This node's game state
     * @param prevS The game state of the previous turn
     * @param move The move/action taken to get here
     * @param depth The current depth
     * @return A Result object containing the calculated evaluation and the move needed to get there
     */
    private Result Eval(GameState s, GameState prevS, Position move, int depth) {
        int v;
        if (depth < MAX_DEPTH) v = CoinDifEval(s);
        else v = CornerEval(s)*20000 + CoinDifEval(s)*50 + StabilityEval(s)*400 + MobilityEval(s, prevS)*80 + borderLineEval(s)*400 + frontierEval(s)*75;
        return new Result(v, move);
    }

    public void printBoard(GameState s) {
        for (int i = 0; i < s.getBoard().length; i++) {
            for (int j = 0; j < s.getBoard().length; j++) {
                System.out.print(s.getBoard()[i][j] + " ");
            }
            System.out.println("");
        }
    }

    /**
     * Evaluates the percentage of frontiers for MAX out of all appearing frontiers
     * @param s The current game state
     * @return The percentage of frontiers for MAX out of all appearing frontiers
     */
    private int frontierEval(GameState s) {
        int max_frontiers = 0;
        int min_frontiers = 0;

        for (int i = 0; i < s.getBoard().length; i++) {
            for (int j = 0; j < s.getBoard().length; j++) {
                if(s.getBoard()[i][j] == 0) continue;
                int freeSpaces = 0;
                if(i-1 > 0 && j-1 > 0 && s.getBoard()[i-1][j-1]==0) freeSpaces++;
                if(j-1 > 0 && s.getBoard()[i][j-1]==0) freeSpaces++;
                if(i-1 > 0 && s.getBoard()[i-1][j]==0) freeSpaces++;
                if(i+1 < s.getBoard().length && j+1 < s.getBoard().length && s.getBoard()[i+1][j+1]==0) freeSpaces++;
                if(i+1 < s.getBoard().length && s.getBoard()[i+1][j]==0) freeSpaces++;
                if(j+1 < s.getBoard().length && s.getBoard()[i][j+1]==0) freeSpaces++;
                if(i-1 > 0 && j+1 < s.getBoard().length && s.getBoard()[i-1][j+1]==0) freeSpaces++;
                if(i+1 < s.getBoard().length && j-1 > 0 && s.getBoard()[i+1][j-1]==0) freeSpaces++;

                if(freeSpaces >= 1 && s.getBoard()[i][j]==player) max_frontiers++;
                else if(freeSpaces >= 1 && s.getBoard()[i][j]==opponent) min_frontiers++;
            }
        }

        if(min_frontiers+max_frontiers == 0) return 0;
        return 100*(min_frontiers-max_frontiers)/(max_frontiers+min_frontiers);
    }

    /**
     * Evaluates the percentage of captured border lines for MAX out of all appearing captured border lines
     * @param s The current game state
     * @return The percentage of captured border lines for MAX out of all appearing captured border lines
     */
    private int borderLineEval(GameState s) {
        int max_lines = 0;
        int min_lines = 0;

        /*Top row*/
        for(int i = 0; i < s.getBoard().length; i++) {
            if(i==0) continue;
            if(s.getBoard()[i][0] != s.getBoard()[i-1][0] ) break;
            else if(i == s.getBoard().length-1) {
                if (s.getBoard()[i][0] == player) max_lines++;
                else if (s.getBoard()[i][0] == opponent) min_lines++;
            }
        }
        /*Bottom row*/
        for(int i = 0; i < s.getBoard().length; i++) {
            if(i==0) continue;
            if(s.getBoard()[i][s.getBoard().length-1] != s.getBoard()[i-1][s.getBoard().length-1] ) break;
            else if(i == s.getBoard().length-1) {
                if (s.getBoard()[i][s.getBoard().length-1] == player) max_lines++;
                else if (s.getBoard()[i][s.getBoard().length-1] == opponent) min_lines++;
            }
        }
        /*Right row*/
        for(int i = 0; i < s.getBoard().length; i++) {
            if(i==0) continue;
            if(s.getBoard()[s.getBoard().length-1][i] != s.getBoard()[s.getBoard().length-1][i-1] ) break;
            else if(i == s.getBoard().length-1) {
                if (s.getBoard()[s.getBoard().length-1][i] == player) max_lines++;
                else if (s.getBoard()[s.getBoard().length-1][i] == opponent) min_lines++;
            }
        }
        /*Left row*/
        for(int i = 0; i < s.getBoard().length; i++) {
            if(i==0) continue;
            if(s.getBoard()[0][i] != s.getBoard()[0][i-1] ) break;
            else if(i == s.getBoard().length-1) {
                if (s.getBoard()[0][i] == player) max_lines++;
                else if (s.getBoard()[0][i] == opponent) min_lines++;
            }
        }

        if(max_lines+min_lines==0) return 0;
        return 100*(max_lines-min_lines)/(max_lines+min_lines);
    }

    /**
     * Evaluates the percentage of coins for MAX out of all appearing coins
     * @param s The current game state
     * @return The percentage of coins for MAX out of all appearing coins
     */
    private int CoinDifEval(GameState s) {
        return 100*(s.countTokens()[player-1]-s.countTokens()[opponent-1])/(s.countTokens()[player-1]+s.countTokens()[opponent-1]);
    }

    /**
     * Evaluates the percentage of captured corners for MAX out of all captured corners
     * @param s The current game state
     * @return The percentage of captured corners for MAX out of all captured corners
     */
    private int CornerEval(GameState s) {
        int max_corners = 0;
        int min_corners = 0;

        if (s.getBoard()[0][0] == player) max_corners++;
        else if(s.getBoard()[0][0] == opponent) min_corners++;
        if (s.getBoard()[0][s.getBoard().length-1] == player) max_corners++;
        else if(s.getBoard()[0][s.getBoard().length-1] == opponent) min_corners++;
        if (s.getBoard()[s.getBoard().length-1][0] == player) max_corners++;
        else if(s.getBoard()[s.getBoard().length-1][0] == opponent) min_corners++;
        if (s.getBoard()[s.getBoard().length-1][s.getBoard().length-1] == player) max_corners++;
        else if(s.getBoard()[s.getBoard().length-1][s.getBoard().length-1] == opponent) min_corners++;

        if(max_corners+min_corners==0) return 0;
        return 100*(max_corners-min_corners)/(max_corners+min_corners);
    }

    /**
     * Evaluates the percentage of possible moves for MAX out of all possible moves in this and the previous game state
     * @param s The current game state
     * @param prevS The previous game state
     * @return The percentage of possible moves for MAX out of all possible moves
     */
    private int MobilityEval(GameState s, GameState prevS) {
        int min_moves = s.getPlayerInTurn() == opponent ? s.legalMoves().size() : prevS.legalMoves().size();
        int max_moves = s.getPlayerInTurn() == player ? s.legalMoves().size() : prevS.legalMoves().size();
        if(min_moves+max_moves==0) return 0;
        return 100*(max_moves-min_moves)/(min_moves+max_moves);
    }

    /**
     * Evaluates the percentage of frontiers for MAX out of all appearing frontiers
     * @param s The current gam state
     * @return The percentage of frontiers for MAX out of all appearing frontiers
     */
    private int StabilityEval(GameState s) {
        int max_stability = 0;
        int min_stability = 0;
        for(int i = 0; i < s.getBoard().length; i++) {
            for(int j = 0; j < s.getBoard().length; j++) {
                if(s.getBoard()[i][j] == player) {
                    max_stability += STABILITY_BOARD[i][j];
                } else if(s.getBoard()[i][j] == opponent) {
                   min_stability += STABILITY_BOARD[i][j];
                }
            }
        }
        if(max_stability+min_stability==0) return 0;
        return 100*(max_stability-min_stability)/(max_stability+min_stability);
    }

    /**
     * Evaluates whether the search should be cut off
     * @param s The current game state
     * @param depth The current depth
     * @return Whether the search should be cut off
     */
    private boolean CutOff(GameState s, int depth) {
        if (s.isFinished() || depth >= MAX_DEPTH) return true;
        return false;
    }

    /**
     * A tuple class to return a node's minimax value and the action taken to get there.
     */
    static class Result {
        public final int v;
        public final Position action;

        Result(int v, Position action) {
            this.v = v;
            this.action = action;
        }
    }
}
