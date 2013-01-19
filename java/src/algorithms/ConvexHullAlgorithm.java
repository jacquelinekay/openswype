package algorithms;
import java.util.ArrayList;

import android.graphics.PointF;


public interface ConvexHullAlgorithm 
{
        ArrayList<PointF> execute(ArrayList<PointF> points);
}