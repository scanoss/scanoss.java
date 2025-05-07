// SPDX-License-Identifier: MIT
/*
 * Copyright (c) 2025, SCANOSS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.scanoss;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;
/**
 * Tests to validate thread safety of the path obfuscation feature in the Winnowing class.
 */
@Slf4j
public class WinnowingConcurrencyTest {

    /**
     * Test that concurrent obfuscation of paths works correctly without data loss or corruption.
     * This simulates multiple threads processing different files simultaneously.
     */
    @Test
    public void testConcurrentObfuscation() throws InterruptedException, ExecutionException {
        int fileCount = 500; // More files to increase collision chances
        int iterations = 3;  // Run multiple iterations to increase stress


        for (int iter = 0; iter < iterations; iter++) {
            log.info("Starting high-collision test iteration {}", iter);

            Winnowing winnowing = Winnowing.builder().obfuscate(true).build();

            ExecutorService executor = Executors.newFixedThreadPool(fileCount);

            // Create a list of paths to obfuscate
            List<String> paths = new ArrayList<>();
            for (int i = 0; i < fileCount; i++) {
                paths.add("/path/to/file" + i + ".java");
            }

            List<Future<String>> futures = new ArrayList<>(fileCount);

            // Use a CyclicBarrier to ensure all threads start exactly together.
            CyclicBarrier barrier = new CyclicBarrier(fileCount, () -> log.info("All threads released simultaneously!"));

            for (String path : paths) {
                futures.add(executor.submit(() -> {
                    try {
                        byte[] contents = ("sample content for " + path).getBytes();

                        // Wait at barrier until all threads are ready
                        barrier.await();

                        // Access the same Winnowing instance concurrently
                        return winnowing.wfpForContents(path, false, contents);

                    } catch (InterruptedException | BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            // Wait for all tasks to complete
            for (Future<String> future : futures) {
                future.get();
            }

            executor.shutdown();
            assertTrue("Executor did not terminate properly",
                    executor.awaitTermination(5, TimeUnit.SECONDS));

            // Verify results
            log.info("Processed {} paths with {} unique results",
                    paths.size(), winnowing.getObfuscationMapSize());
            assertEquals(paths.size(), winnowing.getObfuscationMapSize());
        }
    }
}