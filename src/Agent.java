/*********************************************
 *  Agent.java 
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2017
*/

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.Point;

public class Agent {
    
    private enum Dir { NORTH,EAST,SOUTH,WEST };

    private static final int BUF_SIZE = 25;
    public static char[][] map;
    private static int x;
    private static int y;
    private static Dir dir;
    private static HashSet<Character> obstacles;
    private static HashSet<Character> normal;
    private static HashSet<Character> goals;
    private static HashSet<Point> seen;
    private static HashSet<Point> explored;
    private static Hashtable<Character, Integer> assets;

    public char get_action( char view[][] ) {
    	explored.add(new Point(x,y));
    	System.out.println(x + " " + y);
        
        // add new view to map if not seen before
        System.out.println(dir);
        if (dir == Dir.WEST || dir == Dir.EAST || dir == Dir.SOUTH) rotateMatrix(view);
        if (dir == Dir.SOUTH || dir == Dir.EAST) rotateMatrix(view);
        if (dir == Dir.EAST) rotateMatrix(view);
        
        for (int j = -2; j < 3; j++) {
            for (int i = -2; i < 3; i++) {
            	Point p = new Point(x+i, y+i);
            	if (seen.contains(p)) continue;
            	seen.add(p);
                map[y+j][x+i] = view[j+2][i+2];
            }
        }
        map[y][x] = 'O';
        
        // print out whole map
        for (int i = 0; i < BUF_SIZE; i++) {
            for (int j = 0; j < BUF_SIZE; j++) {
                char c = map[i][j];
                String s = "";
                if (c == '?') s = String.format("%6s", j +","+ i + " ");
                else s = String.format("%6s", "[ "+c+"] ");
                System.out.print(s);
            }
            System.out.println();
        }

        ArrayList<Point> p = null;
        String steps;
        char[] idk;
        Queue<Character> q = null;

        ArrayList<Point> g = findGoals();
        if (!g.isEmpty()) {
            p = pathToGoal(g.get(0));
            if (p != null) {
                System.out.println("p.x: " + p.get(0).x);
                System.out.println("p.y: " + p.get(0).y);
                updateAssets(p.get(1));
                steps = steps(p);
                idk = steps.toCharArray();

                q = new LinkedList<Character>();
                for (int i = 0; i != steps.length(); i++) {
                    Character c = new Character(idk[i]);
                    q.add(c);
                }
            }
        }

        System.out.println("assets: " + assets);

        int ch=0;

        System.out.print("Enter Action(s): ");

        try {
            while ( ch != -1 ) {
                // read character from keyboard
                if (p != null) {
                    ch = q.poll();
                } else {
                    ch  = System.in.read();
                    ch = Character.toUpperCase(ch);
                }
                switch( ch ) { // if character is a valid action, return it
                case 'F': 
                    switch(dir) {
                    case NORTH:
                        y--;
                        break;  
                    case SOUTH:
                        y++;
                        break;
                    case WEST:
                        x--;
                        break;
                    case EAST:
                        x++;
                        break;
                    }
                    break;
                case 'L':
                    dir = Dir.values()[(dir.ordinal()+3)%4];
                    break;
                case 'R': 
                    dir = Dir.values()[(dir.ordinal()+5)%4];
                    break;
                case 'C': 
                    break;
                case 'U': 
                    break;
                case 'B':
                    break;
                case '?':

                    
                }
                return (char)ch;
            }
        }
        catch (IOException e) {
            System.out.println ("IO error:" + e );
        }

        return 0;
    }

    void print_view( char view[][] )
    {
        int i,j;

        System.out.println("\n+-----+");
        for( i=0; i < 5; i++ ) {
            System.out.print("|");
            for( j=0; j < 5; j++ ) {
                if(( i == 2 )&&( j == 2 )) {
                    System.out.print('^');
                }
                else {
                    System.out.print( view[i][j] );
                }
            }
            System.out.println("|");
        }
        System.out.println("+-----+");
    }

