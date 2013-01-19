package algorithms;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.ArrayList;

import android.graphics.PointF;

/**
 * Filters data using Ramer-Douglas-Peucker algorithm with specified tolerance
 * 
 * @author Rzeźnik
 * @see <a href="http://en.wikipedia.org/wiki/Ramer-Douglas-Peucker_algorithm">Ramer-Douglas-Peucker algorithm</a>
 */
public class RamerDouglasPeuckerFilter implements DataFilter {

        private float epsilon;

        /**
         * 
         * @param epsilon
         *            epsilon in Ramer-Douglas-Peucker algorithm (maximum distance
         *            of a point in data between original curve and simplified
         *            curve)
         * @throws IllegalArgumentException
         *             when {@code epsilon <= 0}
         */
        public RamerDouglasPeuckerFilter(float epsilon) {
                if (epsilon <= 0) {
                        throw new IllegalArgumentException("Epsilon nust be > 0");
                }
                this.epsilon = epsilon;
        }

        @Override
        public float[][] filter(float[][] data) {
                return ramerDouglasPeuckerFunction(data, 0, data.length - 1);
        }
        
        public ArrayList<PointF> filter(ArrayList<PointF> input){
        	float[][] data = new float[input.size()][2];
        	for(int i = 0; i < input.size(); i++){
        		data[i] = new float[] {input.get(i).x, input.get(i).y};
        	}
        	float[][] result = ramerDouglasPeuckerFunction(data, 0, data.length - 1);
        	ArrayList<PointF> output = new ArrayList<PointF>();
        	for(int i = 0; i < result.length; i++){
        		output.add(new PointF(result[i][0], result[i][1]));
        	}
        	return output;
        }

        /**
         * 
         * @return {@code epsilon}
         */
        public float getEpsilon() {
                return epsilon;
        }

        protected float[][] ramerDouglasPeuckerFunction(float[][] points,
                        int startIndex, int endIndex) {
                float dmax = 0;
                int idx = 0;
                float dx = points[endIndex][0] - points[startIndex][0];
                float dy = points[endIndex][1] - points[startIndex][1];
                float c = -(dy * points[startIndex][0] - dx * points[startIndex][1]);
                float norm = (float) sqrt(pow(dx, 2) + pow(dy, 2));
                for (int i = startIndex + 1; i < endIndex; i++) {
                        float distance = abs(dy * points[i][0] - dx * points[i][1] + c) / norm;
                        if (distance > dmax) {
                                idx = i;
                                dmax = distance;
                        }
                }
                if (dmax >= epsilon) {
                        float[][] recursiveResult1 = ramerDouglasPeuckerFunction(points,
                                        startIndex, idx);
                        float[][] recursiveResult2 = ramerDouglasPeuckerFunction(points,
                                        idx, endIndex);
                        float[][] result = new float[(recursiveResult1.length - 1)
                                        + recursiveResult2.length][2];
                        System.arraycopy(recursiveResult1, 0, result, 0,
                                        recursiveResult1.length - 1);
                        System.arraycopy(recursiveResult2, 0, result,
                                        recursiveResult1.length - 1, recursiveResult2.length);
                        return result;
                } else {
                        return new float[][] { points[startIndex], points[endIndex] };
                }
        }

        /**
         * 
         * @param epsilon
         *            maximum distance of a point in data between original curve and
         *            simplified curve
         */
        public void setEpsilon(float epsilon) {
                if (epsilon <= 0) {
                        throw new IllegalArgumentException("Epsilon nust be > 0");
                }
                this.epsilon = epsilon;
        }

}