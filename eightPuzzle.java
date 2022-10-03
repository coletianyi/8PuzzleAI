import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class eightPuzzle {
    static class Puzzle {
        final int[][] SOLVED_PUZZLE = new int[][]{{0, 1, 2}, {3, 4, 5}, {6, 7, 8}};
        int[][] puzzle;
        boolean[] moves;

        public Puzzle (){
            puzzle = SOLVED_PUZZLE;
        }

        public Puzzle (int[][] puzzle){
            this.puzzle = puzzle;
            moves = possibleMoves();
        }

        private int[] zeroPos(){
            for (int i = 0; i < puzzle.length; i++) {
                for (int j = 0; j < puzzle.length; j++){
                    if (puzzle[i][j] == 0)
                        return new int[]{i, j};
                }
            }
            return new int[]{-1, -1};
        }

        boolean[] possibleMoves(){
            int[] zero = zeroPos();
            return new boolean[]{zero[0] !=0, zero[0] != 2, zero[1] != 0, zero[1] != 2};
        }

        void shuffle(int num){
            Random rand = new Random();
            for (int i = 0; i < num; i++){
                boolean[] moves = possibleMoves();
                int randInt = rand.nextInt(4);
                if (moves[randInt]){
                    Puzzle newPuzzle = swap(this, randInt, zeroPos());
                    puzzle = newPuzzle.puzzle;
                }
                else {i--;}
            }
        }

        private int[][] copy (int[][] puzzle){
            int[][] ret = new int[puzzle.length][puzzle[0].length];
            for (int i = 0; i < puzzle.length; i++){
                for (int j = 0; j < puzzle[i].length; j++){
                    ret[i][j] = puzzle[i][j];
                }
            }
            return ret;
        }

        private Puzzle swap (Puzzle p, int move, int[] zeroPosition){
            int[][] puzz = copy(p.puzzle);
            int i = zeroPosition[0];
            int j = zeroPosition[1];
            if (move == 0){
                int temp = puzz[i][j];
                puzz[i][j] = puzz[i-1][j];
                puzz[i-1][j] = temp;
            }
            if (move == 1){
                int temp = puzz[i][j];
                puzz[i][j] = puzz[i+1][j];
                puzz[i+1][j] = temp;
            }
            if (move == 2){
                int temp = puzz[i][j];
                puzz[i][j] = puzz[i][j-1];
                puzz[i][j-1] = temp;
            }
            if (move == 3){
                int temp = puzz[i][j];
                puzz[i][j] = puzz[i][j+1];
                puzz[i][j+1] = temp;
            }
            Puzzle ret = new Puzzle(puzz);
            return ret;
        }

        public String toString (){
            String ret = "";
            for (int i = 0; i < puzzle.length; i++){
                for (int j = 0; j < puzzle[i].length; j++){
                    ret += puzzle[i][j] + " ";
                }
                ret += "\n";
            }
            return ret;
        }
    }

    static class Problem {
        Node initialState;
        Node goalState;

        public Problem (Puzzle puzzle){
            initialState = new Node(puzzle);
            initialState.pathCost = 0;
            initialState.parent = null;
            initialState.action = -1;
            goalState = new Node(new Puzzle());
        }

        public boolean[] actions (Node state){
            return state.state.possibleMoves();
        }

        public Node result (Node node, int action){
            Node tempNode = new Node (node.state, node.parent, node.action, node.pathCost);

            Puzzle puzzle = new Puzzle(node.state.puzzle);
            Puzzle newPuzzle = new Puzzle(puzzle.swap(puzzle, action, puzzle.zeroPos()).puzzle);

            return new Node(newPuzzle, tempNode, action, node.pathCost+1);
        }

        public boolean isGoal(Node node){
            int[][] goalPuzzle = goalState.state.puzzle;
            int[][] nodePuzzle = node.state.puzzle;
            for (int i = 0; i < nodePuzzle.length; i++){
                for (int j = 0; j<nodePuzzle[i].length; j++){
                    if (nodePuzzle[i][j] != goalPuzzle[i][j]){
                        return false;
                    }
                }
            }
            return true;
        }

    }

    static class Node {
        Puzzle state;
        Node parent;
        int pathCost;
        int action;

        public Node(Puzzle state){
            this.state = state;
            parent = null;
            action = -1;
            pathCost = 0;
        }

        public Node(Puzzle state, Node parent, int action, int pathCost){
            this.state = state;
            this.parent = parent;
            this.action = action;
            this.pathCost = pathCost;
        }

        public String toString (){
            String ret = "";
            ret += "Node State: \n";
            ret += this.state.toString();
            ret += "Node parent" + "\n";
            if (this.parent == null) ret+="null" + "\n";
            else ret += this.parent.state.toString();
            ret += "Node action" + "\n";
            ret += this.action + "\n";
            ret += "Node cost" + "\n";
            ret += this.pathCost + "\n";
            System.out.println();
            return ret;
        }
    }

    public static String iterativeDeepeningSearch (Problem problem) {
        for (int i = 0; i < 50; i++){
            String result = depthLimitedSearch(problem, i);
            if (!result.equals("cutoff")){
                return result;
            }
        }
        return "didn't work lol";
    }

    public static String depthLimitedSearch (Problem problem, int depth){
        Stack<Node> frontier = new Stack<>();
        frontier.push(problem.initialState);
        String result = "failure";

        while (!frontier.isEmpty()){
            Node node = frontier.pop();
            if (problem.isGoal(node)){
                return solution(node);
            }
            if (node.pathCost > depth) result = "cutoff";
            else if (!isCycle(node)){
                ArrayList<Node> expandedNodes = expand(problem, node);
                frontier.addAll(expandedNodes);
            }
        }
        return result;
    }

    public static boolean isCycle (Node node){
        int[][] tempState = node.state.puzzle;
        while (node.parent != null){
            node = node.parent;
            if (equal(tempState, node.state.puzzle))
                return true;
        }
        return false;
    }
    private static boolean equal (int[][] arr1, int[][] arr2){
        for (int i = 0; i < arr1.length; i++){
            for (int j = 0; j < arr1.length; j++){
                if (arr1[i][j] != arr2[i][j])
                    return false;
            }
        }
        return true;
    }

    public static ArrayList<Node> expand (Problem problem, Node node) {
        ArrayList<Node> ret = new ArrayList<>();
        Puzzle nodePuzzle = node.state;
        Node nodeParentPuzzle = node.parent;
        int nodeAction = node.action;
        int nodePathCost = node.pathCost;
        Node tempNode = new Node(nodePuzzle, nodeParentPuzzle, nodeAction, nodePathCost);

        for (int i = 0; i < problem.actions(node).length; i++){
            if (problem.actions(node)[i]){

                Node result = problem.result(node, i);

                ret.add(result);
                tempNode = new Node(nodePuzzle, nodeParentPuzzle, nodeAction, nodePathCost);
            }
        }
        return ret;
    }
    private static String solution (Node node){
        String ret = "";
        while (node.parent != null){
            ret = node.action + ret;
            node = node.parent;
        }
        return ret;
    }

    public static void main(String[] args) {
        Puzzle puzzle = new Puzzle();
        System.out.println(puzzle);
        puzzle.shuffle(20);
        System.out.println(puzzle);
        Problem problem = new Problem(puzzle);
        System.out.println(iterativeDeepeningSearch(problem));
    }
}
