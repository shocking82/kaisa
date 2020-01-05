package com.sap.kaisa

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.Toast

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_kakao_map.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPOIItem.MarkerType.BluePin
import net.daum.mf.map.api.MapPOIItem.MarkerType.RedPin
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapReverseGeoCoder
import net.daum.mf.map.api.MapView
import java.lang.Exception
import java.security.MessageDigest


class KakaoMapActivity : AppCompatActivity(), MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener {

    private val LOG_TAG = "KakaoMapActivity"
    private val GPS_ENABLE_REQUEST_CODE = 2001
    private val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    lateinit var imageView : ImageView

    private lateinit var mapView: MapView
//    lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakao_map)

//        MobileAds.initialize(this) {}
//        mAdView = findViewById(R.id.adView)
//        val adRequest = AdRequest.Builder().build()
//        mAdView.loadAd(adRequest)


//        try {
//            val info = packageManager.getPackageInfo("com.sap.kaisa", PackageManager.GET_SIGNATURES)
//
//            for (sig in info.signatures ){
//
//                val md = MessageDigest.getInstance("SHA")
//                md.update(sig.toByteArray())
//                Log.e("keyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
//
//            }
//        }catch (e : Exception){
//            e.printStackTrace()
//        }

        // GPS 체크
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            buildAlertMessgeNoGps()
        }


        // 맵 생성
        mapView = MapView(this)
        mapView.setCurrentLocationEventListener(this)

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting()
        }else {
            checkRunTimePermission()
        }

        val mapViewContainer = map_view

        mapViewContainer.addView(mapView)


        //현재위치로 이동
//        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(33.3, 127.0),true)
//        mapView.setZoomLevel(4, true)
//        mapView.zoomIn(true)
//
//        //마커 생성
//        val marker = MapPOIItem();
//        marker.itemName = "현재위치"
//        marker.tag = 0
//        marker.mapPoint = MapPoint.mapPointWithGeoCoord(33.3,127.0)
//        marker.markerType = BluePin
//        marker.selectedMarkerType = RedPin
//        mapView.addPOIItem(marker)
    }

    private fun buildAlertMessgeNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    11)
            }
            .setNegativeButton("No") {dialog, id ->
                dialog.cancel()
                finish()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff)
        mapView.setShowCurrentLocationMarker(false)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size){
            var check_result = true

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false
                    break
                }
            }

            if (check_result) {
                Log.d("@@@", "start")
                //위치 값을 가져올 수 있음
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading)
            } else { // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        REQUIRED_PERMISSIONS[0]
                    )
                ) {
                    Toast.makeText(
                        this,
                        "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showDialogForLocationServiceSetting() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
            "앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                    + "위치 설정을 수정하실래요?"
        )
        builder.setCancelable(true)
        builder.setPositiveButton("설정") { dialog, id ->
            val callGPSSettingIntent =
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)
        }
        builder.setNegativeButton(
            "취소"
        ) { dialog, id -> dialog.cancel() }
        builder.create().show()
    }

    fun checkRunTimePermission() { //런타임 퍼미션 처리
// 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) { // 2. 이미 퍼미션을 가지고 있다면
// ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
// 3.  위치 값을 가져올 수 있음
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading)
        } else { //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
// 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    REQUIRED_PERMISSIONS[0]
                )
            ) { // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG)
                    .show()
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            } else { // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
// 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            GPS_ENABLE_REQUEST_CODE ->  //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음")
                        checkRunTimePermission()
                        return
                    }
                }
        }
    }

    fun checkLocationServicesStatus(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {

    }

    override fun onCurrentLocationUpdate(p0: MapView?, mapPoint: MapPoint?, accuracyInMeters: Float) {
        val mapPointGeo: MapPoint.GeoCoordinate  = mapPoint!!.mapPointGeoCoord
        Log.i(LOG_TAG,  String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters))
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {

    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {

    }

    override fun onReverseGeoCoderFailedToFindAddress(p0: MapReverseGeoCoder?) {

    }

    override fun onReverseGeoCoderFoundAddress(p0: MapReverseGeoCoder?, p1: String?) {

    }

}
