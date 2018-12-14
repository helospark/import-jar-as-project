Import a .jar or .war as a project into you Eclipse IDE, even if you don't have the source by decompiling the jar

You can find it on marketplace here: https://marketplace.eclipse.org/content/import-jar-project-0

## Supported project types

 - Maven
 - PDE Eclipse plugin
 - WAR file
 - Generic Eclipse project

## Usage

 - Install the plugin
 - File->Import->Other->Jar without source
 - Select a jar/war file and click finish
 - New project will be created from the jar

## Goals

 - Validate/audit the content of a jar/war file for potential malicious content
   - Think for example jar files downloaded from Maven central. Most of them are not really validated, any of them can potentially contain malicious content.
   - Even if the library is open source, compiled version could potentially contain code not present in the source repository, Interesting case study is the original Eclipse decompiler plugin: https://0x10f8.wordpress.com/2017/08/07/reverse-engineering-an-eclipse-p...
   - Source attachment of a Maven artifact also cannot be trusted, since it is only uploaded to Maven, it can contain anything the author wants, if your IDE shows the source attachment instead of the decompiled source, you IDE can potentially help to hide the malicious code.
   - Even if the library owner has no malicious intent, build servers can be compromised to include additional code into an otherwise legitimate library
   - Even with valid artifacts tricking caches, MITM and compramising artifactories is possible
   - Via this plugin the actual decompiled source can be pulled into Eclipse, where you can manually study it (or even build it).
 - Continue the deveopment of a project you no longer have source for.

## Limitation

 - Compiled jar lacks some information present in the source code, most importantly many generic types, some local variable names, comments, etc., therefore perfect reproduction of the source code is not always possible and sometimes you will see compilation failure which you have to correct manually. This project uses CFR to decompile, issues with the decompiled code should be reported for: http://www.benf.org/other/cfr/
 - Sometimes Maven artifact refer to SNAPSHOTs built at the same time they were originally built, however this will not be automatically connected, you have to manually have to change the version or also import other dependencies.
 - Sometimes Maven artifacts are missing pom.xml, because they are managed elsewhere, in that case some dependency problems may arise which requires manual fix
