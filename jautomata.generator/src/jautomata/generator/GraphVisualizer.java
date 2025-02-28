package jautomata.generator;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.Set;


/**
 * @author Bastian Polewka
 * @summary class to visualize generated weighted automata
*/
public class GraphVisualizer {
	
	private static int width;
	private static int height;
	private static Graph<State, DefaultWeightedEdge> graph;
	private static Map<State, Point> vertexPositions;
	private static JFrame frame;
	
	public GraphVisualizer() {
		GraphVisualizer.width = 800;
		GraphVisualizer.height = 600;
		
        frame = new JFrame("WeightedAutomaton");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
	}

    
    public void updateGraph(Graph<State, DefaultWeightedEdge> newGraph, WeightedAutomaton weightedAutomaton) {
        graph = newGraph;
        
        vertexPositions.clear();
        layoutGraph(graph, vertexPositions);

        JFrame frame = new JFrame(weightedAutomaton.getName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);

        GraphPanel panel = new GraphPanel(graph, weightedAutomaton, vertexPositions);
        frame.add(panel);
        frame.setVisible(true);
    }

    
    private static Graph<State, DefaultWeightedEdge> createGraph(WeightedAutomaton weightedAutomaton) {
        Graph<State, DefaultWeightedEdge> graph = new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        Set<State> states = weightedAutomaton.getStates();
        Set<Transition> transitions = weightedAutomaton.getTransitions();

        for (State state : states) {
            graph.addVertex(state);
        }

        for (Transition transition : transitions) {
            graph.addEdge(transition.getFromState(), transition.getToState());
        }

        return graph;
    }

    
    public void visualizeGraph(WeightedAutomaton weightedAutomaton) {
    	Graph<State, DefaultWeightedEdge> graph = createGraph(weightedAutomaton);
    	Component[] components = frame.getContentPane().getComponents();

        if (components.length > 0) {
            JPanel lastAddedPanel = (JPanel) components[components.length - 1];

            frame.getContentPane().remove(lastAddedPanel);
            frame.revalidate();
            frame.repaint();
        }
    	  	
        vertexPositions = new HashMap<>();
        layoutGraph(graph, vertexPositions);
        
        GraphPanel panel = new GraphPanel(graph, weightedAutomaton, vertexPositions);
        frame.add(panel);
        frame.setVisible(true);
    }

    
    private static void layoutGraph(Graph<State, DefaultWeightedEdge> graph, Map<State, Point> vertexPositions) {
        int width = 800;
        int height = 600;
        int radius = 200;

        Set<State> vertices = graph.vertexSet();
        int numVertices = vertices.size();
        double angleStep = 2 * Math.PI / numVertices;

        int i = 0;
        for (State vertex : vertices) {
            double angle = i * angleStep;
            int x = (int) (width / 2 + radius * Math.cos(angle));
            int y = (int) (height / 2 + radius * Math.sin(angle));
            vertexPositions.put(vertex, new Point(x, y));
            i++;
        }
    }

    
    @SuppressWarnings("serial")
    static class GraphPanel extends JPanel {
        private final Graph<State, DefaultWeightedEdge> graph;
        private final WeightedAutomaton weightedAutomaton;
        private final Map<State, Point> vertexPositions;

        private State selectedVertex = null;
        private Point dragOffset = null;

