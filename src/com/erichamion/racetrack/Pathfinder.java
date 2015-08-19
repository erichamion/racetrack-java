package com.erichamion.racetrack;

import java.nio.file.Path;
import java.util.*;

/**
 * Created by me on 8/17/15.
 */
public class Pathfinder {
    // Give impassable nodes a cost too high for anything else to match,
    // but not so high as to create any possibility of overflow.
    private static final double COST_IMPASSABLE = Double.MAX_VALUE / 1e6;
    private static final double COST_OPEN = 1.0;
    private static final double COST_NEAR_WALL = 2.0;
    private static final double COST_DIRECTION_CONSTANT = 0.001;

    private Queue<GridPoint> mPath = new LinkedList<>();



    public Pathfinder(final Track track, int playerIndex) {
        PathNode pathEnd = findBestPath(track, track.getPlayerPos(playerIndex));
        if (pathEnd != null) {
            //noinspection StatementWithEmptyBody
            while (smoothPath(track, pathEnd)) {}

            PathNode currentNode = pathEnd;
            while (currentNode != null) {
                mPath.add(currentNode.getPosition());
                currentNode = currentNode.getPrev();
            }
        }

    }

    private static PathNode findBestPath(final Track track, GridPoint start) {
        // Since we don't know where our goals are, use Dijkstra's algorithm.
        PriorityQueue<PathNode> frontier = new PriorityQueue<>(PathNode.costComparator);
        frontier.add(new PathNode(start, null, 0.0));
        Set<PathNode> visited = new TreeSet<>(PathNode.gridPointComparator);

        PathNode endNode = null;
        while (!frontier.isEmpty()) {
            PathNode currentNode = frontier.remove();

            // Fail when we run out of passable locations, or succeed when
            // we reach a finish line or run out of passable locations.
            if (currentNode.getTotalCost() >= COST_IMPASSABLE) {
                break;
            }
            Track.SpaceType spaceType = track.getSpace(currentNode.getPosition());
            if (spaceType == Track.SpaceType.FINISH_UP || spaceType == Track.SpaceType.FINISH_DOWN ||
                    spaceType == Track.SpaceType.FINISH_LEFT || spaceType == Track.SpaceType.FINISH_RIGHT) {
                endNode = currentNode;
                break;
            }

            List<PathNode> neighbors = getNeighbors(currentNode);
            for (PathNode neighbor : neighbors) {
                if (visited.contains(neighbor)) continue;

                PathNode realNeighborNode = Util.getObjectFromIterable(frontier, neighbor);
                boolean needsRemovedFromFrontier = true;
                if (realNeighborNode == null) {
                    realNeighborNode = neighbor;
                    needsRemovedFromFrontier = false;
                }
                double totalCostToNeighbor = getMoveCost(track, currentNode, realNeighborNode) + currentNode.getTotalCost();
                if (totalCostToNeighbor < realNeighborNode.getTotalCost()) {
                    if (needsRemovedFromFrontier) frontier.remove(realNeighborNode);

                    realNeighborNode.setPrev(currentNode);
                    realNeighborNode.setTotalCost(totalCostToNeighbor);
                    frontier.add(realNeighborNode);
                }
            }

            visited.add(currentNode);
        }

        return endNode;
    }

    private boolean smoothPath(Track track, PathNode pathEnd) {
        boolean madeChanges = false;
        PathNode middleNode = pathEnd.getPrev();
        if (middleNode == null) {
            // pathEnd is also the path start, can't change anything
            return madeChanges;
        }
        PathNode anchorNode = middleNode.getPrev();
        if (anchorNode == null) {
            // middleNode is the path start, can't remove it
            return madeChanges;
        }

        boolean hasLineOfSight = true;
        for (GridPoint point : track.getPath(pathEnd.getPosition(), anchorNode.getPosition())) {
            if (track.getSpace(point) == Track.SpaceType.WALL) {
                hasLineOfSight = false;
                break;
            }
        }

        if (hasLineOfSight) {
            pathEnd.setPrev(anchorNode);
            madeChanges = true;
        }

        return madeChanges || smoothPath(track, pathEnd.getPrev());
    }



    /**
     * Retrieves all 8 neighbors of a node. Does not check the neighboring
     * nodes for validity in any way.
     * @param centerNode The central node for which to get neighbors
     * @return A List containing 8 PathNodes, each representing a
     * neighbor (horizontal, vertical, or diagonal) of the original node.
     * The GridPoints may represent locations that are outside of the
     * track or otherwise invalid.
     */
    private static List<PathNode> getNeighbors(final PathNode centerNode) {
        List<PathNode> result = new ArrayList<>(8);

        GridPoint displacement = new GridPoint(-1, -1);
        for (; displacement.getRow() <= 1; displacement.setRow(displacement.getRow() + 1)) {
            for (displacement.setCol(-1); displacement.getCol() <= 1; displacement.setCol(displacement.getCol() + 1)) {
                if (displacement.getRow() == 0 && displacement.getCol() == 0) continue;

                GridPoint point = GridPoint.add(centerNode.getPosition(), displacement);
                result.add(new PathNode(point, centerNode, Double.MAX_VALUE));
            }
        }

        return result;
    }

