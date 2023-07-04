package com.scanoss.cli;

import com.scanoss.Scanner;
import com.scanoss.exceptions.ScannerException;
import com.scanoss.exceptions.WinnowingException;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

import static com.scanoss.cli.CommandLine.printMsg;

/**
 * Fingerprint Command Line Processor Class
 * <p>
 *     Produce fingerprints using the Winnowing algorithm
 * </p>
 */
@CommandLine.Command(name = "wfp", aliases = {"fingerprint", "fp"}, description = "Fingerprint the given file/folder")
public class WfpCommandLine implements Runnable {
    @picocli.CommandLine.ParentCommand
    com.scanoss.cli.CommandLine parent;
    @picocli.CommandLine.Spec
    picocli.CommandLine.Model.CommandSpec spec;
    @picocli.CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display help information")
    private boolean helpRequested = false;
    @picocli.CommandLine.Parameters(arity = "1", description = "file/folder to fingerprint")
    private String fileFolder;

    /**
     * Run the 'wfp' command
     */
    @Override
    public void run() {
        if (fileFolder == null || fileFolder.isEmpty()) {
            throw new RuntimeException("Error: No file or folder specified to scan");
        }
        File f = new File(fileFolder);
        if (!f.exists()) {
            throw new RuntimeException( String.format("Error: File or folder does not exist: %s\n", fileFolder));
        }
        if (f.isFile()) {
            wfpFile(fileFolder);
        } else if (f.isDirectory()) {
            wfpFolder(fileFolder);
        } else {
            throw new RuntimeException( String.format("Error: Specified path is not a file or a folder: %s\n", fileFolder));
        }
    }

    /**
     * Fingerprint the specified file and output the results
     *
     * @param file file to fingerprint
     */
    private void wfpFile(String file) {
        var out = spec.commandLine().getOut();
        var err = spec.commandLine().getErr();
        Scanner scanner = Scanner.builder().build();
        try {
            printMsg(err, String.format("Fingerprinting %s...", file));
            String result = scanner.wfpFile(file);
            if (result != null && !result.isEmpty()) {
                out.println(result);
                return;
            } else {
                err.println("Warning: No WFP returned.");
            }
        } catch (ScannerException | WinnowingException e) {
            if (parent.debug) {
                e.printStackTrace(err);
            }
            throw e;
        }
        throw new RuntimeException(String.format("Something went wrong while fingerprinting %s", file));
    }

    /**
     * Fingerprint the specified folder and output the results
     *
     * @param folder folder to fingerprint
     */
    private void wfpFolder(String folder) {
        var out = spec.commandLine().getOut();
        var err = spec.commandLine().getErr();
        Scanner scanner = Scanner.builder().build();
        try {
            printMsg(err, String.format("Fingerprinting %s...", folder));
            List<String> results = scanner.wfpFolder(folder);
            if (results != null && !results.isEmpty()) {
                printMsg(err, String.format("Found %d files.", results.size()));
                results.forEach(out::print);
                out.flush();
                return;
            } else {
                err.println("Error: No results return.");
            }
        } catch (ScannerException | WinnowingException e) {
            if (parent.debug) {
                e.printStackTrace(err);
            }
            throw e;
        }
        throw new RuntimeException(String.format("Something went wrong while fingerprinting %s", folder));
    }
}
