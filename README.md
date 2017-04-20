Digits Android Auth Migrator
======================

An Android library for migrating logged in users from the Digits SDK to the new
Firebase SDK.

Pre-requisites
--------------
TODO(ashwinraghav): Link to Migration Doc

Before using this code, you must add the [Firebase/Auth modules](https://firebase.google.com/docs/auth/android/start/) to your project.

Getting Started
---------------
1. To you app module's build.gradle, add:
```groovy
compile 'digitsmigrator'
```
2. In your Application's or main Activity's `onCreate()` override,
   call `migrate()` to log in any user who was previously logged in with the
   legacy SDK.
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
                //an error accured
            }
        }
});
```
//TODO(ashwinraghav) Link here
To see a detailed example, see our sample migration app.

Support
-------
TODO (ashwinraghav)

Contributing
-------
TODO (ashwinraghav)

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