    public static void main( String[] args )
    {
        InputStream in  = null;
        OutputStream out= null;
        Socket socket    = null;
        Agent  agent     = new Agent();
        char    view[][] = new char[5][5];
        char    action    = 'F';
        int port;
        int ch;
        int i,j;

        if( args.length < 2 ) {
            System.out.println("Usage: java Agent -p <port>\n");
            System.exit(-1);
        }

        port = Integer.parseInt( args[1] );

        try { // open socket to Game Engine
            socket = new Socket( "localhost", port );
            in  = socket.getInputStream();
            out = socket.getOutputStream();
        }
        catch( IOException e ) {
            System.out.println("Could not bind to port: "+port);
            System.exit(-1);
        }
        
        // initialize agent's map
        map = new char[BUF_SIZE][BUF_SIZE];
        for (char[] row : map) Arrays.fill(row, '?');
        x = BUF_SIZE/2;
        y = BUF_SIZE/2;
        dir = Dir.SOUTH;
        
        seen = new HashSet<Point>();
        obstacles = new HashSet<Character>();
        normal = new HashSet<Character>();
        goals = new HashSet<Character>();
        assets = new Hashtable<Character, Integer>();
        explored = new HashSet<Point>();
        
        obstacles.add('*');
        obstacles.add('~'); 
        obstacles.add('-');
        obstacles.add('T');
        obstacles.add('?');
        normal.add(' ');
        goals.add('a');
        goals.add('k');
        goals.add('d');
        goals.add('$');
        assets.put('a', 0);
        assets.put('k', 0);
        assets.put('d', 0);
        assets.put('$', 0);

        try { // scan 5-by-5 window around current location
            while( true ) {
                for( i=0; i < 5; i++ ) {
                    for( j=0; j < 5; j++ ) {
                        if( !(( i == 2 )&&( j == 2 ))) {
                            ch = in.read();
                            if( ch == -1 ) {
                                System.exit(-1);
                            }
                            view[i][j] = (char) ch;
                        }
                    }
                }
                agent.print_view( view ); // COMMENT THIS OUT BEFORE SUBMISSION

                action = agent.get_action( view );
                out.write( action );
            }
        }
        catch( IOException e ) {
            System.out.println("Lost connection to port: "+ port );
            System.exit(-1);
        }
        finally {
            try {
                socket.close();
            }
            catch( IOException e ) {}
        }
    }

    private static void rotateMatrix(char[][] matrix) {
        if(matrix.length == 0 || matrix.length != matrix[0].length) return;
        for (int layer = 0; layer < matrix.length/2; layer++) {
            char[] buf = new char[matrix.length-layer*2-1];
            
            if (buf.length == 0) break;
            
            for (int i = 0; i < buf.length; i++) {
                buf[i] = matrix[layer+i][layer];
            }

            // Rotate Left to Top
            for (int i = 0; i < buf.length; i++) {
                int j = buf.length-i;
                matrix[i+layer][layer] = matrix[layer][j+layer];
            }
            // Rotate Bottom to Left
            for (int i = 0; i < buf.length; i++) {
                int j = matrix.length - i - 1;
                matrix[layer][j-layer] = matrix[j-layer][matrix.length-layer-1];
            }

            // Rotate Right to Bottom
            for (int i = 0; i < buf.length; i++) {
                int j = matrix.length - i - 1;
                matrix[j-layer][matrix.length-layer-1] = matrix[matrix.length-layer-1][i+layer];
            }
            // Rotate Top (Buffered) to Right
            for (int i = 0; i < buf.length; i++) {
                matrix[matrix.length-layer-1][i+layer] = buf[i];
            }
        }
    }

    public void updateAssets(Point p) {
        Character c = map[p.y][p.x];
        if (c.equals('a') || c.equals('d') || c.equals('k')) {
            assets.put(c, assets.get(c)+1);
        }
    }

    private ArrayList<Point> pathToGoal(Point goal) {
        Comparator<State> c = new CompareDistance();
        PriorityQueue<State> pq = new PriorityQueue<State>(c);
        HashSet<Point> searched = new HashSet<Point>();
        State s = new State(new Point(x,y), goal);
        State endState = s;
        pq.add(s);
        while (pq.size() != 0) {
            State st = pq.poll();
            if (searched.contains(st.getCurrPoint())) continue;
            if (st.getCurrPoint().equals(goal)) {
                endState = st;
                searched.clear();
                break;
            }
            searched.add(st.getCurrPoint());
            addToPQ(pq,st, goal);
        }
        if (pq.size() == 0) return null;
        
        ArrayList<Point> path = new ArrayList<>();
        path.add(endState.getCurrPoint());
        while (endState.getPrevious() != null) {
            path.add(0, endState.getPrevious().getCurrPoint());
            endState = endState.getPrevious();
        }
        
        // printing
        System.out.println("------------");
        for (int i = 0; i != path.size(); i++) {
            Point p = path.get(i);
            System.out.println("[" + p.x + "," + p.y + "]");
        }
        return path;
    }

    private ArrayList<Point> getNeighbouringPoints(Point p) {
        ArrayList<Point> points = new ArrayList<Point>();
        Point p1 = new Point(p.x, p.y+1);
        if (!obstacles.contains(map[p1.y][p1.x])) {
            // System.out.println("DOWN: [" + p1.y + "," + p1.x + "]: '" + map[p1.y][p1.x] + "'");
            points.add(p1);
        }
        Point p2 = new Point(p.x, p.y-1);
        if (!obstacles.contains(map[p2.y][p2.x])) {
            // System.out.println("UP: [" + p2.y + "," + p2.x + "]: '" + map[p2.y][p2.x] + "'");
            points.add(p2);
        }
        Point p3 = new Point(p.x+1, p.y);
        if (!obstacles.contains(map[p3.y][p3.x])) {
            // System.out.println("RIGHT: [" + p3.y + "," + p3.x + "]: '" + map[p3.y][p3.x] + "'");
            points.add(p3);
        }
        Point p4 = new Point(p.x-1, p.y);
        if (!obstacles.contains(map[p4.y][p4.x])) {
            // System.out.println("LEFT: [" + p4.y + "," + p4.x + "]: '" + map[p4.y][p4.x] + "'");
            points.add(p4);
        }
        return points;
    }

