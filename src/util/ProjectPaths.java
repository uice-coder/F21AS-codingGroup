package util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves project resources from the repository-style runtime layout.
 * Supported runtime model: run the application from the project root, or from
 * a child directory such as {@code bin/}, while keeping {@code data/} and
 * {@code assets/} as external folders beside the project sources.
 */
public final class ProjectPaths {

    private ProjectPaths() {}

    public static Path resolveExisting(String relativePath) {
        Path resolved = resolve(relativePath);
        if (!Files.exists(resolved)) {
            throw new IllegalStateException(
                    "Required project resource not found: " + relativePath
                    + " (looked near " + Paths.get("").toAbsolutePath() + ")");
        }
        return resolved;
    }

    public static Path resolveOptional(String relativePath) {
        return resolve(relativePath);
    }

    public static Path resolveOutput(String fileName) {
        Path root = findProjectRoot();
        return root.resolve(fileName);
    }

    private static Path resolve(String relativePath) {
        Path root = findProjectRoot();
        return root.resolve(relativePath).normalize();
    }

    private static Path findProjectRoot() {
        Path current = Paths.get("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve("src"))
                    && Files.isDirectory(current.resolve("data"))
                    && Files.isDirectory(current.resolve("assets"))) {
                return current;
            }
            current = current.getParent();
        }
        return Paths.get("").toAbsolutePath().normalize();
    }
}
