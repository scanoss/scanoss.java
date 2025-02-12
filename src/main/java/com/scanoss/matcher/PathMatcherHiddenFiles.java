package com.scanoss.matcher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.nio.file.PathMatcher;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathMatcherHiddenFiles implements PathMatcher {

    @Override
    public boolean matches(Path path) {
        //TODO: If path.
        if path.toFile().isDirectory() {
            //oen thing
        } else {
            //another thing
        }
        return false;
    }
}
