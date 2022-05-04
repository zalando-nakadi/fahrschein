# Releasing a new version of Fahrschein

Fahrschein is released as maven artifacts in the `org.zalando` organisation. Follow these steps to create and publish a new maven release using the prepared Github workflows:

* Create a branch and bump the version in `pom.xml` to the release version:

`NEW_VERSION=0.22.0; mvn scm:check-local-modification versions:set -DnewVersion=$NEW_VERSION scm:add -Dincludes="**/pom.xml" scm:checkin -Dmessage="Release $NEW_VERSION"` 

* Create and merge the PR.

* Create a [new release in Github](https://github.com/zalando-nakadi/fahrschein/releases/new) and tag the latest master with the release version.

* Wait for the Github workflow to succeed, as this should publish the release artifacts to `oss.sonatype.org`.

* Create and merge another PR bumping the version string back to `-SNAPSHOT`.

`NEW_VERSION=0.23.0-SNAPSHOT; mvn scm:check-local-modification versions:set -DnewVersion=$NEW_VERSION scm:add -Dincludes="**/pom.xml" scm:checkin -Dmessage="Start development of $NEW_VERSION"`

