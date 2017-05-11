# Digits Android Auth Migrator

An Android library for migrating logged in users from the Digits SDK to the new
Firebase SDK.

## Pre-requisites

Please see our [Migration Doc](https://firebase.google.com/support/guides/digits-android) for full migration steps. 

Before using this code, you must add the [Firebase/Auth modules](https://firebase.google.com/docs/auth/android/start/) to your project.

## Getting Started
This sdk handles converting your Digits user into a firebase one, without forcing the user to re-authenticate. If there is an active Digits session on the device, the user will be seamlessly transitioned into a Firebase session. 
This guide provides instructions on how to integrate and test this sdk.

#### Integration
1. Add the following to your app module's build.gradle:
```groovy
compile 'com.google.firebase:firebase-auth:11.0.0'
compile 'com.firebase:digitsmigrationhelpers:0.1.3'
```
2. To your AndroidManifest.xml, add the following entries
```xml
        <meta-data
            android:name="io.fabric.ApiKey"
            android:value="YOUR_FABRIC_API_KEY"
            tools:replace="android:value"/>
        <meta-data
            android:name="com.digits.sdk.android.ConsumerKey"
            android:value="YOUR_DIGITS_CONSUMER_KEY"/>
        <meta-data
            android:name="com.digits.sdk.android.ConsumerSecret"
            android:value="YOUR_DIGITS_CONSUMER_SECRET"/>
```
3. In your Android Application class' s `onCreate()` or Activity's `onCreate()` override, call the migrator's `migrate()` method to seamlessly exchange the default digits session found on the device.

```java
AuthMigrator.getInstance().migrate(!BuildConfig.DEBUG)
.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
                if (task.getResult().getUser() != null) {
                    // Either your existing user remains logged in
                    // or a FirebaseUser was created from your digits Auth state.
                    Log.d("MyApp", "Digits id preserved:" 
                      + authResult.getUser().getUid());
                    Log.d("MyApp", "Digits Phone number preserved: " 
                      + authResult.getUser().getPhoneNumber());
                } else {
                    //No valid digits session was found
                }
            } else {
                // An error occurred in attempting to migrate the session
            }
        }
});
```
**Our sample app shows how the token exchange can be done on app launch in a way that is transparent to the end user. As a best practice, we integrate with [Firebase UI](https://github.com/firebase/FirebaseUI-Android) to authenticate users that do not have digits tokens**.
 
#### Testing
Before the digits sdk can be removed with confidence from your app, we need to simulate a user logged into your app using digits sdk that needs to be migrated to the new firebase sdk.
1. Sign in using the digits sdk flow in your app. This helps simulate an existing user who has signed into Digits in your published app. Our test is to ensure that the user id and phone number of this user are preserved without requiring the user to re-login.
2. Next, complete the [integration](#integration) as shown above. Once the success callbacks have been invoked, you should be able to verify that the firebase user was created using the same id and phone number without requiring any intervention.
In addition, the following logs can be used to verify
```bash
$ adb logcat | grep -i "DigitsAuthMigrator\|MyApp"

05-11 16:14:38.322  3593  3593 D DigitsAuthMigrator: Exchanging digits session
05-11 16:14:40.718  3593  3593 D MyApp   : Digits id preserved:8215196027230
05-11 16:14:40.718  3593  3593 D MyApp   : Digits Phone number preserved+14148981327

```
3. In addition, if your integration in in your Application's `onCreate()` override, restart your app to make sure that repeated invocations of `migrate` are no-ops.
```bash
$ adb logcat | grep -i "DigitsAuthMigrator\|MyApp"

05-11 16:21:43.347  9129  9129 D DigitsAuthMigrator: Found existing firebase session. Skipping Exchange.
05-11 16:21:43.504  9129  9129 D MyApp   : Digits id preserved:821519602702090240
05-11 16:21:43.504  9129  9129 D MyApp   : Digits Phone number preserved+14349873237
```
Note: The `migrate()` method accepts an argument to delete a digits session after the exchange completes. By setting this value to `migrate(!BuildConfig.DEBUG)`, we retain the digits token to facilitate testing in debug builds. In all other builds, the token will be deleted.

At this point, it is safe to remove the digits sdk dependency from your app.

## Support

Please go to [Firebase support](https://firebase.google.com/support/) if you need help.

## Contributing

### Installing locally

You can download the migration helper and install it locally by cloning this
repository and running:

```bash
./gradlew :digitsmigrationhelpers:install
```

###  Deployment

To deploy the migration helpers to Bintray

  1. Set `BINTRAY_USER` and `BINTRAY_KEY` in your environment. You must
     be a member of the firebase Bintray organization.
  2. Run `./gradlew :digitsmigrationhelpers:bintrayUpload`
  3. Go to the Bintray dashboard and click 'Publish'.
  4. In Bintray click the 'Maven Central' tab and publish the release.

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
