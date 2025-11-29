This repository currently includes the Gradle wrapper scripts `gradlew` and `gradlew.bat` and
`gradle/wrapper/gradle-wrapper.properties` but does not include the binary `gradle-wrapper.jar`.

Options to make `./gradlew` runnable immediately:

1) Let the wrapper download a Gradle distribution on first run (the repository already contains
   logic in `gradlew` to download a Gradle distribution if the wrapper jar is missing). Ensure
   you have `curl` or `wget` and `unzip` installed, then run:

   ```bash
   chmod +x gradlew
   ./gradlew --version
   ```

2) Generate the wrapper locally (requires a system Gradle):

   If you have `gradle` installed locally you can generate the wrapper JAR by running:

   ```bash
   gradle wrapper
   ```

   This will populate `gradle/wrapper/gradle-wrapper.jar` and make `./gradlew` work without
   a network download.

3) Ask me to add the `gradle-wrapper.jar` binary to the repo now.

   - If you want this, reply `sim, adicione o JAR` and I'll add the binary file to
     `gradle/wrapper/gradle-wrapper.jar`. Note: it is a binary file (~200KB) which will be
     committed into the repository.

If you prefer I add the binary JAR now, reply with `sim, adicione o JAR` and I'll commit it.
