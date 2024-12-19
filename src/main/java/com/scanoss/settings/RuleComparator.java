package com.scanoss.settings;

import java.util.Comparator;

public class RuleComparator implements Comparator<Rule> {
    @Override
    public int compare(Rule r1, Rule r2) {
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