    private static double getMoveCost(final Track track, final PathNode fromNode, final PathNode toNode) {
        switch (track.getSpace(toNode.getPosition())) {
            case WALL:
                return COST_IMPASSABLE;
            case TRACK:
                return getOpenSpaceMoveCost(track, fromNode, toNode);
            case FINISH_UP:
                return (GridPoint.subtract(toNode.getPosition(), fromNode.getPosition()).getRow() == -1) ?
                        getOpenSpaceMoveCost(track, fromNode, toNode) : COST_IMPASSABLE;
            case FINISH_DOWN:
                return (GridPoint.subtract(toNode.getPosition(), fromNode.getPosition()).getRow() == 1) ?
                        getOpenSpaceMoveCost(track, fromNode, toNode) : COST_IMPASSABLE;
            case FINISH_LEFT:
                return (GridPoint.subtract(toNode.getPosition(), fromNode.getPosition()).getCol() == -1) ?
                        getOpenSpaceMoveCost(track, fromNode, toNode) : COST_IMPASSABLE;
            case FINISH_RIGHT:
                return (GridPoint.subtract(toNode.getPosition(), fromNode.getPosition()).getCol() == 1) ?
                        getOpenSpaceMoveCost(track, fromNode, toNode) : COST_IMPASSABLE;
            default:
                // Should never happen
                return COST_IMPASSABLE;
        }
    }

    private static double getOpenSpaceMoveCost(Track track, PathNode fromNode, PathNode toNode) {
        // Prefer straight paths that don't hug the walls
        double baseCost = isNearWall(track, toNode) ? COST_NEAR_WALL : COST_OPEN;
        double directionPenalty = 0.0;
        if (fromNode.getPrev() != null) {
            GridPoint oldDirection =
                    GridPoint.subtract(fromNode.getPosition(), fromNode.getPrev().getPosition());
            GridPoint newDirection = GridPoint.subtract(toNode.getPosition(), fromNode.getPosition());
            directionPenalty = COST_DIRECTION_CONSTANT * (1 - unitDotProduct(oldDirection, newDirection));
        }
        return baseCost + directionPenalty;
    }

    private static boolean isNearWall(final Track track, final PathNode node) {
        List<PathNode> neighbors = getNeighbors(node);
        for (PathNode neighbor : neighbors) {
            if (track.getSpace(neighbor.getPosition()) == Track.SpaceType.WALL) {
                return true;
            }
        }
        return false;
    }

    private static int dotProduct(final GridPoint vectorA, final GridPoint vectorB) {
        return (vectorA.getRow() * vectorB.getRow()) + (vectorA.getCol() * vectorB.getCol());
    }

    private static double unitDotProduct(final GridPoint vectorA, final GridPoint vectorB) {
        int dot = dotProduct(vectorA, vectorB);
        double lengthASquared = Math.pow(vectorA.getRow(), 2) + Math.pow(vectorA.getCol(), 2);
        double lengthBSquared = Math.pow(vectorB.getRow(), 2) + Math.pow(vectorB.getCol(), 2);
        return dot / Math.sqrt(lengthASquared * lengthBSquared);
    }


    private static class PathNode {
        private GridPoint mPosition;
        private PathNode mPrev;
        private Double mTotalCost;

        /**
         * Compares nodes based on the total cost from the starting point
         * to each node. Note that this Comparator is NOT consistent with
         * PathNode#equals.
         */
        public static final Comparator<PathNode> costComparator = new Comparator<PathNode>() {
            @Override
            public int compare(PathNode o1, PathNode o2) {
                return o1.getTotalCost().compareTo(o2.getTotalCost());
            }
        };

        /**
         * Compares nodes based on their location on the grid.
         */
        public static final Comparator<PathNode> gridPointComparator = new Comparator<PathNode>() {
            @Override
            public int compare(PathNode o1, PathNode o2) {
                // I don't care too much about what the actual order is,
                // as long as it is well defined.
                // Directly access the mPosition members to avoid creating
                // new copies.
                int result = o1.mPosition.getRow() - o2.mPosition.getRow();
                if (result == 0) {
                    result = o1.mPosition.getCol() - o2.mPosition.getCol();
                }
                return result;
            }
        };

        public PathNode() { }

        public PathNode(GridPoint pos, PathNode prev, double totalCost) {
            mPosition = new GridPoint(pos);
            mPrev = prev;
            mTotalCost = totalCost;
        }

        public Double getTotalCost() {
            return mTotalCost;
        }

        public void setTotalCost(double totalCost) {
            mTotalCost = totalCost;
        }

        public GridPoint getPosition() {
            return mPosition;
        }

        public PathNode getPrev() {
            return mPrev;
        }

        public void setPrev(PathNode prev) {
            mPrev = prev;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PathNode) {
                PathNode secondNode = (PathNode) obj;
                return gridPointComparator.compare(this, secondNode) == 0;
            } else if (obj instanceof GridPoint) {
                GridPoint point = (GridPoint) obj;
                return this.mPosition.equals(point);
            } else {
                throw new ClassCastException();
            }
        }

        @Override
        public String toString() {
            return "Location: " + mPosition.toString() + "; Cost: " + mTotalCost.toString();
        }
    }
}
