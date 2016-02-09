package uk.ac.cam.echo2016.multinarrative;

import java.util.ArrayList;

import android.os.BaseBundle;

/**
 * The EditableNarrative that is edited using the GUI.
 * 
 * <p>
 * ALT: The {@code MultiNarrative} graph structure used by the {@code FXMLGUI} editor to store the game design. This
 * graph is generated alongside the editor and used to build the template when the design is finished. New {@code Node}s
 * and {@code Route}s can be added 
 * 
 * 
 * @author tr393
 * @author eyx20
 * @version 1.0
 * @throws NonUniqueIdException
 * @see MultiNarrative
 */
public class GUINarrative extends EditableNarrative { // TODO Finish Documentation
    private static final long serialVersionUID = 1;

    /**
     * Adds a route with ID {@code id} to the graph, connecting the node with ID {@code startId} to the route with
     * ID {@code endId}.
     * 
     * @param id
     * @param startId
     * @param endId
     * @throws NonUniqueIdException
     * @throws GraphElementNotFoundException
     */
    public void newRoute(String id, String startId, String endId)
            throws NonUniqueIdException, GraphElementNotFoundException {
        if (isUniqueId(id)) {
            Node startNode = getNode(startId);
            if (startNode == null)
                throw new GraphElementNotFoundException("Node with id: " + startId + " not found");
            Node endNode = getNode(endId);
            if (endNode == null)
                throw new GraphElementNotFoundException("Node with id: " + endId + " not found");

            Route route = new Route(id, startNode, endNode);
            // Updates references of graph and nodes
            route.setup();
            addRoute(route);
        } else {
            throw new NonUniqueIdException("Invalid id: " + id + " is not unique.");
        }
    }

    public void newSynchronizationNode(String id) throws NonUniqueIdException {
        if (isUniqueId(id))
            nodes.put(id, new SynchronizationNode(id));
        else
            throw new NonUniqueIdException("Invalid id: " + id + " is not unique.");
    }

    public void newChoiceNode(String id) throws NonUniqueIdException {
        if (isUniqueId(id))
            nodes.put(id, new ChoiceNode(id));
        else
            throw new NonUniqueIdException("Invalid id: " + id + " is not unique.");
    }

    /**
     * Takes the route with ID {@code routeId} and splits it in two, where the divisor is a new {@code ChoiceNode} with
     * ID {@code newChoiceId}. Here, the original route is preserved between its start and the new node.
     * 
     * <pre>
     * Before:
     *           start
     *             |
     *             | routeId
     *             |
     *            end  
     *
     * After:
     *           start
     *             |
     *             | newRouteId1 (=routeId)
     *             |
     *        newChoiceId
     *             |
     *             | newRouteId2
     *             |
     *            end
     * </pre>
     * 
     * @see insertChoiceOnRoute
     * @param routeId
     * @param newChoiceId
     * @param newRouteId
     * @throws NonUniqueIdException
     * @throws GraphElementNotFoundException
     */
    public void insertChoiceOnRoute(String routeId, String newChoiceId, String newRouteId) 
    		throws NonUniqueIdException, GraphElementNotFoundException {
        
        if (!isUniqueId(newChoiceId) || !isUniqueId(newRouteId)) {
            throw new NonUniqueIdException(
                    "Error: Invalid id: " + (isUniqueId(newChoiceId) ? newChoiceId : newRouteId) + " is not unique.");
        }
        if(newChoiceId.equals(newRouteId)) {
            throw new NonUniqueIdException("Error: Arguments not unique");
        }
        Route route1 = getRoute(routeId);
        if (route1 == null)
            throw new GraphElementNotFoundException("Error: Route with id: " + routeId + " not found");

        ChoiceNode choice = new ChoiceNode(newChoiceId);
        // Connect route2 start and end
        Route route2 = new Route(newRouteId, choice, route1.getEnd());
        route2.setup();
        route2.getEnd().getEntering().remove(route1);
        
        // route1.start and start.exitRoutes already correct
        route1.setEnd(choice);
        choice.getEntering().add(route1);
        
        routes.put(route2.getId(), route2);
        nodes.put(choice.getId(), choice);
    }

