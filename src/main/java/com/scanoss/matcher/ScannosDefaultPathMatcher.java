package com.scanoss.matcher;

import com.scanoss.ScanossConstants;

import java.nio.file.Path;
import java.nio.file.PathMatcher;

public class ScannosDefaultPathMatcher implements PathMatcher {


    @Override
    public boolean matches(Path path) {
        path.toFile().isDirectory()


        return false;
    }


    /**
     * Determine if a folder should be processed or not
     *
     * @param name folder/directory to review
     * @return <code>true</code> if the folder should be skipped, <code>false</code> otherwise
     */
    private Boolean filterFolder(String name) {
        String nameLower =  name.toLowerCase();
        if (!hiddenFilesFolders && name.startsWith(".") && !name.equals(".")) {
            log.trace("Skipping hidden folder: {}", name);
            return true;
        }
        boolean ignore = false;
        if (!allFolders) { // skip this check if all folders is selected
            for (String ending : ScanossConstants.FILTERED_DIRS) {
                if (nameLower.endsWith(ending)) {
                    log.trace("Skipping folder due to ending: {} - {}", name, ending);
                    ignore = true;
                }
            }
            if(!ignore){
                for (String ending : ScanossConstants.FILTERED_DIR_EXT) {
                    if (nameLower.endsWith(ending)) {
                        log.trace("Skipping folder due to ending: {} - {}", name, ending);
                        ignore = true;
                    }
                }
            }
        }
        return ignore;
    }


    /*

     */

    /**
     * Determine if a file should be processed or not
     *
     * @param name filename to review
     * @return <code>true</code> if the file should be skipped, <code>false</code> otherwise
     */
    private Boolean filterFile(String name) {
        if (skipFilters) return false;

        // Skip hidden files unless explicitly asked to read them
        if (!hiddenFilesFolders && name.startsWith(".")) {
            log.trace("Skipping hidden file: {}", name);
            return true;
        }
        // Process all file extensions if requested
        if (this.allExtensions) {
            log.trace("Processing all file extensions: {}", name);
            return false;
        }
        // Skip some specific files
        if (ScanossConstants.FILTERED_FILES.contains(name)) {
            log.trace("Skipping specific file: {}", name);
            return true;
        }
        // Skip specific file endings/extensions
        for (String ending : ScanossConstants.FILTERED_EXTENSIONS) {
            if (name.endsWith(ending)) {
                log.trace("Skipping file due to ending: {} - {}", name, ending);
                return true;
            }
        }
        return false;
    }

}
