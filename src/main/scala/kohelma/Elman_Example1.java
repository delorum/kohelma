package kohelma;


import java.util.Random;
import java.text.DecimalFormat;

public class Elman_Example1
{
    // BEVector is the symbol used to start or end a sequence.
    private static final double BE_VECTOR[] = new double[] {1.0, 0.0, 0.0, 0.0, 0.0, 0.0};

                                                                //   0    1    2    3    4    5
    private static final double SAMPLE_INPUT[][] = new double[][] {{0.0, 0.0, 0.0, 1.0, 0.0, 0.0},
                                                                   {0.0, 0.0, 0.0, 0.0, 0.0, 1.0},
                                                                   {0.0, 0.0, 1.0, 0.0, 0.0, 0.0}};

    private static final int MAX_TESTS = 10000;

    private static final int MAX_SAMPLES = 4;

    private static final int INPUT_NEURONS = 6;
    private static final int HIDDEN_NEURONS = 3;
    private static final int OUTPUT_NEURONS = 6;
    private static final int CONTEXT_NEURONS = 3;

    private static final double LEARN_RATE = 0.2;    // Rho.
    private static final int TRAINING_REPS = 2000;

    // Input to Hidden Weights (with Biases).
    private static double wih[][] = new double[INPUT_NEURONS + 1][HIDDEN_NEURONS];

    // Context to Hidden Weight (with Biases).
    private static double wch[][] = new double[CONTEXT_NEURONS + 1][HIDDEN_NEURONS];

    // Hidden to Output Weights (with Biases).
    private static double who[][] = new double[HIDDEN_NEURONS + 1][OUTPUT_NEURONS];

    // Hidden to Context Weights (no Biases).
    private static double whc[][] = new double[OUTPUT_NEURONS + 1][CONTEXT_NEURONS];

    // Activations.
    private static double inputs[] = new double[INPUT_NEURONS];
    private static double hidden[] = new double[HIDDEN_NEURONS];
    private static double target[] = new double[OUTPUT_NEURONS];
    private static double actual[] = new double[OUTPUT_NEURONS];
    private static double context[] = new double[CONTEXT_NEURONS];

    // Unit errors.
    private static double erro[] = new double[OUTPUT_NEURONS];
    private static double errh[] = new double[HIDDEN_NEURONS];

    private static void elmanNetwork()
    {
        double err = 0.0;
        int sample = 0;
        int iterations = 0;
        boolean stopLoop = false;

        assignRandomWeights();

        // Train the network.
        while(!stopLoop)
        {
            if(sample == 0){
                for(int i = 0; i < INPUT_NEURONS; i++)
                {
                    inputs[i] = BE_VECTOR[i];
                }
            }else{
                for(int i = 0; i < INPUT_NEURONS; i++)
                {
                    inputs[i] = SAMPLE_INPUT[sample - 1][i];
                }
            }

            // After the samples are entered into the input units, the sample are
            // then offset by one and entered into target-output units for
            // later comparison.
            if(sample == MAX_SAMPLES - 1){
                for(int i = 0; i < INPUT_NEURONS; i++)
                {
                    target[i] = BE_VECTOR[i];
                }
            }else{
                for(int i = 0; i < INPUT_NEURONS; i++)
                {
                    target[i] = SAMPLE_INPUT[sample][i];
                }
            }

            feedForward();

            err = 0.0;
            for(int i = 0; i < OUTPUT_NEURONS; i++)
            {
                err += Math.sqrt(target[i] - actual[i]);
            }
            err = 0.5 * err;

            if(iterations > TRAINING_REPS){
                stopLoop = true;
            }
            iterations++;

            backPropagate();

            sample++;
            if(sample == MAX_SAMPLES){
                sample = 0;
            }
        }

        System.out.println("Iterations = " + iterations);
        return;
    }

    private static void testNetwork()
    {
        int index = 0;
        int randomNumber = 0;
        int predicted = 0;
        boolean stopSample = false;
        boolean successful = false;
        DecimalFormat dfm = new java.text.DecimalFormat("###0.000");

        // Test the network with random input patterns.
        for(int test = 0; test < MAX_TESTS; test++) // Do random tests.
        {
            // Enter Beginning string.
            inputs[0] = 1.0;
            inputs[1] = 0.0;
            inputs[2] = 0.0;
            inputs[3] = 0.0;
            inputs[4] = 0.0;
            inputs[5] = 0.0;
            System.out.print("\n(0) ");

            feedForward();

            stopSample = false;
            successful = false;
            index = 0;
            randomNumber = 0;
            predicted = 0;
            while(stopSample == false)
            {
                for(int i = 0; i < OUTPUT_NEURONS; i++)
                {
                    System.out.print(dfm.format(actual[i]) + " ");
                    if(actual[i] >= 0.3){
                        // The output unit with the highest value (usually over 3.0)
                        // is the network's predicted unit that it expects to appear
                        // in the next input vector.
                        // For example, if the 3rd output unit has the highest value,
                        // the network expects the 3rd unit in the next input to
                        // be 1.0
                        // If the actual value isn't what it expected, the random
                        // sequence has failed, and a new test sequence begins.
                        predicted = i;
                    }
                } // i
                System.out.print("\n");

                index++;
                if(index == OUTPUT_NEURONS - 1){
                    stopSample = true;
                }

                // Enter a random number.
                randomNumber = getRandomNumber();
                System.out.print("(" + randomNumber + ") ");
                for(int i = 0; i < INPUT_NEURONS; i++)
                {
                    if(i == randomNumber){
                        inputs[i] = 1.0;
                        if(i == predicted){
                            successful = true;
                        }else{
                            // failure. Stop this sample and try a new sample.
                            stopSample = true;
                        }
                    }else{
                        inputs[i] = 0.0;
                    }
                } // i

                feedForward();

            } // Enter another number into this sample sequence.

            if((index > OUTPUT_NEURONS - 2) && (successful == true)){
                // If the random sequence happens to be in the correct order, the network reports success.
                System.out.println("Success.");
                System.out.println("Completed " + test + " tests.");
                break;
            }else{
                System.out.println("Failed.");
                if(test > MAX_TESTS){
                    System.out.println("Completed " + test + " tests with no success.");
                    break;
                }
            }
        } // test

        return;
    }

