dependencies {
    implementation("com.tngtech.archunit:archunit")
    testImplementation(project(":common-contract"))
    testImplementation("org.springframework:spring-tx")
    testImplementation("org.springframework.data:spring-data-jpa")
}
