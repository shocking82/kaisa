package com.sap.kaisa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_kakao_map.*
import net.daum.mf.map.api.MapView

class KakaoMapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakao_map)

        val mapView = MapView(this)

        // 더이상 사용되지 않는다고 하지만 아래 코드를 추가하지 않으면 Black Screen 현상 발생
        // '네이티브 앱 키를 잘못 입력하면 White Scree 현상 발생
        mapView.setDaumMapApiKey("b5516e798a256e0590a2ddb2169c2085")

        val mapViewContainer = map_view
        mapViewContainer.addView(mapView)

    }
}
