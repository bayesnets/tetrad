package edu.cmu.tetrad.algcomparison.algorithm.bootstrap;

import edu.cmu.tetrad.algcomparison.algorithm.Algorithm;
import edu.cmu.tetrad.algcomparison.independence.IndependenceWrapper;
import edu.cmu.tetrad.algcomparison.score.ScoreWrapper;
import edu.cmu.tetrad.algcomparison.utils.HasKnowledge;
import edu.cmu.tetrad.annotation.AlgType;
import edu.cmu.tetrad.annotation.AlgorithmDescription;
import edu.cmu.tetrad.annotation.OracleType;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.DagToPag;
import edu.cmu.tetrad.util.Parameters;
import edu.pitt.dbmi.algo.bootstrap.BootstrapAlgName;
import edu.pitt.dbmi.algo.bootstrap.BootstrapEdgeEnsemble;
import java.io.PrintStream;
import java.util.List;

/**
 *
 * Apr 28, 2017 11:45:44 PM
 *
 * @author Chirayu (Kong) Wongchokprasitti, PhD
 *
 */
@AlgorithmDescription(
        name = "BootstrapGFCI",
        algType = AlgType.bootstrapping,
        oracleType = OracleType.Both,
        description = "Short blurb goes here"
)
public class BootstrapGfci implements Algorithm, HasKnowledge {

    static final long serialVersionUID = 23L;
    private IndependenceWrapper test;
    private ScoreWrapper score;
    private IKnowledge knowledge = new Knowledge2();

    public BootstrapGfci(IndependenceWrapper test, ScoreWrapper score) {
        this.test = test;
        this.score = score;
    }

    @Override
    public Graph search(DataModel dataSet, Parameters parameters) {
        if (dataSet == null || !(dataSet instanceof DataSet)) {
            throw new IllegalArgumentException(
                    "Sorry, I was expecting a (tabular) data set.");
        }
        DataSet data = (DataSet) dataSet;
        edu.pitt.dbmi.algo.bootstrap.BootstrapTest search = new edu.pitt.dbmi.algo.bootstrap.BootstrapTest(
                data, BootstrapAlgName.GFCI);
        search.setParameters(parameters);
        search.setKnowledge(knowledge);
        search.setVerbose(parameters.getBoolean("verbose"));
        search.setNumBootstrapSamples(parameters.getInt("bootstrapSampleSize"));

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

        Object obj = parameters.get("printStream");

        if (obj instanceof PrintStream) {
            search.setOut((PrintStream) obj);
        }

        return search.search();
    }

    @Override
    public Graph getComparisonGraph(Graph graph) {
        return new DagToPag(graph).convert();
    }

    @Override
    public String getDescription() {
        return "Bootstrapping GFCI (Greedy Fast Causal Inference) using "
                + test.getDescription();
    }

    @Override
    public DataType getDataType() {
        return test.getDataType();
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = test.getParameters();
        parameters.addAll(score.getParameters());
        parameters.add("faithfulnessAssumed");
        parameters.add("maxDegree");
        parameters.add("printStream");
        parameters.add("maxPathLength");
        parameters.add("completeRuleSetUsed");
        parameters.add("bootstrapSampleSize");
        parameters.add("bootstrapEnsemble");
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

}
