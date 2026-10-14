group = "app.action"

patches {
    about {
        name = "Action Dark Mode Patches"
        description = "Morphe patches for Action app (com.action.consumerapp)"
        source = "https://github.com/janowakps/morphe-patches-action.git"
        author = "janowakps"
        contact = "na"
        website = "https://github.com/janowakps/morphe-patches-action"
        license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
