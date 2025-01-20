// SPDX-License-Identifier: MIT
/*
 * Copyright (c) 2024, SCANOSS
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
package com.scanoss.settings;

import lombok.NonNull;

import java.util.Comparator;


/**
 * A comparator implementation for sorting {@link Rule} objects based on their attributes
 * and a defined priority system.
 *
 * <p>The comparison is performed using the following criteria in order:</p>
 * <ol>
 *   <li>Priority score based on the presence of PURL and path attributes</li>
 *   <li>Path length comparison (if priority scores are equal)</li>
 * </ol>
 *
 * <p>Priority scoring system:</p>
 * <ul>
 *   <li>Score 4: Rule has both PURL and path</li>
 *   <li>Score 2: Rule has only PURL</li>
 *   <li>Score 1: Rule has only path</li>
 *   <li>Score 0: Rule has neither PURL nor path</li>
 * </ul>
 */
public class RuleComparator implements Comparator<Rule> {

    /**
     * Compares two Rule objects based on their priority scores and path lengths.
     *
     * <p>The comparison follows these steps:</p>
     * <ol>
     *   <li>Calculate priority scores for both rules</li>
     *   <li>If scores differ, higher score takes precedence</li>
     *   <li>If scores are equal and both rules have paths, longer path takes precedence</li>
     *   <li>If no differentiation is possible, rules are considered equal</li>
     * </ol>
     *
     * @param r1 the first Rule to compare
     * @param r2 the second Rule to compare
     * @return a negative integer, zero, or a positive integer if the first rule is less than,
     *         equal to, or greater than the second rule respectively
     */
    @Override
    public int compare(@NonNull Rule r1, @NonNull Rule r2) {
        int score1 = calculatePriorityScore(r1);
        int score2 = calculatePriorityScore(r2);

        // If scores are different, use the score comparison
        if (score1 != score2) {
            return Integer.compare(score2, score1);
        }

        String r1Path = r1.getPath();
        String r2Path = r2.getPath();

        // If both rules have paths and scores are equal, compare path lengths
        if (r1Path != null &&  r2Path != null) {
            return Integer.compare(r2Path.length(), r1Path.length());
        }

        return 0;
    }


    /**
     * Calculates a priority score for a rule based on its attributes.
     *
     * <p>The scoring system is as follows:</p>
     * <ul>
     *   <li>4 points: Rule has both PURL and path</li>
     *   <li>2 points: Rule has only PURL</li>
     *   <li>1 point: Rule has only path</li>
     *   <li>0 points: Rule has neither PURL nor path</li>
     * </ul>
     *
     * @param rule the Rule object to calculate the score for
     * @return an integer representing the priority score of the rule
     */
    private int calculatePriorityScore(Rule rule) {
        int score = 0;

        boolean hasPurl = rule.getPurl() != null;
        boolean hasPath = rule.getPath() != null;

        // Highest priority: has all fields
        if (hasPurl && hasPath) {
            score = 4;
        }
        // Second priority: has purl only
        else if (hasPurl) {
            score = 2;
        }
        // Lowest priority: has path only
        else if (hasPath) {
            score = 1;
        }

        return score;
    }
}