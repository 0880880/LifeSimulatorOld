package com.lifesimulator;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkSystem {

    private Array<Chunkable>[][] chunks;

    private int chunkSizeX;
    private int chunkSizeY;

    public ChunkSystem(int chunksX, int chunksY, int chunkSizeX, int chunkSizeY) {
        this.chunkSizeX = chunkSizeX;
        this.chunkSizeY = chunkSizeY;
        chunks = new Array[chunksX][chunksY];

        for (int i = 0; i < chunksX; i++) {
            for (int j = 0; j < chunksY; j++) {
                chunks[i][j] = new Array<>();
            }
        }
    }

    public void add(Chunkable chunkable) {
        int x = (int) (chunkable.getPosition().x / chunkSizeX);
        int y = (int) (chunkable.getPosition().y / chunkSizeY);
        chunks[Math.min(x, chunks.length - 1)][Math.min(y, chunks[0].length - 1)].add(chunkable);
    }

    public void remove(Chunkable chunkable) {
        int x = (int) (chunkable.getPosition().x / chunkSizeX);
        int y = (int) (chunkable.getPosition().y / chunkSizeY);
        chunks[Math.min(x, chunks.length - 1)][Math.min(y, chunks[0].length - 1)].removeValue(chunkable, true);
    }

    public void update() {
        for (int i = 0; i < chunks.length; i++) {
            for (int j = 0; j < chunks[0].length; j++) {
                Array<Chunkable> chunk = chunks[i][j];
                for (int k = 0; k < chunk.size; k++) {
                    Chunkable chunkable = chunk.get(k);
                    int x = (int) (chunkable.getPosition().x / chunkSizeX);
                    int y = (int) (chunkable.getPosition().y / chunkSizeY);
                    if (i != x || j != y) {
                        chunk.removeValue(chunkable, true);
                        Array<Chunkable> newChunk = chunks[Math.min(x, chunks.length - 1)][Math.min(y, chunks[0].length - 1)];
                        newChunk.add(chunkable);
                    }
                }
            }
        }
    }

    public Array<Array<Chunkable>> getChunksInRange(Vector2 position, int r) {
        int x = (int) (position.x / chunkSizeX);
        int y = (int) (position.y / chunkSizeY);
        int range = r / chunkSizeX;
        Array<Array<Chunkable>> chunks = new Array<>();
        for (int i = x - range / 2; i < x + range / 2; i++) {
            for (int j = y - range / 2; j < y + range / 2; j++) {
                if (i >= 0 && j >= 0 && i < this.chunks.length && j < this.chunks[0].length) {
                    chunks.add(this.chunks[i][j]);
                }
            }
        }
        return chunks;
    }

}
