package speciesnetwork;

import java.util.*;

import beast.core.Description;
import beast.core.Function;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.StateNode;
import beast.core.StateNodeInitialiser;
import beast.core.parameter.RealParameter;
import beast.evolution.tree.Tree;
import speciesnetwork.operators.RebuildEmbedding;

/**
 * @author Joseph Heled
 * @author Huw Ogilvie
 * @author Chi Zhang
 */

@Description("Set a starting point for a *BEAST analysis from gene alignment data.")
public class StarBeastInitializer extends Tree implements StateNodeInitialiser {

    private enum Method {
        POINT("point"),
        RANDOM("random");

        Method(final String name) {
            this.ename = name;
        }

        @Override
		public String toString() {
            return ename;
        }

        private final String ename;
    }
    public final Input<Method> initMethod = new Input<>("method", "Initialise either with a totally random state" +
            "or a point estimate based on alignments data (default point)", Method.POINT, Method.values());
    public final Input<Network> speciesNetworkInput
            = new Input<>("speciesNetwork", "Species network to initialize.");
    public final Input<List<Tree>> geneTreesInput =
            new Input<>("geneTree", "Gene tree to initialize.", new ArrayList<>());
    public final Input<List<RebuildEmbedding>> rebuildEmbeddingInput = new Input<>("rebuildEmbedding",
            "Operator which rebuilds embedding of gene trees within species network.", new ArrayList<>());
    public final Input<YuleHybridModel> hybridYuleInput = new Input<>("hybridYule",
            "The species network (with hybridization) to initialize.", Validate.XOR, speciesNetworkInput);
    public final Input<RealParameter> birthRateInput =
            new Input<>("birthRate", "Network prior birth rate to initialize.");
    public final Input<RealParameter> hybridRateInput =
            new Input<>("hybridRate", "Network hybridization rate to initialize.");
    public final Input<Function> clockRateInput =
            new Input<>("baseRate", "Main clock rate used to scale trees (default 1).");

    @Override
    public void initAndValidate() {
        // what does this do and is it dangerous to call it or not to call it at the start or at the end??????
        super.initAndValidate();
    }

    @Override
    public void initStateNodes() {
        final Method method = initMethod.get();
        switch( method ) {
            case POINT:
                pointInit();
                break;
            case RANDOM:
                randomInit();
                break;
        }

        // initialize embedding for all gene trees
        for (RebuildEmbedding operator: rebuildEmbeddingInput.get()) {
            if (operator.initializeEmbedding() < 0)
                throw new RuntimeException("Failed to build gene tree embedding!");
        }
    }

    private void pointInit() {
        // TODO: initialize parameters using point estimates
    }

    private void randomInit() {
        final Network sNetwork = speciesNetworkInput.get();

        // initialize population sizes to equal average branch length
        // final double speciesNetworkLength = sNetwork.getNetworkLength();
        // final int nSpeciesBranches = sNetwork.getBranchCount();
        // final double averageBranchLength = speciesNetworkLength / (nSpeciesBranches - 1);
        // final PopulationSizeModel populationModel = populationModelInput.get();
        // populationModel.initPopSizes(averageBranchLength);

        // do not scale the species network at the moment!
        final double rootHeight = sNetwork.getRoot().getHeight();
        final List<Tree> geneTrees = geneTreesInput.get();
        for (final Tree gtree : geneTrees) {
            gtree.makeCaterpillar(2*rootHeight, 2*rootHeight/gtree.getInternalNodeCount(), true);
        }
    }

    @Override
    public void getInitialisedStateNodes(List<StateNode> stateNodes) {
        stateNodes.add(speciesNetworkInput.get());
        for(final Tree gtree : geneTreesInput.get()) {
            stateNodes.add(gtree);
        }

        final RealParameter brate = birthRateInput.get();
        if(brate != null) stateNodes.add(brate);
        final RealParameter hrate = hybridRateInput.get();
        if(hrate != null) stateNodes.add(hrate);
    }
}
