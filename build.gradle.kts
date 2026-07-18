plugins {
    id("dev.kikugie.loom-back-compat")
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = property("mod.id") as String

val javaVersion = (property("mod.java") as String).toInt()
val obfuscated = sc.current.version.startsWith("1.")

repositories {
    maven("https://api.modrinth.com/maven") { name = "Modrinth" }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    if (obfuscated) {
        mappings(loom.officialMojangMappings())
        modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
        modCompileOnly("maven.modrinth:modmenu:${property("deps.modmenu")}")
    } else {
        implementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
        compileOnly("maven.modrinth:modmenu:${property("deps.modmenu")}")
    }
    compileOnly("io.github.llamalad7:mixinextras-common:0.5.4")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.4")
}

loom {
    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run")
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = javaVersion
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version.toString(),
        "minecraft" to project.property("mod.mc_compat") as String,
        "java" to javaVersion.toString()
    )
    props.forEach { (key, value) -> inputs.property(key, value) }
    filesMatching(listOf("fabric.mod.json", "chatlookup.mixins.json")) { expand(props) }
}

tasks.jar {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

    inputs.property("version", project.property("mod.version"))
    from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
}
