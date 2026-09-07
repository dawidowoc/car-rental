plugins {
	`java-library`
}

dependencies {
	testImplementation(platform("org.junit:junit-bom:6.0.3"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testImplementation("org.mockito:mockito-core:5.23.0")
	testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
	testImplementation("org.assertj:assertj-core:3.27.3")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// This module holds the pure domain model. It must stay free of infrastructure
// concerns, so guard the classpath instead of relying on review to catch it.
val forbiddenDependencyPrefixes = listOf(
	"org.springframework",
	"jakarta.persistence",
	"jakarta.servlet",
	"jakarta.validation",
	"org.hibernate",
	"com.fasterxml.jackson",
)

val runtimeDependencyNames: Provider<List<String>> =
	configurations.named("runtimeClasspath")
		.flatMap { it.incoming.artifacts.resolvedArtifacts }
		.map { artifacts -> artifacts.map { it.id.componentIdentifier.displayName } }

val checkDomainIsInfrastructureFree = tasks.register("checkDomainIsInfrastructureFree") {
	group = "verification"
	description = "Fails if the domain model gains a dependency on Spring or other infrastructure."

	val dependencyNames = runtimeDependencyNames
	val prefixes = forbiddenDependencyPrefixes

	doLast {
		val violations = dependencyNames.get()
			.filter { name -> prefixes.any { name.startsWith(it) } }
			.sorted()
		if (violations.isNotEmpty()) {
			throw GradleException(
				"carrental-domain must stay free of infrastructure dependencies, but found:\n" +
					violations.joinToString("\n") { "  - $it" }
			)
		}
	}
}

tasks.named("check") {
	dependsOn(checkDomainIsInfrastructureFree)
}
