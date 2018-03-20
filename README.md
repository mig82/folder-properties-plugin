# Jenkins Folder Properties Plugin

A Jenkins plugin which allows users with config permission to define properties for a folder which can then be used by
any jobs contained within it or any of its sub-folders.

The aim here is to remove the need to specify the same properties over and over again for all the jobs inside a folder.

In structures where two or more folders are nested, any property defined for a folder will be overridden by any other property of the same name defined by one of its sub-folders.

## Develop

For ease of development I've added a `logging.properties` file that can be used by specifying it as an option to the
Maven `hpi:run` target like so:

    mvn hpi:run -Djava.util.logging.config.file=logging.properties

## Package

Since this is my first version of this and I'm still missing the tests...

    mvn package -DskipTests=true

## Use

In structures where two or more folders are nested, any property defined for a folder will be overridden by any other
property of the same name defined by one of its sub-folders.

Freestyle jobs must opt into the `Folder Properties` build wrapper from the `Build Environment` section of their
configuration page in order to be able to access these properties as they would any other environment variable.

    echo $FOO

Pipeline jobs can use step `withFolderProperties` to access them:

    withFolderProperties{
        echo("Foo: ${env.FOO}")
    }

Jenkins deployments using some of the older versions of the
[Structs Plugin](https://wiki.jenkins.io/display/JENKINS/Structs+plugin) will need to do this using the `wrap` meta-step:


    wrap([$class: 'ParentFolderBuildWrapper']) {
        echo("Foo: ${env.FOO}")
    }


**Author:** Miguelángel Fernández Mendoza.

## References

Site: https://mig82.github.io/folder-properties-plugin

Dependencies: https://mig82.github.io/folder-properties-plugin/doc/dependencies.html

Javadoc: https://mig82.github.io/folder-properties-plugin/apidocs

