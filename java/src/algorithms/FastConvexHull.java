package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.graphics.PointF;

public class FastConvexHull implements ConvexHullAlgorithm
{
	public static final float threshold = 50f;
        @Override
        public ArrayList<PointF> execute(ArrayList<PointF> Points) 
        {
            if(Points.size() <= 3){
            	return Points;
            }
                ArrayList<PointF> result2 = new ArrayList<PointF>();
                //result2.add(Points.remove(0));
                //PointF last = Points.remove(Points.size() - 1);
                ArrayList<PointF> xSorted = (ArrayList<PointF>) Points.clone();
                Collections.sort(xSorted, new XCompare());
                int n = xSorted.size();
                
                PointF[] lUpper = new PointF[n];
                
                lUpper[0] = xSorted.get(0);
                lUpper[1] = xSorted.get(1);
                
                int lUpperSize = 2;
                
                for (int i = 2; i < n; i++)
                {
                        lUpper[lUpperSize] = xSorted.get(i);
                        lUpperSize++;
                        
                        while (lUpperSize > 2 && !rightTurn(lUpper[lUpperSize - 3], lUpper[lUpperSize - 2], lUpper[lUpperSize - 1]))
                        {
                                // Remove the middle PointF of the three last
                                lUpper[lUpperSize - 2] = lUpper[lUpperSize - 1];
                                lUpperSize--;
                        }
                }
                
                PointF[] lLower = new PointF[n];
                
                lLower[0] = xSorted.get(n - 1);
                lLower[1] = xSorted.get(n - 2);
                
                int lLowerSize = 2;
                
                for (int i = n - 3; i >= 0; i--)
                {
                        lLower[lLowerSize] = xSorted.get(i);
                        lLowerSize++;
                        
                        while (lLowerSize > 2 && !rightTurn(lLower[lLowerSize - 3], lLower[lLowerSize - 2], lLower[lLowerSize - 1]))
                        {
                                // Remove the middle PointF of the three last
                                lLower[lLowerSize - 2] = lLower[lLowerSize - 1];
                                lLowerSize--;
                        }
                }
                
                ArrayList<PointF> result = new ArrayList<PointF>();
                
                for (int i = 0; i < lUpperSize; i++)
                {
                        result.add(lUpper[i]);
                }
                
                for (int i = 1; i < lLowerSize - 1; i++)
                {
                        result.add(lLower[i]);
                }
                //sort result by position in original list
                for (int i = 0; i < Points.size(); i++){
                	PointF point = Points.get(i);
                	if(result.contains(point)){
                		result2.add(point);
                	}
                }
                //result2.add(last);
                return result2;
        }
        
        private boolean rightTurn(PointF a, PointF b, PointF c)
        {
                return (b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x) > threshold;
        }

        private class XCompare implements Comparator<PointF>
        {
                @Override
                public int compare(PointF o1, PointF o2) 
                {
                        return (new Float(o1.x)).compareTo(new Float(o2.x));
                }
        }
}