    private static void feedForward()
    {
        double sum = 0.0;

        // Calculate input and context connections to hidden layer.
        for(int hid = 0; hid < HIDDEN_NEURONS; hid++)
        {
            sum = 0.0;
            for(int inp = 0; inp < INPUT_NEURONS; inp++)    // from input to hidden...
            {
                sum += inputs[inp] * wih[inp][hid];
            } // inp

            for(int con = 0; con < CONTEXT_NEURONS; con++)    // from context to hidden...
            {
                sum += context[con] * wch[con][hid];
            } // con

            sum += wih[INPUT_NEURONS][hid];    // Add in bias.
            sum += wch[CONTEXT_NEURONS][hid];
            hidden[hid] = sigmoid(sum);
        } // hid

        // Calculate the hidden to output layer.
        for(int out = 0; out < OUTPUT_NEURONS; out++)
        {
            sum = 0.0;
            for(int hid = 0; hid < HIDDEN_NEURONS; hid++)
            {
                sum += hidden[hid] * who[hid][out];
            } // hid

            sum += who[HIDDEN_NEURONS][out];    // Add in bias.
            actual[out] = sigmoid(sum);
        } // out

        // Copy outputs of the hidden to context layer.
        for(int con = 0; con < CONTEXT_NEURONS; con++)
        {
            context[con] = hidden[con];
        }
        return;
    }

    private static void backPropagate()
    {
        // Calculate the output layer error (step 3 for output cell).
        for(int out = 0; out < OUTPUT_NEURONS; out++)
        {
            erro[out] = (target[out] - actual[out]) * sigmoidDerivative(actual[out]);
        }

        // Calculate the hidden layer error (step 3 for hidden cell).
        for(int hid = 0; hid < HIDDEN_NEURONS; hid++)
        {
            errh[hid] = 0.0;
            for(int out = 0; out < OUTPUT_NEURONS; out++)
            {
                errh[hid] += erro[out] * who[hid][out];
            } // out
            errh[hid] *= sigmoidDerivative(hidden[hid]);
        } // hid

        // Update the weights for the output layer (step 4).
        for(int out = 0; out < OUTPUT_NEURONS; out++)
        {
            for(int hid = 0; hid < HIDDEN_NEURONS; hid++)
            {
                who[hid][out] += (LEARN_RATE * erro[out] * hidden[hid]);
            } // hid

            who[HIDDEN_NEURONS][out] += (LEARN_RATE * erro[out]);    // Update the bias.
        } // out

        // Update the weights for the hidden layer (step 4).
        for(int hid = 0; hid < HIDDEN_NEURONS; hid++)
        {
            for(int inp = 0; inp < INPUT_NEURONS; inp++)
            {
                wih[inp][hid] += (LEARN_RATE * errh[hid] * inputs[inp]);
            } // inp

            wih[INPUT_NEURONS][hid] += (LEARN_RATE * errh[hid]);    // Update the bias.
        } // hid
        return;
    }

    private static void assignRandomWeights()
    {
        for(int inp = 0; inp <= INPUT_NEURONS; inp++)    // Do not subtract 1 here.
        {
            for(int hid = 0; hid < HIDDEN_NEURONS; hid++)
            {
                // Assign a random weight value between -0.5 and 0.5
                wih[inp][hid] = new Random().nextDouble() - 0.5;
            } // hid
        } // inp

        for(int con = 0; con <= CONTEXT_NEURONS; con++)
        {
            for(int hid = 0; hid < HIDDEN_NEURONS; hid++)
            {
                // Assign a random weight value between -0.5 and 0.5
                wch[con][hid] = new Random().nextDouble() - 0.5;
            } // hid
        } // con

        for(int hid = 0; hid <= HIDDEN_NEURONS; hid++)    //Do not subtract 1 here.
        {
            for(int out = 0; out < OUTPUT_NEURONS; out++)
            {
                // Assign a random weight value between -0.5 and 0.5
                who[hid][out] = new Random().nextDouble() - 0.5;
            } // out
        } // hid

        for(int out = 0; out <= OUTPUT_NEURONS; out++)
        {
            for(int con = 0; con < CONTEXT_NEURONS; con++)
            {
                // These are all fixed weights set to 0.5
                whc[out][con] = 0.5;
            } // con
        } //  out
        return;
    }

    private static int getRandomNumber()
    {
        // Generate random value between 0 and INPUT_NEURONS.
        return new Random().nextInt(INPUT_NEURONS);
    }

    private static double sigmoid(double val)
    {
        return (1.0 / (1.0 + Math.exp(-val)));
    }

    private static double sigmoidDerivative(double val)
    {
        return (val * (1.0 - val));
    }

    public static void main(String[] args)
    {
        elmanNetwork();
        testNetwork();
        return;
    }

}
