package snetworktests;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import beast.util.TreeParser;
import speciesnetwork.NetworkParser;

public class NetworkParserTest {
    TreeParser treeParser;
    NetworkParser networkParser;

    // "Partial" reticulation nodes (that is, #H nodes without any directly attached children in the extended newick string)
    // should be on the right. "Complete" reticulation nodes (with directly attached children) should be on the left.

    final String testNetwork1 = "(((A:0.2,(B:0.1)#H1[&gamma=0.9]:0.1)S1:0.3,(C:0.3,#H1:0.2)S2:0.2)R:0.1)";

    final String testNetwork = "(((((A:0.1)#H1[&gamma=0.9]:0.1)#H2[&gamma=0.8]:0.3,((#H2:0.1,(#H1:0.1)#H3[&gamma=0.7]:0.1)S1:0.1)#H4[&gamma=0.6]:0.1)S2:0.1,(#H4:0.1,#H3:0.3)S3:0.1)R:0.1)";

    public NetworkParserTest() {
        treeParser = new TreeParser();
        networkParser = new NetworkParser();

        try {
            treeParser.initByName("newick", testNetwork, "IsLabelledNewick", true, "adjustTipHeights", false);
            networkParser.initByName("tree", treeParser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParser() throws Exception {
        // System.out.println(testNetwork2);
        // System.out.println(networkParser.toString());
        assertEquals(testNetwork, networkParser.toString());
    }
}
