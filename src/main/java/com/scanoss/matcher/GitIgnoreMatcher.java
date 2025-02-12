package com.scanoss.matcher;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Setter;
import org.eclipse.jgit.ignore.FastIgnoreRule;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.ignore.IgnoreNode.MatchResult;
import org.jetbrains.annotations.NotNull;


import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

public class GitIgnoreMatcher implements PathMatcher {

    List<String> patterns;

    @Setter(AccessLevel.PRIVATE)
    List<FastIgnoreRule> rules;

    @Builder
    public GitIgnoreMatcher(@NotNull List<String> patterns) {
        this.rules = new ArrayList<>();
        this.patterns = patterns;
        this.patterns.forEach(pattern -> rules.add(new FastIgnoreRule(pattern)));
    }

    @Override
    public boolean matches(Path path) {
        IgnoreNode node = new IgnoreNode(rules);

        MatchResult result = node.isIgnored(path.toString(), path.toFile().isDirectory());
        return result.equals(MatchResult.IGNORED);
    }
}
