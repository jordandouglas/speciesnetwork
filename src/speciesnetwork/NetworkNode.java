package speciesnetwork;

import java.text.DecimalFormat;
import java.util.*;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import beast.core.Description;
import beast.evolution.tree.Node;

/**
 * NetworkNode is equivalent to Node but can have 2 parents or 2 children.
 * @author Chi Zhang
 * @author Huw Ogilvie
 */

@Description("Network node for binary rooted network")
public class NetworkNode {
    /**
     * the taxonomic name of this node
     */
    protected String label;

    /**
     * inheritance probability associated with the left parent branch
     */
    protected double inheritProb;

    /**
     * height of this node
     */
    protected double height;

    /**
     * children and parents of this node
     */
    public List<Integer> childBranchNumbers;
    public int gammaBranchNumber;
    protected Multiset<NetworkNode> children;
    protected Multiset<NetworkNode> parents;

    /**
     * counts of children and parents of this node
     */
    protected int nodeNumber;
    protected int nParents;
    protected int nChildren;

    /**
     * status of this node after an operation is performed on the state
     */
    int isDirty;

    private DecimalFormat df;

    protected void updateRelationships() {
        nodeNumber = -1;
        for (int i = 0; i < network.nodes.length; i++) {
            if (network.nodes[i] == this) {
                nodeNumber = i;
                break;
            }
        }

        if (nodeNumber < 0) {
            throw new RuntimeException("Node is not attached to the network!");
        }

        gammaBranchNumber = network.getBranchNumber(nodeNumber);
        parents = updateParents();
        children = updateChildren();
        nParents = parents.size();
        nChildren = children.size();
    }

    /**
     * meta-data contained in square brackets in Newick
     */
    protected String metaDataString;

    /**
     * arbitrarily labeled metadata on this node
     */
    protected Map<String, Object> metaData = new TreeMap<>();

    /**
     * the network that this node is a part of
     */
    protected Network network;

    public NetworkNode() {
        initProps();
    }

    public NetworkNode(Network n) {
        initProps();
        network = n;
    }

    private void initProps() {
        label = null;
        inheritProb = 0.5;
        height = 0.0;
        childBranchNumbers = new ArrayList<>();
        children = HashMultiset.create();
        parents = HashMultiset.create();
        nodeNumber = -1;
        nParents = 0;
        nChildren = 0;
        isDirty = Network.IS_DIRTY;
        df = new DecimalFormat("0.########");
    }

    /* instantiate a new network node with the same height, labels and metadata as a tree node
       this does not copy the parents or children */
    public NetworkNode(Node treeNode) {
        height = treeNode.getHeight();
        label = treeNode.getID();
        metaDataString = treeNode.metaDataString;
        for (String metaDataKey: treeNode.getMetaDataNames()) {
            Object metaDataValue = treeNode.getMetaData(metaDataKey);
            metaData.put(metaDataKey, metaDataValue);
        }
    }

    protected void copyTo(NetworkNode dst) {
        copyNode(this, dst);
    }

    protected void copyFrom(NetworkNode src) {
        copyNode(src, this);
    }

    protected static void copyNode(NetworkNode src, NetworkNode dst) {
        dst.label = src.label;
        dst.inheritProb = src.inheritProb;
        dst.height = src.height;
        dst.childBranchNumbers.clear();
        dst.childBranchNumbers.addAll(src.childBranchNumbers);
        dst.children.clear();
        dst.parents.clear();
        dst.nParents = src.nParents;
        dst.nChildren = src.nChildren;
        dst.isDirty = src.isDirty;
    }

    public Network getNetwork() {
        return network;
    }

