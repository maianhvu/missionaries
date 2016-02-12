import java.util.List;
import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

public class Missionaries {

    /**
     * Constants
     */
    private static final int COUNT_PEOPLE = 3;

    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RESET = "\u001B[0m";

    private static final String VISUAL_MISSIONARY = "M";
    private static final String VISUAL_CANNIBAL = "C";
    private static final String VISUAL_BOAT = "\\___/";
    private static final String VISUAL_SEA = "~~~~";
    private static final String VISUAL_LAND = "─";
    private static final String VISUAL_LEDGE_LEFT = "┐";
    private static final String VISUAL_LEDGE_RIGHT = "┌";
    private static final String VISUAL_AIR = " ";

    private static final String COLOR_MISSIONARY = ANSI_YELLOW;
    private static final String COLOR_CANNIBAL = ANSI_PURPLE;
    private static final int SIZE_SEA_AREA = VISUAL_BOAT.length() + VISUAL_SEA.length();
    private static final String OUTPUT_SEA = colorOutput(VISUAL_SEA, ANSI_BLUE);

    private static final int SIZE_BANK = COUNT_PEOPLE * 2 + 1;
    private static final String VISUAL_BANK_LEFT = repeatString(VISUAL_LAND, SIZE_BANK) + VISUAL_LEDGE_LEFT;
    private static final String VISUAL_BANK_RIGHT = VISUAL_LEDGE_RIGHT + repeatString(VISUAL_LAND, SIZE_BANK);
    private static final int SIZE_BANK_WITH_LEDGE_LEFT = SIZE_BANK + VISUAL_LEDGE_LEFT.length();
    private static final int SIZE_BANK_WITH_LEDGE_RIGHT = SIZE_BANK + VISUAL_LEDGE_RIGHT.length();

    public static void main(String[] args) {
        // Initialize requirements
        final State initialState = State.initialState();
        final State goalState = State.goalState();
        final Queue<State> frontier = new LinkedList<State>();
        final Set<State> explored = new HashSet<>();

        // Initialize frontier and explored set
        frontier.add(initialState);
        explored.add(initialState);

        // Start looping through frontier
        State currentState = null;
        while ((currentState = frontier.poll()) != null) {
            if (currentState.equals(goalState))
                break;

            // Get children states and explore
            List<State> nextStates = currentState.nextStates();

            for (State next : nextStates) {
                if (!next.isDesirable() || explored.contains(next))
                    continue;
                frontier.add(next);
                explored.add(next);
            }
        }

        // If currentState is null, there is no solution
        if (currentState == null) {
            System.out.println("No solution found.");
            return;
        }

        // Trace back the tree and print out result
        System.out.println(traceSolution(currentState));
    }

    /**
     * Recursively walk up the tree and find the solution path
     * @param state The state found from the solution
     * @return {String} The string describing the path to this node
     */
    private static String traceSolution(State state) {
        if (!state.hasParent()) return state.toString();
        StringBuilder sb = new StringBuilder();
        sb.append(traceSolution(state.getParentState()));
        sb.append(state.getFromAction().toString());
        sb.append(state.toString());
        return sb.toString();
    }

    private static void printFrontier(Queue<State> frontier) {
        LinkedList<State> states = (LinkedList<State>) frontier;
        StringBuilder sb = new StringBuilder();
        for (State s : states) {
            if (sb.length() > 0) sb.append("\t");
            sb.append(s.toString());
        }
        System.out.println(sb);
    }

    protected static class State {


        private int leftMCount_;
        private int leftCCount_;
        private boolean isBoatLeft_;
        private Integer hashCode_;

        private State parentState_;
        private Action fromAction_;

        private State(int leftMCount, int leftCCount, boolean isBoatLeft) {
            this.leftMCount_ = leftMCount;
            this.leftCCount_ = leftCCount;
            this.isBoatLeft_ = isBoatLeft;
        }

        /**
         * Initial state constructor
         */
        public static State initialState() {
            return new State(COUNT_PEOPLE, COUNT_PEOPLE, true);
        }

        public static State goalState() {
            return new State(0, 0, false);
        }

        public int getLeftMCount() { return this.leftMCount_; }
        public int getLeftCCount() { return this.leftCCount_; }
        public int getRightMCount() { return COUNT_PEOPLE - this.leftMCount_; }
        public int getRightCCount() { return COUNT_PEOPLE - this.leftCCount_; }

        public void setOrigin(State parent, Action fromAction) {
            this.parentState_ = parent;
            this.fromAction_ = fromAction;
        }
        public State getParentState() { return this.parentState_; }
        public Action getFromAction() { return this.fromAction_; }
        public boolean hasParent() { return this.parentState_ != null; }

        @Override
        public int hashCode() {
            if (this.hashCode_ != null) return this.hashCode_;
            Integer feed = new Integer(
                    this.leftMCount_ * 127 +
                    this.leftCCount_ * 31 +
                    (isBoatLeft_ ? 0 : 1) * 7);
            this.hashCode_ = feed.hashCode();
            return this.hashCode_;
        }

