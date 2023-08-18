package dk.itu.datastructure;

import java.util.Arrays;

public class OsmFloatArray {
    private float[] list;
    private int index;

    private float minX, minY, maxX, maxY;

    private boolean isAtMaxCapacity;

    private boolean isSmall;

    //Construct a homemade array
    public OsmFloatArray(boolean small){
        index = 0;
        if(small){
            list = new float[9];
            isSmall = true;
        }
        else list = new float[999];
    }

    public boolean add(float inp){
        if(index == 8192000){
            isAtMaxCapacity = true;
            return false;
        }
        if(index == list.length - 1) resize();//Resize array if necessary

        list[index] = inp;
        index++;
        return true;
    }

    public float getElement(int index){
        return list[index];
    }

    public float[] getList(){
        return list;
    }

    public int usedSize(){
        return index;
    }

    public void clear(){
        if(isSmall) list = new float[8];
        else list = new float[1000];
        index = 0;
    }

    public void trim(){
        float[] trimmed = Arrays.copyOfRange(list,0, index);
        list = trimmed;
    }

    private void resize(){
        float[] resizedArray = Arrays.copyOf(list, list.length*2);
        list = resizedArray;
    }

    public boolean getIsAtMaxCapacity(){return isAtMaxCapacity;}

    public float getMinX(){return minX;}
    public float getMinY(){return minY;}
    public float getMaxX(){return maxX;}
    public float getMaxY(){return maxY;}
    public float getMinId(){
        return list[0];
    }
    public float getMaxId(){
        return list[index - 3];
    }
}