    public int getNr() {
        return nodeNumber;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(final double height) {
        this.height = height;
        isDirty |= Network.IS_DIRTY;
        for (NetworkNode c: children) {
            c.isDirty |= Network.IS_DIRTY;
        }
    }

    /**
     * A Node IS_DIRTY if its value (like height) has changed.
     * A Node IS_FILTHY if its parent or child has changed.
     * Otherwise the node IS_CLEAN.
     */
    public int isDirty() {
        return isDirty;
    }

    public void makeDirty(final int nDirty) {
        isDirty |= nDirty;
    }

    public int getParentCount() {
        return nParents;
    }

    public int getChildCount() {
        return nChildren;
    }

    public Multiset<NetworkNode> getParents() {
        return parents;
    }

    public Multiset<NetworkNode> getChildren() {
        return children;
    }

    public Multiset<NetworkNode> updateParents() {
        final Multiset<NetworkNode> parents = HashMultiset.create();

        for (NetworkNode n: network.nodes) {
            for (Integer i: n.childBranchNumbers) {
                final int childNodeNumber = network.getNodeNumber(i);
                final NetworkNode childNode = network.nodes[childNodeNumber];
                if (childNode == this) parents.add(n);
            }
        }

        return parents;
    }

    public Multiset<NetworkNode> updateChildren() {
        final Multiset<NetworkNode> children = HashMultiset.create();

        for (Integer i: childBranchNumbers) {
            final int childNodeNumber = network.getNodeNumber(i);
            final NetworkNode childNode = network.nodes[childNodeNumber];
            children.add(childNode);
        }

        return children;
    }

    public NetworkNode getParentByBranch(int branchNr) {
        for (NetworkNode p: parents) {
            if (p.childBranchNumbers.contains(branchNr))
                return p;
        }
        return null;
    }

    public NetworkNode getChildByBranch(int childBranchNr) {
        if (childBranchNumbers.contains(childBranchNr)) {
            final int childNodeNumber = network.getNodeNumber(childBranchNr);
            return network.nodes[childNodeNumber];
        }
        return null;
    }

    public boolean addParent(NetworkNode parent) {
        //TODO
        return true;
    }

    public boolean deleteParent(NetworkNode parent) {
        //TODO
        return true;
    }

    public boolean addChild(NetworkNode child) {
        //TODO
        return true;
    }

    public boolean deleteChild(NetworkNode child) {
        //TODO
        return true;
    }

    /**
     * @return true if current node is root node
     */
    public boolean isRoot() {
        return nParents == 0;
    }
    /**
     * @return true if current node is leaf node
     */
    public boolean isLeaf() {
        return nChildren == 0;
    }

    /**
     * @return true if current node is reticulation node
     */
    public boolean isReticulation() {
        return nParents == 2;
    }

    /**
     * @return true if current node is reticulation node
     */
    public boolean isSpeciation() {
        return nChildren == 2;
    }

    /**
     * whether the node has been visited, say by a recursive method
     */
    protected boolean visited = false; // this should be used outside of this class

    /* get and (re)set the visited indicator */
    public boolean isVisited() {
        return visited;
    }
    public void setVisited(boolean v) {
        visited = v;
    }

    private boolean touched = false; // this is only used inside this class

    private void resetAllTouched() {
        for (NetworkNode n: network.nodes) {
            n.touched = false;
        }
    }

    @Override
    public String toString() {
        resetAllTouched();
        return buildNewick(Double.POSITIVE_INFINITY, -1, true);
    }

    private String buildNewick(Double parentHeight, Integer parentBranchNumber, boolean printLabels) {
        final StringBuilder subtreeString = new StringBuilder();
        // only add children to a reticulation node once
        if (nChildren > 0 && !(touched)) {
            touched = true;
            subtreeString.append("(");
            int i = 0;
            for (Integer childBranchNumber: childBranchNumbers) {
                if (i > 0) subtreeString.append(",");
                final int childNodeNumber = network.getNodeNumber(childBranchNumber);
                NetworkNode childNode = network.nodes[childNodeNumber];
                subtreeString.append(childNode.buildNewick(height, childBranchNumber, printLabels));
                i++;
            }
            subtreeString.append(")");
        }

        if (label != null) {
            if (printLabels) subtreeString.append(label);
            else subtreeString.append(nodeNumber);
        }

        // add inheritance probabilities to reticulation nodes
        if (nParents == 2) {
            subtreeString.append("[&gamma=");

            if (parentBranchNumber == gammaBranchNumber) subtreeString.append(df.format(inheritProb));
            else subtreeString.append(df.format(1.0 - inheritProb));

            subtreeString.append("]");
        }

        if (parentHeight < Double.POSITIVE_INFINITY) {
            final double branchLength = parentHeight - height;
            subtreeString.append(":");
            subtreeString.append(df.format(branchLength));
        }

        return subtreeString.toString();
    }

    public Double getGamma() {
        return inheritProb;
    }

    public void setGamma(final Double newGamma) {
        inheritProb = newGamma;
        isDirty |= Network.IS_DIRTY;
    }

    public String getLabel() {
        return label;
    }

    public int getTraversalNumber() {
        return nodeNumber - network.leafNodeCount;
    }

    /**
     * returns total node count (leaf, internal including root) of subtree defined by this node
     */
    public int getNodeCount() {
        resetAllTouched();
        return recurseNodeCount();
    }
    private int recurseNodeCount() {
        if (touched) return 0;

        int nodeCount = 1;
        for (NetworkNode c: children) {
            nodeCount += c.recurseNodeCount();
        }

        touched = true;
        return nodeCount;
    }

    public int getLeafNodeCount() {
        resetAllTouched();
        return recurseLeafNodeCount();
    }
    private int recurseLeafNodeCount() {
        if (touched)
            return 0;
        else if (nChildren == 0)
            return 1;

        int nodeCount = 0;
        for (NetworkNode c: children) {
            nodeCount += c.recurseLeafNodeCount();
        }

        touched = true;
        return nodeCount;
    }

    public int getSpeciationNodeCount() {
        resetAllTouched();
        return recurseSpeciationNodeCount();
    }
    private int recurseSpeciationNodeCount() {
        if (touched) return 0;

        // don't count reticulation nodes
        int nodeCount = (nChildren == 2) ? 1 : 0;
        for (NetworkNode c: children) {
            nodeCount += c.recurseSpeciationNodeCount();
        }

        touched = true;
        return nodeCount;
    }

    public int getReticulationNodeCount() {
        resetAllTouched();
        return recurseReticulationNodeCount();
    }
    private int recurseReticulationNodeCount() {
        if (touched) return 0;

        // only count reticulation nodes
        int nodeCount = (nParents == 2) ? 1 : 0;
        for (NetworkNode c: children) {
            nodeCount += c.recurseReticulationNodeCount();
        }

        touched = true;
        return nodeCount;
    }

    public void printDeets() {
        System.out.println(String.format("%s: %s", "label", label));
        System.out.println(String.format("%s: %f", "inheritProb", inheritProb));
        System.out.println(String.format("%s: %f", "height", height));
        System.out.println(String.format("%s: %d", "nodeNr", nodeNumber));
        System.out.println(String.format("%s: %d", "branchNr", gammaBranchNumber));
        System.out.println(String.format("%s: %d", "nParents", nParents));
        System.out.println(String.format("%s: %d", "nChildren", nChildren));
        for (Integer i: childBranchNumbers) {
            System.out.println(String.format("%s: %d", "childBranchNumber", i));
        }
        System.out.println();
    }
}
