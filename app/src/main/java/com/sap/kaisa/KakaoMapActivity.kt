package com.sap.kaisa

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import kotlinx.android.synthetic.main.activity_kakao_map.*
import net.daum.mf.map.api.MapView
import java.lang.Exception
import java.security.MessageDigest

class KakaoMapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakao_map)
//        try {
//            val info = packageManager.getPackageInfo("com.sap.kaisa", PackageManager.GET_SIGNATURES)
//
//            for (sig in info.signatures ){
//
//                val md = MessageDigest.getInstance("SHA")
//                md.update(sig.toByteArray())
//                Log.e("keyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
//
//
//            }
//        }catch (e : Exception){
//            e.printStackTrace()
//        }
//
        val mapView = MapView(this)

        // 더이상 사용되지 않는다고 하지만 아래 코드를 추가하지 않으면 Black Screen 현상 발생
        // '네이티브 앱 키를 잘못 입력하면 White Scree 현상 발생
        mapView.setDaumMapApiKey("85c1ad0eb6b18561d94cf539fd442b68")

        val mapViewContainer = map_view
        mapViewContainer.addView(mapView)

    }
}
