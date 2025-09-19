val includeFrontend =
    (System.getenv("INCLUDE_ANDROID")
        ?: findProperty("includeAndroid") as? String ?: "true").toBoolean()

