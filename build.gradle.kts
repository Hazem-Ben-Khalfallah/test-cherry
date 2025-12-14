fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(properties("javaVersion").toInt()))
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("org.jetbrains:annotations:24.0.1")
    
    intellijPlatform {
        val platformType = properties("platformType")
        val platformVersion = properties("platformVersion")
        val platformPlugins = properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty)
        
        // For 2025.3+, use intellijIdea() instead of intellijIdeaCommunity()
        when (platformType) {
            "IC" -> intellijIdea(platformVersion)
            "IU" -> intellijIdeaUltimate(platformVersion)
            else -> intellijIdea(platformVersion)
        }
        
        if (platformPlugins.isNotEmpty()) {
            bundledPlugins(*platformPlugins.toTypedArray())
        }
    }
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")
    }

    buildSearchableOptions {
        isEnabled = false
    }
}