    /**
     * Takes the route with ID {@code routeId} and splits it in two, where the divisor is a new 
     * {@code ChoiceNode} with ID {@code newChoiceId}. Here, the original route is discarded.
     * 
     * <pre>
     *
     * Before:
     *           start
     *             |
     *             | routeId
     *             |
     *            end  
     *         
     * After:
     *           start
     *             |
     *             | newRouteId1
     *             |    
     *        newChoiceId  
     *             |
     *             | newRouteId2
     *             |
     *            end 
     * </pre>
     *
     * 
     * @see insertChoiceOnRoute
     * @param routeId
     * @param newChoiceId
     * @param newRouteId1
     * @param newRouteId2
     * @throws NonUniqueIdException
     * @throws GraphElementNotFoundException
     */
    public void insertChoiceOnRoute(String routeId, String newChoiceId, String newRouteId1, String newRouteId2)
            throws NonUniqueIdException, GraphElementNotFoundException {
        
        if (!isUniqueId(newChoiceId) || !isUniqueId(newRouteId1) || !isUniqueId(newRouteId2)) {
            throw new NonUniqueIdException("Error: Invalid id: "
                    + (isUniqueId(newChoiceId) ? (isUniqueId(newRouteId1) ? newRouteId2 : newRouteId1) : newChoiceId)
                    + " is not unique.");
        }
        if(newChoiceId.equals(newRouteId1) || newChoiceId.equals(newRouteId2) || newRouteId1.equals(newRouteId2)) {
            throw new NonUniqueIdException("Error: Arguments are not unique");
        }

        Route route = getRoute(routeId);
        if (route == null)
            throw new GraphElementNotFoundException("Error: Route with id: " + routeId + " not found");

        ChoiceNode choice = new ChoiceNode(newChoiceId);
        
        Node start = route.getStart();
        Node end = route.getEnd();
        
        // Connect route1
        Route route1 = new Route(newRouteId1, start, choice);
        route1.setup();
        start.getExiting().remove(route);
        
        // Connect route2
        Route route2 = new Route(newRouteId2, choice, end);
        route2.setup();
        end.getEntering().remove(route);
        
        // Update GuiNarrative references
        routes.remove(route.getId());
        routes.put(route1.getId(), route1);
        routes.put(route2.getId(), route2);
        nodes.put(choice.getId(), choice);
    }

    private boolean isUniqueId(String id) {
        Route route = getRoute(id);
        Node node = getNode(id);
        return ((route == null) && (node == null));
    }

    public boolean setStartPoint(String id) throws GraphElementNotFoundException {
        Node node = getNode(id);
        if (node == null) throw new GraphElementNotFoundException("Error: Node with id: " + id + " not found");
        if (node instanceof SynchronizationNode) {
            start = (SynchronizationNode) node;
            return true;
        } else {
            return false;
        }
    }
    
    public void addPrimaryProperty(String routeId, String property) throws GraphElementNotFoundException { // TODO add to tests
        Route route = getRoute(routeId);
        if (route == null) throw new GraphElementNotFoundException("Error: Node with id: " + routeId + " not found");
        route.createProperties();
        ArrayList<String> primaries = route.getProperties().getStringArrayList("Primaries");
        if (primaries == null) primaries = new ArrayList<String>();
        primaries.add(property);
    }
    public boolean removePrimaryProperty(String routeId, String property) throws GraphElementNotFoundException { // TODO add to tests
        Route route = getRoute(routeId);
        if (route == null) throw new GraphElementNotFoundException("Error: Node with id: " + routeId + " not found");
        BaseBundle prop = route.getProperties();
        if (prop == null) return false;
        ArrayList<String> primaries = route.getProperties().getStringArrayList("Primaries");
        if (primaries == null) return false;
        return primaries.remove(property);
    }

    public BaseBundle getProperties(String id) throws GraphElementNotFoundException {
        Route route = getRoute(id);
        if (route != null) {
            return route.getProperties();
        } else {
            Node node = getNode(id);
            if (node != null) {
                return node.getProperties();
            }
        }
        throw new GraphElementNotFoundException("Error: Element with id: " + id + " not found");
    }
}
