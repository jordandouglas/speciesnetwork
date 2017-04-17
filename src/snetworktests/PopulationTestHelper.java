package snetworktests;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import beast.core.State;
import beast.core.StateNode;
import beast.evolution.alignment.TaxonSet;
import beast.util.TreeParser;
import speciesnetwork.NetworkParser;
import speciesnetwork.EmbeddableTree;
import speciesnetwork.EmbeddedTreeParser;
import speciesnetwork.GeneTreeInSpeciesNetwork;
import speciesnetwork.MultispeciesCoalescent;
import speciesnetwork.PopulationSizeModel;
import speciesnetwork.operators.RebuildEmbedding;

abstract class PopulationTestHelper {
    String newickSpeciesNetwork;
    List<String> newickGeneTrees = new ArrayList<>();

    TaxonSet speciesSuperset;
    TreeParser speciesTree;
    NetworkParser speciesNetwork;
    List<EmbeddableTree> geneTrees = new ArrayList<>();
    List<GeneTreeInSpeciesNetwork> geneTreeWrappers = new ArrayList<>();

    State state = null;
    MultispeciesCoalescent msc;
    int nSpecies;
    int nBranches;
    int individualsPerSpecies;
    double popSize;
    double ploidy;
    double expectedLogP;
    List<int[]> embedding;

    final double allowedError = 1e-6;

    abstract public TaxonSet generateSuperset();
    abstract public PopulationSizeModel generatePopulationModel();

    @Test
    public void testLogP() {
        speciesSuperset = generateSuperset();
        initializeSpeciesNetwork();
        initializeStateNodes();
        initializeGeneTrees(false);

        final PopulationSizeModel populationModel = generatePopulationModel();
        populationModel.initPopSizes(nBranches);
        populationModel.initPopSizes(popSize);

        msc = new MultispeciesCoalescent();
        msc.initByName("speciesNetwork", speciesNetwork, "geneTreeWithin", geneTreeWrappers, "populationModel", populationModel);

        double calculatedLogP = msc.calculateLogP();
        assertEquals(expectedLogP, calculatedLogP, allowedError);
    }

    private void initializeSpeciesNetwork() {
        speciesTree = new TreeParser();
        speciesTree.initByName("newick", newickSpeciesNetwork, "IsLabelledNewick", true, "adjustTipHeights", false);
        speciesNetwork = new NetworkParser();
        speciesNetwork.initByName("tree", speciesTree);
    }

    private void initializeStateNodes() {
        if (state == null) state = new State();
        state.initialise();
    }

    private void initializeGeneTrees(boolean reembed) {
        for (int i = 0; i < newickGeneTrees.size(); i++) {
            final String geneTreeNewick = newickGeneTrees.get(i);
            EmbeddableTree embeddedTree = new EmbeddedTreeParser();
            ((StateNode) embeddedTree).initByName("newick", geneTreeNewick, "IsLabelledNewick", true);
            final int[] embedding = this.embedding.get(i);
            final int nRow = embeddedTree.getNodeCount();
            final int nCol = embedding.length / nRow;
            embeddedTree.resetEmbedding(nCol, -1);

            for (int r = 0; r < nRow; r++) {
            	for (int c = 0; c < nCol; c++)
            		embeddedTree.setEmbedding(r, c, embedding[r * nCol + c]);
            }

            geneTrees.add(embeddedTree);
            GeneTreeInSpeciesNetwork geneTreeWrapper = new GeneTreeInSpeciesNetwork();
            geneTreeWrapper.initByName("geneTree", embeddedTree, "ploidy", ploidy, "speciesNetwork", speciesNetwork);
            geneTreeWrappers.add(geneTreeWrapper);
        }
        if (reembed) { // rebuild the embedding
            RebuildEmbedding rebuildOperator = new RebuildEmbedding();
            rebuildOperator.initByName("speciesNetwork", speciesNetwork, "taxonSuperset", speciesSuperset,
                                       "geneTree", geneTrees);
            assertTrue(rebuildOperator.initializeEmbedding(true) >= 0);
        }
    }
}
