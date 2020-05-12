package edu.fandm.research.ideal.MachineLearning;

import android.content.Context;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import edu.fandm.research.ideal.Application.Database.DatabaseHandler;

public class TensorFlowClassifier {
    static {
        System.loadLibrary("tensorflow_inference");
    }

    private TensorFlowInferenceInterface inferenceInterface;
    private static final String MODEL_FILE = "file:///android_asset/frozen_model.pb";
    private static final String INPUT_NODE = "inputs";
    private static final String[] OUTPUT_NODES = {"y_"};
    private static final String OUTPUT_NODE = "y_";
    private static final long[] INPUT_SIZE = {1, 150, 8};
    private static final int OUTPUT_SIZE = 24;

    public TensorFlowClassifier(final Context context) {
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);
    }

    public void predictProbabilities(float[] data, Context context, String[] mapPackageNameToNo) {
        float[] result = new float[OUTPUT_SIZE];
        inferenceInterface.feed(INPUT_NODE, data, INPUT_SIZE);
        inferenceInterface.run(OUTPUT_NODES);
        inferenceInterface.fetch(OUTPUT_NODE, result);

        DatabaseHandler db = DatabaseHandler.getInstance(context);
        for(int i = 0; i < result.length; i++) {
            if (result[i] > 0.5) {
                SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);
                String formattedTimestamp = s.format(Calendar.getInstance().getTime());
                String timestamp = formattedTimestamp.replaceAll(":",".");
                db.addPrediction(timestamp, mapPackageNameToNo[(int) data[0]]);
            }
        }
    }
}