    private void addToPQ(PriorityQueue<State> pq, State st, Point goal) {
        Point p = st.getCurrPoint();
        ArrayList<Point> points = getNeighbouringPoints(p);
        for (int i = 0; i != points.size(); i++) {
            State s = new State(points.get(i), goal);
            s.setPrevious(st);
            pq.add(s);
        }
    }

    public ArrayList<Point> findGoals() {
        ArrayList<Point> goalPoints = new ArrayList<Point>();
        for (int i = 0; i < BUF_SIZE; i++) {
            for (int j = 0; j < BUF_SIZE; j++) {
                if (goals.contains(map[j][i])) goalPoints.add(new Point(i,j));
            }
        }
        // printing
        if (!goalPoints.isEmpty()) {
            System.out.println("Goal point(s): ");
        }
        for (int i = 0; i != goalPoints.size(); i++) {
            Point p = goalPoints.get(i);
            System.out.println("[" + p.x + "," + p.y + "] ");

        }
        // end printing
        return goalPoints;
    }
    
    public ArrayList<Point> getExplorationPoints() {
    	ArrayList<Point> explorationPoints = new ArrayList<Point>();
    	for (int i = 0; i < BUF_SIZE; i++) {
    		for (int j = 0; j < BUF_SIZE; j++) {
    			Point p = new Point(j,i);
    			if (!explored.contains(p)) {
    				explorationPoints.add(p);
    			}
    		}
    	}
    	return explorationPoints;
    }
    
    public String steps(ArrayList<Point> path) {
        StringBuilder sb = new StringBuilder();
        Dir currDir = dir;

        while (path.size() != 1) {
            Point prev = path.get(0);
            Point curr = path.get(1);
            path.remove(0);
            int dx = (curr.x - prev.x);
            int dy = (curr.y - prev.y);

            Dir newDir;
            if (dx == 1 && dy == 0) newDir = Dir.EAST;
            else if (dx == -1 && dy == 0) newDir = Dir.WEST;
            else if (dx == 0 && dy == -1) newDir = Dir.NORTH;
            else newDir = Dir.SOUTH;
            
            while (newDir != currDir) {
            	sb.append("L");
            	System.out.println(sb.toString());
            	System.out.println(currDir);
            	currDir = Dir.values()[(currDir.ordinal()+3)%4];
            }

            char destTile = map[curr.y][curr.x];
            switch (destTile) {
            case '~':
                System.out.println("Swimming");
                break;
            case '*':
                sb.append('B');
                System.out.println("Blew Up a Wall");
                break;
            case '-':
                sb.append('U');
                System.out.println("Unlocked Door");
                break;
            case 'T':
                if (!obstacles.contains('T')) sb.append('C');
                else sb.append('B');
                System.out.println("Killed a Tree");
                break;
            case 'a':
                System.out.println("Got Axe");
                break;
            case 'k':
                System.out.println("Got Key");
                break;
            case 'd':
                System.out.println("Got Dynamite");
                break;
            case '$':
                System.out.println("Got Treasure");
                break;
            }
            sb.append("F");
        }
        return sb.toString().replaceAll("LLL", "R");
    }

    private class State {
        private Point currPoint;
        private State previous;
        private int Heuristic;

        public State(Point currPoint, Point goal) {
            this.currPoint = currPoint;
            this.previous = null;
            this.Heuristic = calcHeuristic(currPoint, goal);
        }

        public Point getCurrPoint() {
            return this.currPoint;
        }

        public void setPrevious(State previous) {
            this.previous = previous;
        }

        public State getPrevious() {
            return this.previous;
        }

        public void setHeuristic(int Heuristic) {
            this.Heuristic = Heuristic;
        }

        public int getHeuristic() {
            return this.Heuristic;
        }

        private int calcHeuristic(Point currPoint, Point goal) {
            return Math.abs(goal.x-currPoint.x) + Math.abs(goal.y-currPoint.y);
        }
    }

    private class CompareDistance implements Comparator<State> {
        @Override
        public int compare(State o1, State o2) {
            if (o1.getHeuristic() < o2.getHeuristic()) {
                return   -1;
            } else if (o1.getHeuristic() > o2.getHeuristic()) {
                return 1;
            } else {
                return 0;
            }
        }

    }
}