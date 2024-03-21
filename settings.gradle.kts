pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url  = uri("https://jcenter.bintray.com") // corrected typo from bintary to bintray
        }
        maven ( "https://jitpack.io" )// no need for parentheses around the URL

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url  = uri("https://jcenter.bintray.com") // corrected typo from bintary to bintray
        }
        maven ( "https://jitpack.io" )// no need for parentheses around the URL
    }
}

rootProject.name = "WeatherApp"
include(":app")
 