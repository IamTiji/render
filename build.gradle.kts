plugins {
    kotlin("jvm") version "2.2.0"
}

group = "com.tiji"
version = "1.0"

repositories {
    mavenCentral()
}

tasks.test {
    failOnNoDiscoveredTests = false
}

dependencies {
    implementation("io.github.kotlin-graphics:glm:0.9.9.1-12")
}

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(23)
}

val lwjglVersion = "3.4.3"
val lwjglNatives = Pair(
	System.getProperty("os.name")!!,
	System.getProperty("os.arch")!!
).let { (name, arch) ->
	when {
		arrayOf("Linux", "SunOS", "Unix").any { name.startsWith(it) } ->
			if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
				"natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
			else if (arch.startsWith("ppc"))
				"natives-linux-ppc64le"
			else if (arch.startsWith("riscv"))
				"natives-linux-riscv64"
			else
				"natives-linux"
		arrayOf("Windows").any { name.startsWith(it) }                ->
			"natives-windows"
		else                                                                            ->
			throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
	}
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-assimp")
    implementation("org.lwjgl:lwjgl-freetype")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-msdfgen")
    implementation("org.lwjgl:lwjgl-openal")
    implementation("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjgl:lwjgl-renderdoc")
    implementation("org.lwjgl:lwjgl-stb")
    implementation("org.lwjgl:lwjgl::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-assimp::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-freetype::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-glfw::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-msdfgen::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-openal::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-opengl::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-stb::$lwjglNatives")
}

val testJar by tasks.registering(Jar::class) {
    archiveClassifier = "tests"
    from(sourceSets.test.get().output)
    from(sourceSets.main.get().output)
}

val testFatJar by tasks.registering(Jar::class) {
    group = "build"
    archiveClassifier.set("tests-all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    dependsOn(tasks.testClasses)

    // Test classes + main classes + all test runtime deps (this includes the Kotlin stdlib)
    from({
        sourceSets.test.get().runtimeClasspath
            .filter { it.exists() }
            .map { if (it.isDirectory) it else zipTree(it) }
    })

    // Strip signatures from signed dependencies, otherwise you get SecurityException at runtime
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    // Optional: make it runnable (e.g. a custom test launcher)
    // manifest { attributes["Main-Class"] = "com.example.TestMain" }
}

tasks.assemble {
    dependsOn(testJar)
    dependsOn(testFatJar)
}
