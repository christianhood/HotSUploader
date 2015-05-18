package com.metacodestudio.hotsuploader.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.metacodestudio.hotsuploader.providers.Provider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReplayFile {

    private final File file;

    private final List<UploadStatus> uploadStatuses;

    public ReplayFile(final File file) {
        uploadStatuses = new ArrayList<>();
        this.file = file;
    }

    public static List<ReplayFile> fromDirectory(File file) {
        List<ReplayFile> replayFiles = new ArrayList<>();
        File[] children = file.listFiles((dir, name) -> name.endsWith(".StormReplay"));
        for (final File child : children) {
            if (child.isDirectory()) {
                replayFiles.addAll(fromDirectory(child));
            } else {
                replayFiles.add(new ReplayFile(child));
            }
        }

        return replayFiles;

    }

    @Override
    public String toString() {
        return file.getName();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ReplayFile that = (ReplayFile) o;

        return !(file != null ? !file.equals(that.file) : that.file != null);

    }

    @Override
    public int hashCode() {
        return file != null ? file.hashCode() : 0;
    }

    @JsonIgnore
    public Status getStatus() {
        if (uploadStatuses.size() < 1) {
            System.out.println("Empty status list");
            return Status.NEW;
        }

        List<Provider> providers = Provider.getAll();
        for(Provider provider : providers) {
            if (provider.isEnabled()) {
                Status providerStatus = getStatusByProvider(provider);

                if (providerStatus != Status.UPLOADED) {
                    return providerStatus;
                }
            }
        }

        return Status.UPLOADED;
    }


    @JsonIgnore
    public Status getStatusByProvider(String providerName ) {
        UploadStatus status = uploadStatuses.stream()
                .filter(uploadStatus -> uploadStatus.getHost().equals(providerName))
                .findFirst()
                .orElse(null);

        if (status == null)
            return Status.NEW;

        return status.getStatus();
    }

    @JsonIgnore
    public Status getStatusByProvider(Provider provider ) { return getStatusByProvider(provider.getName()); }

    public File getFile() {
        return file;
    }

    public void addStatuses(final List<UploadStatus> list) {
        uploadStatuses.addAll(list);
    }

    public List<UploadStatus> getUploadStatuses() {
        return uploadStatuses;
    }
}
