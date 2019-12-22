package com.sap.kaisa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_kakao_map.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPOIItem.MarkerType.BluePin
import net.daum.mf.map.api.MapPOIItem.MarkerType.RedPin
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView




class KakaoMapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakao_map)

        try {
            val info = packageManager.getPackageInfo("com.sap.kaisa", PackageManager.GET_SIGNATURES)

            for (sig in info.signatures ){

                val md = MessageDigest.getInstance("SHA")
                md.update(sig.toByteArray())
                Log.e("keyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))

            }
        }catch (e : Exception){
            e.printStackTrace()
        }

        // GPS 체크
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            buildAlertMessgeNoGps()
        }


        // 맵 생성
        val mapView = MapView(this)
        // 더이상 사용되지 않는다고 하지만 아래 코드를 추가하지 않으면 Black Screen 현상 발생
        // '네이티브 앱 키를 잘못 입력하면 White Scree 현상 발생
        mapView.setDaumMapApiKey("f0bad00d73838e98688639990447d0d8")
        //mapView.setDaumMapApiKey("l13s+5ijlDagVoApn4/KvDGZxu4=")

        val mapViewContainer = map_view
        mapViewContainer.addView(mapView)


        //현재위치로 이동
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(33.3, 127.0),true)
        mapView.setZoomLevel(4, true)
        mapView.zoomIn(true)

        //마커 생성
        val marker = MapPOIItem();
        marker.itemName = "현재위치"
        marker.tag = 0
        marker.mapPoint = MapPoint.mapPointWithGeoCoord(33.3,127.0)
        marker.markerType = BluePin
        marker.selectedMarkerType = RedPin
        mapView.addPOIItem(marker)
    }

    private fun buildAlertMessgeNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    11)
            }
            .setNegativeButton("No") {dialog, id ->
                dialog.cancel()
                finish()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

}
