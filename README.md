# Jenkins Folder Properties Plugin

A Jenkins plugin which allows users with config permission to define properties for a folder which can then be used by
any jobs contained within it or any of its sub-folders.

In structures where two or more folders are nested, any property defined for a folder will be overridden by any other
property of the same name defined by one of its sub-folders.

Freestyle jobs must opt into the `Folder Properties` build wrapper from the `Build Environment` section of their
configuration page in order to be able to access these properties as they would any other environment variable.

    echo $FOO

Pipeline jobs can use step `withFolderProperties` to access them:

    withFolderProperties{
        echo("Test 3: ${env.FOO}")
    }

Jenkins deployments using some of the older versions of the
[Structs Plugin](https://wiki.jenkins.io/display/JENKINS/Structs+plugin) will need to do this using the `wrap` meta-step:


    wrap([$class: 'ParentFolderBuildWrapper']) {
        echo("Test 2: ${env.FOO}")
    }

**Author:** Miguelángel Fernández Mendoza.