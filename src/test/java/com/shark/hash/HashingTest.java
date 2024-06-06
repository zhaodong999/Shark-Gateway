package com.shark.hash;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.junit.jupiter.api.Test;

public class HashingTest {

    @Test
    public void testHashing() {
        HashFunction hashFunction = Hashing.murmur3_128();
        HashCode hashCode1 = hashFunction.hashBytes("12".getBytes());
        System.out.println(Hashing.consistentHash(hashCode1, 1));

        HashCode hashCode2 = hashFunction.hashBytes("13".getBytes());
        System.out.println(Hashing.consistentHash(hashCode2, 1));

        HashCode hashCode3 = hashFunction.hashBytes("14".getBytes());
        System.out.println(Hashing.consistentHash(hashCode3, 1));

        HashCode hashCode4 = hashFunction.hashBytes("15".getBytes());
        System.out.println(Hashing.consistentHash(hashCode4, 1));
    }

    @Test
    public void testHashing2(){
        HashFunction hashFunction = Hashing.murmur3_128();
        HashCode hashCode1 = hashFunction.hashBytes("12".getBytes());
        System.out.println(Hashing.consistentHash(hashCode1, 2));

        HashCode hashCode2 = hashFunction.hashBytes("13".getBytes());
        System.out.println(Hashing.consistentHash(hashCode2, 2));

        HashCode hashCode3 = hashFunction.hashBytes("14".getBytes());
        System.out.println(Hashing.consistentHash(hashCode3, 2));

        HashCode hashCode4 = hashFunction.hashBytes("15".getBytes());
        System.out.println(Hashing.consistentHash(hashCode4, 2));
    }
}
