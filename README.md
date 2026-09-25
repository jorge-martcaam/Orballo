# MeteoGalicia - Tempo en Galicia

Aplicación nativa de predición meteorolóxica para Galicia desenvolvida para Android en **Kotlin** e **Jetpack Compose**. A aplicación prioriza os datos oficiais municipais da axencia autonómica **MeteoGalicia**, complementándoos co modelo numérico de alta resolución **ECMWF IFS** (vía Open-Meteo) para ofrecer información climática de precisión en calquera dos 313 concellos galegos.

---

## 🚀 Capacidades Principais

### 1. Inxestión e Priorización de Datos
- **MeteoGalicia Oficial (Curto Prazo):** Predición municipal oficial por concellos para hoxe e os vindeiros días, con estados de ceo, temperaturas e probabilidade de precipitación validados pola axencia meteorolóxica de Galicia.
- **Modelo ECMWF IFS (Fallback e Medio Prazo):** Integración do modelo europeo de alta precisión para predicións horarias continuas e extensión de previsión.

### 2. Predición por Franxas Horarias Despregables
- Tarxetas visuais independentes e despregables para cada momento do día:
  - **Mañá (06:00 – 12:00)**
  - **Tarde (12:00 – 20:00)**
  - **Noite (20:00 – 06:00)**
- Detalle con estado do ceo, temperaturas máxima e mínima, vento, humidade e probabilidade de chuvia.

### 3. Telemetría Meteorolóxica Actualizada
- Temperatura actual en tempo real e sensación térmica.
- Velocidade media e refachos máximos de vento con compás de dirección.
- Humidade relativa, índice de radiación ultravioleta (UV) e presión atmosférica.
- Predición detallada hora a hora para as seguintes 24 horas.

### 4. Calidade do Aire (ICA)
- Índice de Calidade do Aire oficial desagregado por concello.
- Medición de contaminantes principais:
  - Partículas en suspensión: **PM2.5** e **PM10**.
  - Gases atmosféricos: **NO₂** (dióxido de nitróxeno), **O₃** (ozono) e **SO₂** (dióxido de xofre).
- Indicadores cromáticos de saúde ambiental con recomendacións para actividades ao aire libre.

### 5. Histórico Multianual (Ata 10 Anos)
- Consulta do tempo histórico nunha data determinada ata 10 anos atrás.
- Comparativa térmica e de precipitación histórica para entender tendencias estacionais locais.

### 6. Sistema de Alertas e Avisos Meteorolóxicos
- Monitorización activa de avisos meteorolóxicos adversos con códigos oficiais de risco (Amarelo, Laranxa e Vermello) emitidos por MeteoGalicia.

### 7. Xestión de Localizacións e Favoritos
- **Xeolocalización GPS:** Detección automática do concello actual con cálculo de coordenadas máis próximas.
- **Xestor de Favoritos:** Gardado local de localidades favoritas para acceso instantáneo.

### 8. Widgets de Escritorio Configurables (Android AppWidgets)
- **3 Disposicións Adaptativas:**
  - **Estándar:** Visión xeral con temperatura actual, estado do ceo, máximas/mínimas e vento.
  - **Compacto (2x1):** Deseño minimalista centrado na temperatura e no icono do ceo.
  - **Panorámico (Wide):** Vista horizontal completa con predición por franxas do día.
- **Temas Visuais do Widget:**
  - Azul Océano.
  - Vidro Escuro (Dark Glass).
  - Vidro Claro (Light Glass).
  - Dinámico (adapta a cor e contraste segundo o estado do tempo).
- **Controis Interactivos no Escritorio:**
  - Botón de refresco manual instantáneo.
  - Frechas de navegación bidireccional (⬅ e ➡) para rotar ciclicamente entre as cidades favoritas de xeito independente en cada widget instalado.

---

## 🛠️ Arquitectura e Tecnoloxías

- **Linguaxe:** Kotlin (100% Kotlin).
- **Interface de Usuario:** Jetpack Compose con **Material Design 3 (M3)**.
- **Patrón de Deseño:** MVVM (Model-View-ViewModel) con arquitectura limpa e separación de responsabilidades.
- **Asincronía e Reactividade:** Kotlin Coroutines e StateFlow.
- **Rede e Comunicación API:** Retrofit 2, OkHttp 3 con Gson para deserialización de JSON.
- **Persistencia Local:** Android Room Database e DataStore / SharedPreferences.
- **Widgets:** Android RemoteViews con BroadcastReceivers e PendingIntents específicos.

---

## 📂 Estrutura do Proxecto

```text
app/src/main/
├── java/com/example/
│   ├── data/
│   │   ├── airquality/       # Modelos e repositorios de calidade do aire
│   │   ├── historical/        # Consulta do histórico climático a 10 anos
│   │   ├── location/          # Xestión de concellos, xeolocalización e favoritos
│   │   ├── weather/           # Integracións con MeteoGalicia e Open-Meteo
│   │   └── widget/            # Persistencia de preferencias dos widgets
│   ├── ui/
│   │   ├── components/        # Compoñibles reutilizables (tarxetas de franxas, gráficos)
│   │   ├── screens/           # Pantallas principais (Home, AirQuality, Historical, Settings)
│   │   └── theme/             # Cores, tipografía e formas de Material 3
│   └── widget/                # GaliciaWeatherWidgetProvider e xestión de eventos do widget
└── res/
    ├── drawable/              # Iconografía vectorial e fondos do widget
    ├── layout/                # Layouts XML para RemoteViews dos widgets
    └── values/                # Recursos de cadeas (en galego) e estilos
```

---

## 🔨 Compilación e Instalación

Para compilar o proxecto localmente mediante terminal:

```bash
# Compilar a versión de depuración (Debug APK)
gradle assembleDebug

# Executar os tests unitarios
gradle :app:testDebugUnitTest
```

O ficheiro APK xerado atoparase en:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 Licenza

Distribuído baixo a licenza aberta para fins comunitarios e de divulgación meteorolóxica en Galicia.
