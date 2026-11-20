package com.example.features.map

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.module.http.HttpRequestUtil
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

internal object RamisaBaseMap {
  const val styleUrl = "https://tiles.openfreemap.org/styles/positron"
  const val initialZoom = 11.5
  val dhakaCenter = LatLng(23.7590, 90.3890)

  private var configured = false

  // Both the interactive map and offline manager use this client. OpenFreeMap allows
  // four parallel offline requests and asks apps to identify themselves in the User-Agent.
  fun initialize(context: Context) {
    MapLibre.getInstance(context.applicationContext)
    if (configured) return
    val dispatcher = Dispatcher().apply {
      maxRequests = 4
      maxRequestsPerHost = 4
    }
    HttpRequestUtil.setOkHttpClient(
      OkHttpClient.Builder()
        .dispatcher(dispatcher)
        .addInterceptor { chain ->
          val request = chain.request()
          if (!request.url.isHttps || request.url.host != "tiles.openfreemap.org") {
            throw java.io.IOException("Map resources must use the OpenFreeMap HTTPS endpoint.")
          }
          chain.proceed(request.newBuilder().header("User-Agent", "RAMISA-Android/1.0").build())
        }
        .followRedirects(false)
        .build()
    )
    configured = true
  }
}

internal data class MapPoint(
  val id: String,
  val coordinates: LatLng,
  val color: String,
  val selected: Boolean = false
)

sealed interface MapCameraAction {
  data class Center(val coordinates: LatLng, val minimumZoom: Double) : MapCameraAction
  data class Zoom(val steps: Double) : MapCameraAction
}

