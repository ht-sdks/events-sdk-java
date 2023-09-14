# JitPack Releasing

1.  Update the `<version></version>` nodes in all pom.xml files.
1.  Verify everything passes CI and github actions.
1.  Merge to `main`.
1.  Tag commit on `main` using the version name (e.g. semantic versioning X.Y.Z)
1.  Push tags to github
1.  Visit https://jitpack.io/#ht-sdks/events-sdk-java/ and refresh until the `Version` tab shows your new tag. Click `Get it`. Click the spinner next to `Get it` and wait for the request to return the build.log. Ensure that the build succeeded and created new assets with your desired tags.

## Snapshot

In addition to git's tagged versions, you can point JitPack at specifc commits or branches.
