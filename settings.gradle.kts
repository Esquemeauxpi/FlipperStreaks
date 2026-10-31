import java.util.Properties

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

val sdkRegistryToken: String = run {
    val props = Properties()
    java.io.File(gradle.gradleUserHomeDir, "gradle.properties")
        .takeIf { it.exists() }
        ?.inputStream()?.use { props.load(it) }
    props.getProperty("SDK_REGISTRY_TOKEN", "")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication { create<BasicAuthentication>("basic") }
            credentials {
                username = "mapbox"
                password = sdkRegistryToken
            }
        }
    }
}

rootProject.name = "FlipperStreaks"
include(":app")