        @Override
        public boolean equals(Object another) {
            if (another == null) return false;
            if (this == another) return true;
            if (!(another instanceof State)) return false;
            State anotherState = (State) another;
            if ((this.leftMCount_ != anotherState.leftMCount_) ||
                    (this.leftCCount_ != anotherState.leftCCount_) ||
                    (this.isBoatLeft_ != anotherState.isBoatLeft_)) {
                return false;
                    }
            return true;
        }

        public List<State> nextStates() {
            List<State> states = new ArrayList<>();
            // Iterate through available actions and see if they are valid
            for (Action a : getAvailableActions()) {
                State newState = this.transition(a);
                if (newState == null) continue;

                // Set origin for tracing through tree
                newState.setOrigin(this, a);

                states.add(newState);
            }
            return states;
        }

        private boolean isValidState() {
            if (this.leftMCount_ < 0 || this.leftMCount_ > COUNT_PEOPLE) return false;
            if (this.leftCCount_ < 0 || this.leftCCount_ > COUNT_PEOPLE) return false;
            return true;
        }

        private static List<Action> getAvailableActions() {
            List<Action> actionsList = new ArrayList<>();
            actionsList.add(new Action(0, 0));
            actionsList.add(new Action(1, 0));
            actionsList.add(new Action(2, 0));
            actionsList.add(new Action(0, 1));
            actionsList.add(new Action(0, 2));
            actionsList.add(new Action(1, 1));
            return actionsList;
        }

        private State transition(Action a) {
            // If boat is left, then left modifier should be -1 so that
            // the number will get substracted
            int leftModifier = this.isBoatLeft_ ? -1 : 1;
            int newLeftMCount = this.leftMCount_ + leftModifier * a.getNumberMMoved();
            int newLeftCCount = this.leftCCount_ + leftModifier * a.getNumberCMoved();
            boolean newIsBoatLeft = !this.isBoatLeft_;
            State newState = new State(newLeftMCount, newLeftCCount, newIsBoatLeft);
            if (!newState.isValidState()) return null;
            return newState;
        }

        public boolean isDesirable() {
            if (this.getLeftMCount() > 0 && this.getLeftMCount() < this.getLeftCCount()) return false;
            if (this.getRightMCount() > 0 && this.getRightMCount() < this.getRightCCount()) return false;
            return true;
        }

        @Override
        public String toString() {

            // Left ledge
            String missionariesLeft = repeatString(VISUAL_MISSIONARY, this.getLeftMCount());
            String cannibalsLeft = repeatString(VISUAL_CANNIBAL, this.getLeftCCount());
            String missionariesLeftOutput = colorOutput(missionariesLeft, COLOR_MISSIONARY);
            String cannibalsLeftOutput = colorOutput(cannibalsLeft, COLOR_CANNIBAL);

            String leftPeople = repeatString(VISUAL_AIR, SIZE_BANK - missionariesLeft.length() - cannibalsLeft.length())
                + missionariesLeftOutput + cannibalsLeftOutput;

            // Sea
            String spaceBetween = repeatString(VISUAL_AIR,
                    SIZE_SEA_AREA + VISUAL_LEDGE_LEFT.length() + VISUAL_LEDGE_RIGHT.length());
            String seaOutput = "";
            if (this.isBoatLeft_) seaOutput += VISUAL_BOAT;
            seaOutput += OUTPUT_SEA;
            if (!this.isBoatLeft_) seaOutput += VISUAL_BOAT;

            // Right ledge
            String missionariesRight = repeatString(VISUAL_MISSIONARY, this.getRightMCount());
            String cannibalsRight = repeatString(VISUAL_CANNIBAL, this.getRightCCount());
            String missionariesRightOutput = colorOutput(missionariesRight, COLOR_MISSIONARY);
            String cannibalsRightOutput = colorOutput(cannibalsRight, COLOR_CANNIBAL);

            String rightPeople = missionariesRightOutput + cannibalsRightOutput +
                repeatString(VISUAL_AIR, SIZE_BANK - missionariesLeft.length() - cannibalsLeft.length());

            // Draw
            return String.format("%s%s%s\n%s%s%s\n",
                    leftPeople, spaceBetween,
                    rightPeople,
                    VISUAL_BANK_LEFT, seaOutput, VISUAL_BANK_RIGHT);
        }
    }

    protected static class Action {
        private int numberMMoved_;
        private int numberCMoved_;

        public Action(int numberMMoved, int numberCMoved) {
            this.numberMMoved_ = numberMMoved;
            this.numberCMoved_ = numberCMoved;
        }

        public int getNumberMMoved() { return this.numberMMoved_; }
        public int getNumberCMoved() { return this.numberCMoved_; }

        @Override
        public String toString() {
            if (this.numberMMoved_ == 0 && this.numberCMoved_ == 0) { return "\nMove empty boat.\n"; }
            return String.format("\nMove %s%d missionaries%s and %s%d cannibals%s.\n",
                    COLOR_MISSIONARY, this.numberMMoved_, ANSI_RESET,
                    COLOR_CANNIBAL, this.numberCMoved_, ANSI_RESET);
        }
    }

    private static String repeatString(String string, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) sb.append(string);
        return sb.toString();
    }

    private static String colorOutput(String string, String colorCode) {
        return String.format("%s%s%s", colorCode, string, ANSI_RESET);
    }
}
