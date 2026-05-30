package com.example.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun GoogleMapWebView(
    viewModel: AmbunalGoViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val pickupLat by viewModel.pickupManualLat.collectAsState()
    val pickupLng by viewModel.pickupManualLng.collectAsState()
    val dropoffLat by viewModel.dropoffManualLat.collectAsState()
    val dropoffLng by viewModel.dropoffManualLng.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(310.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF070B12))
            .border(2.dp, Color(0xFF2979FF).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        allowFileAccess = true
                        allowContentAccess = true
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        // Custom User Agent to ensure bypass of OSM CDN blocks or standard request security
                        userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                    }
                    webChromeClient = object : android.webkit.WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                            android.util.Log.d("MapWebView", consoleMessage?.message() ?: "")
                            return true
                        }
                    }
                    setOnTouchListener { v, event ->
                        when (event.action) {
                            android.view.MotionEvent.ACTION_DOWN,
                            android.view.MotionEvent.ACTION_MOVE -> {
                                v.parent?.requestDisallowInterceptTouchEvent(true)
                            }
                            android.view.MotionEvent.ACTION_UP,
                            android.view.MotionEvent.ACTION_CANCEL -> {
                                v.parent?.requestDisallowInterceptTouchEvent(false)
                            }
                        }
                        false
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            evaluateJavascript("if (typeof updatePickupPosition === 'function') { updatePickupPosition($pickupLat, $pickupLng); }", null)
                            evaluateJavascript("if (typeof updateDropoffPosition === 'function') { updateDropoffPosition($dropoffLat, $dropoffLng); }", null)
                            evaluateJavascript("if (typeof focusOn === 'function') { focusOn($pickupLat, $pickupLng); }", null)
                        }
                    }

                    // Native Java-to-JS bridge interface
                    class MapJSInterface {
                        @android.webkit.JavascriptInterface
                        fun onPickupChanged(lat: Double, lng: Double) {
                            coroutineScope.launch {
                                viewModel.updatePickupCoordinates(lat, lng)
                                viewModel.reverseGeocodeLocation(lat, lng, isPickup = true, context = context)
                            }
                        }

                        @android.webkit.JavascriptInterface
                        fun onDropoffChanged(lat: Double, lng: Double) {
                            coroutineScope.launch {
                                viewModel.updateDropoffCoordinates(lat, lng)
                                viewModel.reverseGeocodeLocation(lat, lng, isPickup = false, context = context)
                            }
                        }

                        @android.webkit.JavascriptInterface
                        fun onDistanceCalculated(km: Double) {
                            coroutineScope.launch {
                                viewModel.updateRealRoadDistance(km)
                            }
                        }

                        @android.webkit.JavascriptInterface
                        fun getSetupMode(): String {
                            return viewModel.pinSetupMode.value
                        }
                    }

                    addJavascriptInterface(MapJSInterface(), "AndroidBridge")

                    val mapHtmlContent = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
                            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
                            <style>
                                body, html, #map { margin: 0; padding: 0; height: 100%; width: 100%; background: #070B12; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
                                .leaflet-control-attribution { display: none !important; }
                                .leaflet-touch .leaflet-bar {
                                    border: 2px solid #2979FF !important;
                                    border-radius: 10px !important;
                                    background-color: #101B2B !important;
                                    overflow: hidden;
                                    box-shadow: 0 4px 12px rgba(0,0,0,0.3);
                                }
                                .leaflet-touch .leaflet-bar a {
                                    background-color: #101B2B !important;
                                    color: #00E676 !important;
                                    border-bottom: 1px solid #1B2C46 !important;
                                    font-weight: 800;
                                }
                                /* Vibrant modern map markers */
                                .custom-marker-green {
                                    background-color: #00E676;
                                    border: 2.5px solid white;
                                    border-radius: 50%;
                                    box-shadow: 0 0 12px rgba(0,230,118,0.8), inset 0 0 4px rgba(0,0,0,0.35);
                                }
                                .custom-marker-red {
                                    background-color: #FF1744;
                                    border: 2.5px solid white;
                                    border-radius: 50%;
                                    box-shadow: 0 0 12px rgba(255,23,68,0.8), inset 0 0 4px rgba(0,0,0,0.35);
                                }
                            </style>
                        </head>
                        <body>
                            <div id="map"></div>

                            <script>
                                // Initialize Map with standard coordinate (Jakarta center)
                                var map = L.map('map', { zoomControl: true }).setView([-6.2088, 106.8456], 13);
                                
                                // Standard open source OpenStreetMap tile layer - universally reliable and never blocked
                                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                                    maxZoom: 19,
                                    attribution: 'Map data &copy; OpenStreetMap contributors'
                                }).addTo(map);

                                map.zoomControl.setPosition('bottomright');

                                var greenIcon = L.divIcon({ className: 'custom-marker-green', iconSize: [18, 18], iconAnchor: [9, 9] });
                                var redIcon = L.divIcon({ className: 'custom-marker-red', iconSize: [18, 18], iconAnchor: [9, 9] });

                                var pickupMarker = L.marker([-6.2088, 106.8456], { icon: greenIcon, draggable: true }).addTo(map);
                                var dropoffMarker = L.marker([-6.1963, 106.8475], { icon: redIcon, draggable: true }).addTo(map);

                                // Map click to set marker position dynamically
                                map.on('click', function(e) {
                                    var pos = e.latlng;
                                    var setupMode = AndroidBridge.getSetupMode();
                                    if (setupMode === "PICKUP") {
                                        pickupMarker.setLatLng(pos);
                                        AndroidBridge.onPickupChanged(pos.lat, pos.lng);
                                    } else {
                                        dropoffMarker.setLatLng(pos);
                                        AndroidBridge.onDropoffChanged(pos.lat, pos.lng);
                                    }
                                    updateRoute();
                                });

                                // Beautiful routing line - Google Blue color
                                var routeLine = L.polyline([pickupMarker.getLatLng(), dropoffMarker.getLatLng()], {
                                    color: '#2979FF',
                                    weight: 6,
                                    opacity: 0.95
                                }).addTo(map);

                                function updateRoute() {
                                    var pLat = pickupMarker.getLatLng().lat;
                                    var pLng = pickupMarker.getLatLng().lng;
                                    var dLat = dropoffMarker.getLatLng().lat;
                                    var dLng = dropoffMarker.getLatLng().lng;

                                    // Real Road routing query using Project OSRM (Open Source Routing Machine) API
                                    var url = 'https://router.project-osrm.org/route/v1/driving/' + pLng + ',' + pLat + ';' + dLng + ',' + dLat + '?overview=full&geometries=geojson';
                                    
                                    fetch(url)
                                        .then(function(res) { return res.json(); })
                                        .then(function(data) {
                                            if (data && data.routes && data.routes.length > 0) {
                                                var route = data.routes[0];
                                                var coordinates = route.geometry.coordinates;
                                                
                                                // Convert OSRM [lon, lat] format back to Leaflet [lat, lon]
                                                var latLngs = coordinates.map(function(c) {
                                                    return [c[1], c[0]];
                                                });
                                                
                                                routeLine.setLatLngs(latLngs);
                                                routeLine.setStyle({
                                                    color: '#2979FF',
                                                    weight: 6.5,
                                                    opacity: 0.95,
                                                    dashArray: null
                                                });

                                                // Send the road-based accurate traveling distance (KM) back to our Android app Model
                                                var distKm = parseFloat((route.distance / 1000.0).toFixed(1));
                                                AndroidBridge.onDistanceCalculated(distKm);
                                            } else {
                                                // Fallback to straight dashed line if OSRM is busy/unavailable
                                                routeLine.setLatLngs([pickupMarker.getLatLng(), dropoffMarker.getLatLng()]);
                                                routeLine.setStyle({
                                                    color: '#FF1744',
                                                    weight: 5,
                                                    opacity: 0.8,
                                                    dashArray: '8, 8'
                                                });
                                            }
                                        })
                                        .catch(function(err) {
                                            // Fallback
                                            routeLine.setLatLngs([pickupMarker.getLatLng(), dropoffMarker.getLatLng()]);
                                            routeLine.setStyle({
                                                color: '#FF1744',
                                                weight: 5,
                                                opacity: 0.8,
                                                dashArray: '8, 8'
                                            });
                                        });
                                }

                                pickupMarker.on('dragend', function(e) {
                                    var pos = pickupMarker.getLatLng();
                                    AndroidBridge.onPickupChanged(pos.lat, pos.lng);
                                    updateRoute();
                                });

                                dropoffMarker.on('dragend', function(e) {
                                    var pos = dropoffMarker.getLatLng();
                                    AndroidBridge.onDropoffChanged(pos.lat, pos.lng);
                                    updateRoute();
                                });

                                function updatePickupPosition(lat, lng) {
                                    if (typeof pickupMarker === 'undefined' || !pickupMarker) return;
                                    var cur = pickupMarker.getLatLng();
                                    var diffLat = Math.abs(cur.lat - lat);
                                    var diffLng = Math.abs(cur.lng - lng);
                                    if (diffLat > 0.0001 || diffLng > 0.0001) {
                                        pickupMarker.setLatLng([lat, lng]);
                                        updateRoute();
                                        if (diffLat > 0.005 || diffLng > 0.005) {
                                            map.setView([lat, lng], 14);
                                        }
                                    }
                                }

                                function updateDropoffPosition(lat, lng) {
                                    if (typeof dropoffMarker === 'undefined' || !dropoffMarker) return;
                                    var cur = dropoffMarker.getLatLng();
                                    var diffLat = Math.abs(cur.lat - lat);
                                    var diffLng = Math.abs(cur.lng - lng);
                                    if (diffLat > 0.0001 || diffLng > 0.0001) {
                                        dropoffMarker.setLatLng([lat, lng]);
                                        updateRoute();
                                        if (diffLat > 0.005 || diffLng > 0.005) {
                                            map.setView([lat, lng], 14);
                                        }
                                    }
                                }

                                function focusOn(lat, lng) {
                                    if (typeof map === 'undefined' || !map) return;
                                    map.setView([lat, lng], 14);
                                }

                                // Initial load route calculations
                                setTimeout(updateRoute, 800);
                            </script>
                        </body>
                        </html>
                    """.trimIndent()

                    loadDataWithBaseURL("https://www.openstreetmap.org", mapHtmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.evaluateJavascript("if (typeof updatePickupPosition === 'function') { updatePickupPosition($pickupLat, $pickupLng); }", null)
                webView.evaluateJavascript("if (typeof updateDropoffPosition === 'function') { updateDropoffPosition($dropoffLat, $dropoffLng); }", null)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Floating "Locate Me" GPS target button, positioned overlay top-right like official Google Maps
        IconButton(
            onClick = { viewModel.useCurrentLocationForPickup() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
                .size(44.dp)
                .background(Color(0xFF101B2B), shape = RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFF00E676).copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp))
                .testTag("floating_gps_locate_button")
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Gunakan Lokasi Terkini",
                tint = Color(0xFF00E676),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
