package project.model.maps;

import project.model.Vector2d;

import java.util.Iterator;
import java.util.Random;

public class RandomPositionGenerator implements Iterable<Vector2d> {

    private final int[] listOfNumbers;
    private final int numberOfGrass;
    private final int width;
    private final int height;

    public RandomPositionGenerator(int maxWidth, int maxHeight, int GrassCount) {
        numberOfGrass = GrassCount;
        listOfNumbers = new int[maxWidth * maxHeight];
        width = maxWidth;
        height = maxHeight;

        for(int i = 0; i < maxWidth * maxHeight; i++) {
            listOfNumbers[i] = i;
        }
    }

    @Override
    public Iterator<Vector2d> iterator() {
        return new VectorIterator<Vector2d>(listOfNumbers, numberOfGrass, width, height);
    }
}

class VectorIterator<V> implements Iterator<Vector2d> {

    private int howManyElements = 0;
    private final Random random = new Random();

    private final int[] listOfNumbers;
    private final int numberOfGrass;
    private final int width;
    private final int height;

    public VectorIterator(int[] listOfNumbers, int numberOfGrass, int width, int height) {

        this.listOfNumbers = listOfNumbers;
        this.numberOfGrass = numberOfGrass;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean hasNext() {
        return howManyElements < numberOfGrass;
    }

    @Override
    public Vector2d next() {

        int index = random.nextInt((height * width) - howManyElements);
        int element = listOfNumbers[index];

        int x = element / width;
        int y = element % width;

        listOfNumbers[index] = listOfNumbers[(height * width) - howManyElements - 1];

        howManyElements++;
        return new Vector2d(x, y);
    }
}