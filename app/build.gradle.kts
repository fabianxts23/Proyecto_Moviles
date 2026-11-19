plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
}

android {
    namespace = "cl.duoc.oportunidades"
    compileSdk = 34

    defaultConfig {
        applicationId = "cl.duoc.oportunidades"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    // Compose + AndroidX
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Consumo de la API
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)

    // Navegación entre pantallas
    implementation(libs.androidx.navigation.compose)

    // Base de datos local (Room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Vista previa de fotos
    implementation(libs.coil.compose)
    testImplementation("junit:junit:4.13.2")
}
kover {
    reports {
        filters {
            includes {
                classes(
                    "cl.duoc.oportunidades.domain.validation.Validators*",
                    "cl.duoc.oportunidades.domain.validation.FormularioValidator*",
                    "cl.duoc.oportunidades.data.model.ReglasEstado*",
                    "cl.duoc.oportunidades.data.model.EstadoOportunidad*",
                    "cl.duoc.oportunidades.data.UsuarioRepository*",
                    "cl.duoc.oportunidades.data.UsuarioDto*",
                    "cl.duoc.oportunidades.data.OrganizacionDto*",
                    "cl.duoc.oportunidades.data.OrganizacionSync*",
                    "cl.duoc.oportunidades.data.OrganizacionRemotaRepository*",
                    "cl.duoc.oportunidades.data.local.Converters*"
                )
            }
        }
    }
}