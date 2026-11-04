This is a Kotlin Multiplatform project targeting Android.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
---

## Leer el medidor con la cámara (OCR)

Disponible para los roles **Director** y **Responsable de lecturas** (botón "📷 Leer medidor con la cámara" en la pantalla de inicio).

1. Se elige si es la lectura del **viernes** (cierre) o del **lunes** (inicio).
2. Se toma la foto con la app de cámara del sistema y **ML Kit** reconoce el texto en el celular, sin internet.
3. `ExtractorLectura` propone los números que parecen la lectura; el usuario elige o corrige el valor.
4. Al tocar **Guardar lectura** se envía al backend con `POST /lecturas` (con el token de la sesión). Si el servidor la acepta, también se guarda en el teléfono.

Archivos de la función:

```
shared/src/commonMain/.../service/LectorMedidor.kt      Contrato de la cámara (lo implementa androidApp)
shared/src/commonMain/.../service/ExtractorLectura.kt   Busca la lectura dentro del texto del OCR
shared/src/commonMain/.../service/Fecha.kt              fechaHoy() (expect) + androidMain/.../Fecha.android.kt (actual)
shared/src/commonMain/.../ui/captura/                   CapturaScreen y CapturaViewModel
shared/src/commonMain/.../data/remote/EcovoltApi.kt     enviarLectura() → POST /lecturas
androidApp/.../AndroidLectorMedidor.kt                  Cámara del sistema + ML Kit
androidApp/src/main/res/xml/file_paths.xml              Carpeta temporal de la foto (FileProvider)
```

Requiere el backend con la ruta `POST /lecturas` (EcoVolt - BackEnd).

---

## Reportes de desperdicio o falla (RFN-19)

Disponible para **todos los roles** (botón "📝 Reportar desperdicio o falla" en la pantalla de inicio).

1. Se elige el tipo: **Desperdicio** (luces o equipos encendidos sin uso) o **Falla** (algo descompuesto).
2. Se escribe el **área** (hay botones rápidos: Salón, Baños, Laboratorio...) y una **descripción** (10 a 500 caracteres).
3. Se agrega una **foto** con la cámara o desde la galería. `AndroidSelectorFoto` la reduce a 1280 px y la comprime en JPG (~200 KB).
4. Al tocar **Enviar reporte** se manda al backend con `POST /reportes` (la foto va en Base64 dentro del JSON).
5. En la lista, el **Director** y el **Responsable** ven todos los reportes con el nombre de quien los envió; los demás ven solo los suyos. La foto se descarga al tocar "Ver foto" (`GET /reportes/{id}/foto`).

```
shared/src/commonMain/.../model/Reporte.kt                 TipoReporte y Reporte
shared/src/commonMain/.../service/SelectorFoto.kt          Contrato para conseguir la foto (lo implementa androidApp)
shared/src/commonMain/.../service/Imagen.kt                ByteArray.aImagen() (expect) + androidMain/.../Imagen.android.kt (actual)
shared/src/commonMain/.../data/remote/ReporteDto.kt        JSON de /reportes
shared/src/commonMain/.../data/repository/ReporteRepository.kt
shared/src/commonMain/.../ui/reportes/                     ReportesScreen (lista) y NuevoReporteScreen (formulario) con sus ViewModels
androidApp/.../AndroidSelectorFoto.kt                      Cámara / galería del sistema, reducir y comprimir la foto
```

## Consejos de ahorro (RFN-25)

Disponible para **todos los roles** (botón "💡 Consejos de ahorro").

- Pestañas **Escuela** y **Hogar**. Los consejos vienen del backend (`GET /consejos`) y se guardan en el teléfono para verlos sin internet.
- En la pestaña Escuela aparece una **recomendación con datos reales**: con las lecturas guardadas se calcula el consumo del último fin de semana; si pasa el umbral de `ConsumoService.hayConsumoFantasma` (5 kWh) se avisa de **consumo fantasma**.

```
shared/src/commonMain/.../model/Consejo.kt                 AmbitoConsejo y Consejo
shared/src/commonMain/.../data/remote/ConsejoDto.kt        JSON de /consejos
shared/src/commonMain/.../data/local/ConsejoStorage.kt     Contrato + androidMain/.../SharedPrefsConsejoStorage.kt
shared/src/commonMain/.../data/repository/ConsejoRepository.kt
shared/src/commonMain/.../ui/consejos/                     ConsejosScreen y ConsejosViewModel
```

Ambos módulos requieren el backend con las rutas `/reportes` y `/consejos` (EcoVolt - BackEnd).