        public GraphPanel(Graph<State, DefaultWeightedEdge> graph, WeightedAutomaton weightedAutomaton, Map<State, Point> vertexPositions) {
            this.graph = graph;
            this.weightedAutomaton = weightedAutomaton;
            this.vertexPositions = vertexPositions;           
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    for (Map.Entry<State, Point> entry : vertexPositions.entrySet()) {
                        State vertex = entry.getKey();
                        Point position = entry.getValue();
                        if (position.distance(e.getPoint()) < 15) {
                            selectedVertex = vertex;
                            dragOffset = new Point(e.getX() - position.x, e.getY() - position.y);
                            break;
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    selectedVertex = null;
                    dragOffset = null;
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (selectedVertex != null) {
                        Point newPosition = new Point(e.getX() - dragOffset.x, e.getY() - dragOffset.y);
                        vertexPositions.put(selectedVertex, newPosition);
                        repaint();
                    }
                }
            });
        }

        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw edges
            for (DefaultWeightedEdge edge : graph.edgeSet()) {
                State fromVertex = graph.getEdgeSource(edge);
                State toVertex = graph.getEdgeTarget(edge);

                Point fromPosition = vertexPositions.get(fromVertex);
                Point toPosition = vertexPositions.get(toVertex);

                g2d.setColor(Color.BLACK);

                if (fromVertex.equals(toVertex)) {
                    int loopRadius = 30; 
                    int loopOffsetX = 20;
                    int loopOffsetY = 20;

                    int loopX = fromPosition.x - loopOffsetX;
                    int loopY = fromPosition.y - loopOffsetY;

                    g2d.drawArc(loopX - loopRadius, loopY - loopRadius, 
                                2 * loopRadius, 2 * loopRadius, 
                                0, 360);

                    // Draw transition labels for the self-loop
                    Transition transition = weightedAutomaton.getTransition(fromVertex, toVertex);
                    if (transition != null) {
                        String labelAction = transition.getAction();
                        String labelWeight = transition.getWeightAsString();
                        g2d.drawString(labelAction, loopX - loopRadius - 10, loopY - loopRadius - 10);
                        g2d.drawString(labelWeight, loopX - loopRadius - 10, loopY - loopRadius + 5);
                    }
                } else {
                    drawArrow(g2d, fromPosition, toPosition);

                    // Draw transition labels
                    Transition transition = weightedAutomaton.getTransition(fromVertex, toVertex);
                    if (transition != null) {
                        String labelAction = transition.getAction();
                        String labelWeight = transition.getWeightAsString();
                        int midX = (fromPosition.x + toPosition.x) / 2;
                        int midY = (fromPosition.y + toPosition.y) / 2;
                        g2d.drawString(labelAction, midX, midY);
                        g2d.drawString(labelWeight, midX, midY + 15);
                    }
                }
            }

            // Draw vertices
            for (State vertex : graph.vertexSet()) {
                Point position = vertexPositions.get(vertex);

                // Determine color of vertex
                if (weightedAutomaton.getInitialState().equals(vertex)) {
                    g2d.setColor(Color.GREEN);
                } else if (weightedAutomaton.getFinalState().equals(vertex)) {
                    g2d.setColor(Color.RED);
                } else {
                    g2d.setColor(Color.WHITE);
                }

                // Draw vertex circle
                g2d.fillOval(position.x - 15, position.y - 15, 30, 30);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(position.x - 15, position.y - 15, 30, 30);

                // Draw vertex label
                g2d.drawString(vertex.getId(), position.x - 10, position.y + 5);
            }
        }

        
        private void drawArrow(Graphics2D g2d, Point from, Point to) {
            int dx = to.x - from.x;
            int dy = to.y - from.y;
            double angle = Math.atan2(dy, dx);

            g2d.drawLine(from.x, from.y, to.x, to.y);

            int arrowHeadLength = 15; 
            int arrowRadius = 15; // distance from the target vertex

            int arrowX = to.x - (int) (arrowRadius * Math.cos(angle));
            int arrowY = to.y - (int) (arrowRadius * Math.sin(angle));

            int x1 = arrowX - (int) (arrowHeadLength * Math.cos(angle - Math.PI / 6));
            int y1 = arrowY - (int) (arrowHeadLength * Math.sin(angle - Math.PI / 6));
            int x2 = arrowX - (int) (arrowHeadLength * Math.cos(angle + Math.PI / 6));
            int y2 = arrowY - (int) (arrowHeadLength * Math.sin(angle + Math.PI / 6));

            g2d.fillPolygon(new int[]{arrowX, x1, x2}, new int[]{arrowY, y1, y2}, 3);
        }
    }
}
