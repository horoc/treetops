package org.treetops.core.validation;

import io.github.horoc.treetops.core.predictor.Predictor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author chenzhou@apache.org
 * created on 2023/2/18
 */
public class BreastCancerModelTest extends LoadModelTemplate {

    @Override
    protected double[] getFeature() {
        return new double[] {-0.20656117887535716, 0.2863110515326301, -0.13712355201435386, -0.2792598864377929, 1.0133758820201568, 0.8065563120018667, 0.699320480275225, 0.8460646517821447,
            1.1112791563311162, 1.4817350698939018, -0.05259361139069434, -0.5193621584675753, 0.11234262958699215, -0.14668713819749174, -0.5423482925856186, -0.15806337722209013,
            0.08707974741228168, 0.250429487524521, -0.4228423090304796, 0.0794691444911607, 0.029159331082772376, 0.6485704748522869, 0.1798703441573848, -0.06360678115852603, 1.0972739926049955,
            0.835473817212997, 1.1437848605273928, 1.3779123052290023, 1.1069571429479146, 1.4936880726625947};
    }

    @Test
    public void testPredictByGeneratedClass() {
        try {
            Predictor predictor = loadModel("breast_cancer_model", true);
            double[] result = predictor.predict(getFeature());
            Assertions.assertEquals(0.03825810017556627D, result[0]);
        } catch (Throwable e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void testSimplePredictor() {
        try {
            Predictor predictor = loadModel("breast_cancer_model", false);
            double[] result = predictor.predict(getFeature());
            Assertions.assertEquals(0.03825810017556627D, result[0]);
        } catch (Throwable e) {
            Assertions.fail(e);
        }
    }
}
