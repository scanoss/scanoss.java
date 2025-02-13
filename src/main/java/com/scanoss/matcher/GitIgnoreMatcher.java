package com.scanoss.matcher;

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

public class GitIgnoreMatcher implements PathFilter {

    List<String> patterns;

    @Setter(AccessLevel.PRIVATE)
    List<FastIgnoreRule> rules;

    @Builder
    public GitIgnoreMatcher(@NotNull List<String> patterns) {
        this.rules = new ArrayList<>();
        this.patterns = patterns;
        this.patterns.forEach(pattern -> rules.add(new FastIgnoreRule(pattern)));
    }

    public Predicate<Path> getPathFilter() {
        IgnoreNode node = new IgnoreNode(rules);
        return p -> {
            MatchResult r = node.isIgnored(p.toString(), p.toFile().isDirectory());
            return r.equals(MatchResult.IGNORED);
        };
    }
}
