package com.example.mintplaces1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.android.synthetic.main.activity_map.*

/*
* 툴바, 네비게이션 드로어 관리.
* 액티비티에 붙어있는 fragment 관리.
* 네트워크 연결 상태를 확인하는 인터페이스 NetworkConnectionCheckAdapter를 구현. fragment에서 관련 기능을 호출해서 사용함.
* */
class MapActivity : AppCompatActivity(), NetworkConnectionCheckAdapter {
    // 네트워크 연결 상태 확인
    override lateinit var networkConnectionChecker: NetworkConnectionChecker
    // 호출했을 시점에 네트워크 연결이 끊겨있으면 메시지 띄움.
    override fun notifyNetworkConnection() {
        if (!networkConnectionChecker.isNetworkConnected())
            Toast.makeText(this, getString(R.string.request_network_connection), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        networkConnectionChecker = NetworkConnectionChecker(baseContext)

        // 툴바 설정
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)
        // 사이드바 설정
        drawable_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        setFragment()
    }

    private fun setFragment() {
        val mapFragment: MapFragment = supportFragmentManager.findFragmentById(R.id.layout_map) as MapFragment?
                ?: MapFragment.newInstance()
        val placeSearchFragment: PlaceSearchFragment = supportFragmentManager.findFragmentById(R.id.layout_place_search) as PlaceSearchFragment?
                ?: PlaceSearchFragment.newInstance()
        supportFragmentManager.beginTransaction()
                .replace(R.id.layout_map, mapFragment)
                .replace(R.id.layout_place_search, placeSearchFragment)
                .commit()
    }

    // 툴바 메뉴 세팅
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_my -> {
                setNavigationMode(NavigationMode.MY)
                drawable_layout.openDrawer(navigation_view)
                return true
            }
            R.id.action_search -> {
                setNavigationMode(NavigationMode.SEARCH)
                drawable_layout.openDrawer(navigation_view)
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }

    private val myNavigationFragment: MyNavigationFragment by lazy { supportFragmentManager.findFragmentByTag(MY_FRAGMENT_TAG) as MyNavigationFragment?
            ?: MyNavigationFragment.newInstance() }
    private val searchNavigationFragment: SearchNavigationFragment by lazy { supportFragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG) as SearchNavigationFragment?
            ?: SearchNavigationFragment.newInstance() }

    // 네비게이션 드로어 안에 보여지는 내용물 설정
    private fun setNavigationMode(navigationMode: NavigationMode) {
        val fragment =
                when (navigationMode) {
                    NavigationMode.MY -> {
                        myNavigationFragment
                    }
                    NavigationMode.SEARCH -> {
                        searchNavigationFragment
                    }
                }
        supportFragmentManager.beginTransaction()
                .replace(R.id.navigation_content, fragment)
                .commit()
    }

    companion object {
        private const val TAG = "MyLogMapAct"

        // 네비게이션 드로어 내용물의 모드
        enum class NavigationMode {
            MY, SEARCH
        }

        const val MY_FRAGMENT_TAG = "myFragmentTag"
        const val SEARCH_FRAGMENT_TAG = "searchFragmentTag"
    }
}