@Composable
internal fun LiveSafetyMap(
  points: List<MapPoint>,
  deviceLocation: LatLng?,
  showRiskAreas: Boolean,
  onPointClick: (String) -> Unit,
  onMapLongPress: (LatLng) -> Unit,
  modifier: Modifier = Modifier,
  cameraAction: Pair<Long, MapCameraAction>? = null
) {
  val context = LocalContext.current
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  val pointCallback by rememberUpdatedState(onPointClick)
  val longPressCallback by rememberUpdatedState(onMapLongPress)
  var map by remember { mutableStateOf<MapLibreMap?>(null) }
  var renderReady by remember { mutableStateOf(false) }
  var loadFailed by remember { mutableStateOf(false) }
  var style by remember { mutableStateOf<Style?>(null) }
  var retry by remember { mutableIntStateOf(0) }
  val view = remember(context) {
    RamisaBaseMap.initialize(context)
    MapView(context, MapLibreMapOptions.createFromAttributes(context)
      .textureMode(true)
      .logoEnabled(false)
      .attributionEnabled(true)
      .tiltGesturesEnabled(false)
      .rotateGesturesEnabled(false))
  }

  DisposableEffect(view, lifecycle) {
    var active = true
    var started = false
    var resumed = false
    view.onCreate(null)
    view.addOnDidFailLoadingMapListener { loadFailed = true }
    view.addOnDidFinishRenderingMapListener { fullyRendered ->
      if (fullyRendered) {
        renderReady = true
        loadFailed = false
      }
    }
    fun pause() {
      if (resumed) {
        view.onPause()
        resumed = false
      }
    }
    fun stop() {
      pause()
      if (started) {
        view.onStop()
        started = false
      }
    }
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_START -> {
          view.onStart()
          started = true
        }
        Lifecycle.Event.ON_RESUME -> {
          view.onResume()
          resumed = true
        }
        Lifecycle.Event.ON_PAUSE -> pause()
        Lifecycle.Event.ON_STOP -> stop()
        else -> Unit
      }
    }
    lifecycle.addObserver(observer)
    view.getMapAsync { readyMap ->
      if (active) {
        readyMap.cameraPosition = CameraPosition.Builder()
          .target(RamisaBaseMap.dhakaCenter).zoom(RamisaBaseMap.initialZoom).build()
        readyMap.addOnMapClickListener { location ->
          val pixel = readyMap.projection.toScreenLocation(location)
          val pins = android.graphics.RectF(pixel.x - 20, pixel.y - 20, pixel.x + 20, pixel.y + 20)
          val id = readyMap.queryRenderedFeatures(pins, "ramisa-risk-pins").firstOrNull()
            ?.getStringProperty("zoneId")
          id?.let(pointCallback)
          id != null
        }
        readyMap.addOnMapLongClickListener { location ->
          longPressCallback(location)
          true
        }
        map = readyMap
      }
    }
    onDispose {
      active = false
      lifecycle.removeObserver(observer)
      stop()
      view.onDestroy()
    }
  }

  LaunchedEffect(map, retry) {
    val currentMap = map ?: return@LaunchedEffect
    loadFailed = false
    renderReady = false
    style = null
    currentMap.setStyle(Style.Builder().fromUri(RamisaBaseMap.styleUrl)) { loaded ->
      loaded.addSource(GeoJsonSource("ramisa-risk"))
      loaded.addLayer(CircleLayer("ramisa-risk-areas", "ramisa-risk").withProperties(
        circleColor(get("color")), circleRadius(28f), circleOpacity(0.24f)
      ))
      loaded.addLayer(CircleLayer("ramisa-risk-pins", "ramisa-risk").withProperties(
        circleColor(get("color")), circleRadius(get("pinSize")), circleStrokeColor(Color.WHITE),
        circleStrokeWidth(2f)
      ))
      loaded.addSource(GeoJsonSource("ramisa-device"))
      loaded.addLayer(CircleLayer("ramisa-device-dot", "ramisa-device").withProperties(
        circleColor(Color.rgb(24, 102, 208)), circleRadius(7f), circleStrokeColor(Color.WHITE),
        circleStrokeWidth(2.5f)
      ))
      style = loaded
    }
    delay(15_000)
    if (!renderReady) loadFailed = true
  }

  LaunchedEffect(style, points, deviceLocation, showRiskAreas) {
    val loadedStyle = style ?: return@LaunchedEffect
    val features = points.map { pin ->
      Feature.fromGeometry(Point.fromLngLat(pin.coordinates.longitude, pin.coordinates.latitude)).apply {
        addStringProperty("zoneId", pin.id)
        addStringProperty("color", pin.color)
        addNumberProperty("pinSize", if (pin.selected) 9f else 6f)
      }
    }
    loadedStyle.getSourceAs<GeoJsonSource>("ramisa-risk")
      ?.setGeoJson(FeatureCollection.fromFeatures(features))
    val deviceFeatures = listOfNotNull(deviceLocation?.let {
      Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude))
    })
    loadedStyle.getSourceAs<GeoJsonSource>("ramisa-device")
      ?.setGeoJson(FeatureCollection.fromFeatures(deviceFeatures))
    loadedStyle.getLayer("ramisa-risk-areas")?.setProperties(circleOpacity(if (showRiskAreas) 0.24f else 0f))
  }

  LaunchedEffect(map, cameraAction) {
    val currentMap = map ?: return@LaunchedEffect
    when (val action = cameraAction?.second) {
      is MapCameraAction.Center -> {
        val zoom = maxOf(currentMap.cameraPosition.zoom, action.minimumZoom)
        currentMap.animateCamera(CameraUpdateFactory.newLatLngZoom(action.coordinates, zoom))
      }
      is MapCameraAction.Zoom -> currentMap.animateCamera(CameraUpdateFactory.zoomBy(action.steps))
      null -> Unit
    }
  }

  val mapVisible = renderReady && style != null && !loadFailed
  Box(if (mapVisible) modifier.testTag("openfreemap_ready") else modifier) {
    AndroidView(factory = { view }, modifier = Modifier.fillMaxSize())
    if (!mapVisible) {
      Surface(
        modifier = Modifier.align(Alignment.Center).padding(24.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 3.dp
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (loadFailed) {
            Text("Map tiles unavailable", modifier = Modifier.testTag("map_tiles_unavailable"))
            Text("Try an internet connection or a downloaded area.", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { retry++ }) { Text("Retry") }
          } else {
            CircularProgressIndicator()
            Text("Loading map", style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    }
  }
}
