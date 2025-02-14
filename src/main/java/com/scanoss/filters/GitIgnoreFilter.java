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
package com.scanoss.filters;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Setter;
import org.eclipse.jgit.ignore.FastIgnoreRule;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.ignore.IgnoreNode.MatchResult;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A filter implementation that applies Git-style ignore patterns to file paths.
 * This class uses JGit's IgnoreNode and FastIgnoreRule for efficient pattern matching
 * that follows the same rules as .gitignore files.
 */
public class GitIgnoreFilter {
    private final IgnoreNode node;
    @Setter(AccessLevel.PRIVATE)
    private List<FastIgnoreRule> rules;

    @Builder
    public GitIgnoreFilter(@NotNull List<String> patterns) {
        this.rules = new ArrayList<>();
        patterns.forEach(pattern -> rules.add(new FastIgnoreRule(pattern)));
        this.node = new IgnoreNode(rules);
    }

    /**
     * Creates a predicate that tests if a path matches any of the ignore patterns.
     * The predicate uses JGit's IgnoreNode to perform the matching, which takes into
     * account whether the path represents a directory.
     *
     * @return a predicate that returns true if a path should be ignored according to the patterns
     */
    public Predicate<Path> get() {
        return p -> {
            MatchResult r = node.isIgnored(p.toString(), p.toFile().isDirectory());
            return r.equals(MatchResult.IGNORED);
        };
    }
}
