# Digits Android Auth Migrator

An Android library for migrating logged in users from the Digits SDK to the new
Firebase SDK.

## Pre-requisites

Please see our [Migration Doc](https://firebase.google.com/support/guides/digits-android) for full migration steps. 

Before using this code, you must add the [Firebase/Auth modules](https://firebase.google.com/docs/auth/android/start/) to your project.

## Getting Started

1. Add the following to your app module's build.gradle:
```groovy
compile 'com.google.firebase:firebase-auth:11.0.0'
compile 'com.firebase:digitsmigrationhelpers:0.1.3'
```
2. In your Application's or Main Activity's `onCreate()` method,
   call the `migrate()` method to seamlessly log in any user who had a valid Digits session.
```java
AuthMigrator.getInstance().migrate()
.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
                if (task.getResult().getUser() != null) {
                    // Either your existing user remains logged in
                    // or a FirebaseUser was created from your 
                    // digits Auth state.
                } else {
                    //No valid digits session was found
                }
            } else {
                // An error occurred in attempting to migrate the session
            }
        }
});
```
3. Remove the Digits sdk and associated code.

## Support

Please go to [Firebase support](https://firebase.google.com/support/) if you need help.

## Contributing

### Installing locally

You can download the migration helper and install it locally by cloning this
repository and running:

    ./gradlew :library:prepareArtifacts :library:publishAllToMavenLocal

###  Deployment

To deploy FirebaseUI to Bintray

  1. Set `BINTRAY_USER` and `BINTRAY_KEY` in your environment. You must
     be a member of the digits migration Bintray organization.
  1. Run `./gradlew clean :library:prepareArtifacts :library:bintrayUploadAll`
  1. Go to the Bintray dashboard and click 'Publish'.
    1. In Bintray click the 'Maven Central' tab and publish the release.

### Tag a release on GitHub

* Ensure that all your changes are on master and that your local build is on master
* Ensure that the correct version number is in `common/constants.gradle`

### Contributor License Agreements

We'd love to accept your sample apps and patches! Before we can take them, we
have to jump a couple of legal hurdles.

Please fill out either the individual or corporate Contributor License Agreement
(CLA).

  * If you are an individual writing original source code and you're sure you
    own the intellectual property, then you'll need to sign an
    [individual CLA](https://developers.google.com/open-source/cla/individual).
  * If you work for a company that wants to allow you to contribute your work,
    then you'll need to sign a
    [corporate CLA](https://developers.google.com/open-source/cla/corporate).

Follow either of the two links above to access the appropriate CLA and
instructions for how to sign and return it. Once we receive it, we'll be able to
accept your pull requests.

### Contribution Process

1. Submit an issue describing your proposed change to the repo in question.
1. The repo owner will respond to your issue promptly.
1. If your proposed change is accepted, and you haven't already done so, sign a
   Contributor License Agreement (see details above).
1. Fork the desired repo, develop and test your code changes.
1. Ensure that your code adheres to the existing style of the library to which
   you are contributing.
1. Ensure that your code has an appropriate set of unit tests which all pass.
1. Submit a pull request and cc @puf or @samtstern

License
-------

Copyright 2016 Google, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
