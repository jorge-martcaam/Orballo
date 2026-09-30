# Plan de Correccións e Estabilidade de Orballo

Comprobación e aliñamento técnico tras a revisión da aplicación. A execución organízase en dúas fases graduadas para minimizar o risco de regresión.

---

## 1. Fase 1: Fiabilidade, Limpeza e Navegación (Inmediata)

### Punto 1: Eliminación de datos falsos e xestión de erro real
* **Obxectivo:** Garantir que a aplicación nunca amose temperaturas nin métricas meteorolóxicas inventadas cando falla a rede ou cae o servizo de MeteoGalicia / Open-Meteo.
* **Ficheiros a modificar:**
  - `app/src/main/java/com/example/data/repository/WeatherRepository.kt`
  - `app/src/main/java/com/example/ui/WeatherViewModel.kt`
  - `app/src/main/java/com/example/ui/WeatherScreen.kt`
* **Accións:**
  1. Suprimir o bloque de construción de `CurrentWeatherDto` con valores constantes (16.0°C, 78% humidade, etc.) en `buildOpenMeteoFallbackWeather`.
  2. Lanzar `WeatherFetchException` cando ambos provedores (MeteoGalicia e Open-Meteo) sexan inaccesibles.
  3. Propagar `WeatherUiState.Error` con descrición factual en galego e botón de acción "Tentar de novo" (`onRetry`) que reexecuta `loadDataForLocation`.

### Punto 3: Eliminación do subsistema GPS
* **Obxectivo:** Suprimir o código morto e evitar deixar un botón de localización incompleto.
* **Ficheiros a modificar / eliminar:**
  - `app/src/main/AndroidManifest.xml` (eliminar permisos `ACCESS_FINE_LOCATION` e `ACCESS_COARSE_LOCATION`).
  - `app/src/main/java/com/example/data/location/LocationHelper.kt` (eliminar ficheiro tras confirmación).
  - `app/src/main/java/com/example/ui/WeatherViewModel.kt` (eliminar `tryGpsLocation`).
  - `app/src/main/java/com/example/ui/WeatherScreen.kt` (eliminar botón `Icons.Default.MyLocation` e selector de permisos de localización).
  - `app/src/main/java/com/example/data/model/GaliciaLocation.kt` (eliminar campo e lóxica de `isGps`).

### Punto 5: Widget con soporte de quendas en formato texto compacto (Alternativa B)
* **Obxectivo:** Evitar a desaparición das predicións por franxas (Mañá/Tarde/Noite) en lanzadores con alturas axustadas.
* **Ficheiros a modificar:**
  - `app/src/main/java/com/example/widget/GaliciaWeatherWidgetProvider.kt`
  - `app/src/main/res/layout/widget_galicia_weather_large.xml`
* **Accións:**
  1. Se a altura do widget é suficiente (>= 150dp), amosar as tres tarxetas detalladas actuais.
  2. Se a altura está nun rango intermedio (115dp a 149dp), amosar unha fila compacta con iconas e texto continuo: `Mañá 14° (70%) • Tarde 18° (40%) • Noite 12° (10%)`.
  3. Se a altura é mínima (< 115dp), manter o deseño estándar dunha soa fila (tempo actual).

### Punto 6: Xestión nativa do botón Atrás do sistema (`BackHandler`)
* **Obxectivo:** Evitar que a aplicación se peche accidentalmente ao retroceder dende Histórico ou Axustes.
* **Ficheiros a modificar:**
  - `app/src/main/java/com/example/ui/WeatherScreen.kt`
* **Accións:**
  1. Incorporar `BackHandler(enabled = appPage != AppPage.ACTUAL)` no nivel raíz do composable principal.
  2. Ao activar o evento de retroceso, reverter `appPage` a `AppPage.ACTUAL`.

---

## 2. Fase 2: Persistencia con Room e Descomposición Arquitectónica (Posterior)

### Punto 2: Base de datos local e caché sen conexión (Room)
* **Configuración:**
  - KSP e Room runtime en `app/build.gradle.kts`.
  - Entidade `WeatherCacheEntity` con chave primaria `concelloId`, carga JSON serializada e marca temporal `updatedAtEpoch`.
  - Política de caducidade (TTL): **2 horas** acordadas.
  - Comportamento: Ao solicitar datos, consultar rede en paralelo á lectura de caché. Se a rede falla e a caché ten menos de 2 horas, amosar os datos co distintivo "Última predición gardada (hai X min) — Sen conexión".

### Punto 4: Modularización de `WeatherScreen.kt` (<500 liñas por ficheiro)
* **Estrutura modular resultante:**
  1. `com/example/ui/weather/WeatherTopAppBar.kt`
  2. `com/example/ui/weather/CurrentForecastCard.kt`
  3. `com/example/ui/weather/HourlyForecastSection.kt`
  4. `com/example/ui/weather/SevenDayForecastSection.kt`
  5. `com/example/ui/weather/HistoricalTabContent.kt`
  6. `com/example/ui/WeatherScreen.kt` (Orquestrador reducido a ~180 liñas).

---

## 3. Criterios de Éxito e Verificación
* Compilación limpa mediante `compile_applet` sen fallos nin advertencias.
* Verificación da ausencia total de datos meteorolóxicos simulados de 16°C.
* Navegación fluída sen peches involuntarios mediante xesto Atrás.
* Eliminación limpa de dependencias e permisos de GPS sen código orfo.
