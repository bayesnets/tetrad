package edu.cmu.tetrad.algcomparison.algorithm.oracle.pag;

import edu.cmu.tetrad.algcomparison.algorithm.Algorithm;
import edu.cmu.tetrad.algcomparison.independence.IndependenceWrapper;
import edu.cmu.tetrad.algcomparison.utils.HasKnowledge;
import edu.cmu.tetrad.algcomparison.utils.TakesIndependenceWrapper;
import edu.cmu.tetrad.annotation.AlgType;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataType;
import edu.cmu.tetrad.data.IKnowledge;
import edu.cmu.tetrad.data.Knowledge2;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.DagToPag;
import edu.cmu.tetrad.util.Parameters;
import edu.pitt.dbmi.algo.bootstrap.BootstrapEdgeEnsemble;
import edu.pitt.dbmi.algo.bootstrap.GeneralBootstrapTest;
import java.util.List;

/**
 * RFCI.
 *
 * @author jdramsey
 */
@edu.cmu.tetrad.annotation.Algorithm(
        name = "RFCI",
        command = "rfci",
        algoType = AlgType.allow_latent_common_causes
)
public class Rfci implements Algorithm, HasKnowledge, TakesIndependenceWrapper {

    static final long serialVersionUID = 23L;
    private IndependenceWrapper test;
    private IKnowledge knowledge = new Knowledge2();

    public Rfci() {
    }

    public Rfci(IndependenceWrapper test) {
        this.test = test;
    }

    @Override
    public Graph search(DataModel dataSet, Parameters parameters) {
    	if (parameters.getInt("bootstrapSampleSize") < 1) {
            edu.cmu.tetrad.search.Rfci search = new edu.cmu.tetrad.search.Rfci(test.getTest(dataSet, parameters));
            search.setKnowledge(knowledge);
            search.setDepth(parameters.getInt("depth"));
            search.setMaxPathLength(parameters.getInt("maxPathLength"));
            search.setCompleteRuleSetUsed(parameters.getBoolean("completeRuleSetUsed"));
            return search.search();
        } else {
            Rfci algorithm = new Rfci(test);
            //algorithm.setKnowledge(knowledge);
//          if (initialGraph != null) {
//      		algorithm.setInitialGraph(initialGraph);
//  		}
            DataSet data = (DataSet) dataSet;
            GeneralBootstrapTest search = new GeneralBootstrapTest(data, algorithm, parameters.getInt("bootstrapSampleSize"));
            search.setKnowledge(knowledge);

            BootstrapEdgeEnsemble edgeEnsemble = BootstrapEdgeEnsemble.Highest;
            switch (parameters.getInt("bootstrapEnsemble", 1)) {
                case 0:
                    edgeEnsemble = BootstrapEdgeEnsemble.Preserved;
                    break;
                case 1:
                    edgeEnsemble = BootstrapEdgeEnsemble.Highest;
                    break;
                case 2:
                    edgeEnsemble = BootstrapEdgeEnsemble.Majority;
            }
            search.setEdgeEnsemble(edgeEnsemble);
            search.setParameters(parameters);
            search.setVerbose(parameters.getBoolean("verbose"));
            return search.search();
        }
    }

    @Override
    public Graph getComparisonGraph(Graph graph) {
        return new DagToPag(new EdgeListGraph(graph)).convert();
    }

    public String getDescription() {
        return "RFCI (Really Fast Causal Inference) using " + test.getDescription();
    }

    @Override
    public DataType getDataType() {
        return test.getDataType();
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = test.getParameters();
        parameters.add("depth");
        parameters.add("maxPathLength");
        parameters.add("completeRuleSetUsed");
        // Bootstrapping
        parameters.add("bootstrapSampleSize");
        parameters.add("bootstrapEnsemble");
        parameters.add("verbose");
        return parameters;
    }

    @Override
    public IKnowledge getKnowledge() {
        return knowledge;
    }

    @Override
    public void setKnowledge(IKnowledge knowledge) {
        this.knowledge = knowledge;
    }

    @Override
    public void setIndependenceWrapper(IndependenceWrapper test) {
        this.test = test;
    }
}
