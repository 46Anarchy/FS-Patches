package dev.anarchy.fspatches.updater;

import lombok.Getter;

import java.util.List;

@Getter
public class DownloadCouldntCompleteException extends Exception {

    private final String fileName;
    private final List<String> errors;

    public DownloadCouldntCompleteException(
            String fileName,
            List<String> errors
    ) {
        super("Failed to download: " + fileName);

        this.fileName = fileName;
        this.errors = errors;
    }
}
