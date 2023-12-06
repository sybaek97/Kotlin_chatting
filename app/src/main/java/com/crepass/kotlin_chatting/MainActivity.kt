package com.crepass.kotlin_chatting

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.crepass.kotlin_chatting.chatlist.ChatListFragment
import com.crepass.kotlin_chatting.databinding.ActivityMainBinding
import com.crepass.kotlin_chatting.mypage.MyPageFragment
import com.crepass.kotlin_chatting.userlist.UserFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private val userFragment=UserFragment()
    private val chatListFragment=ChatListFragment()
    private val myPageFragment=MyPageFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser=Firebase.auth.currentUser
        if(currentUser==null){
            //로그인이 안되어 있을 경우
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }

        askNotificationPermission()

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.userList->{
                    replaceFragment(userFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.chatroomList->{
                    replaceFragment(chatListFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.myList->{
                    replaceFragment(myPageFragment)
                    return@setOnItemSelectedListener true
                }
                else->{
                    return@setOnItemSelectedListener false
                }
            }

        }
        replaceFragment(userFragment)

    }

    private fun replaceFragment(fragment:Fragment){
        supportFragmentManager.beginTransaction()
            .apply{
                replace(R.id.frameLayout,fragment)
                commit()
            }


    }

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.


        } else {
            //권한이 없다면?!

        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionRationalDilog() // 아예 다 거절했을때 교육(?)용 알림
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)//티라미수 버전에서만 사용이 되도록 기능설정, 다른곳에서 사용하면 에러 뜸
    private fun showPermissionRationalDilog(){
        AlertDialog.Builder(this)
            .setMessage("알림 권한이 없으면 알림을 받을 수 없습니다")
            .setPositiveButton("권한 허용 하기"){_,_->
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }.setNegativeButton("취소"){ dialogInterface,_->dialogInterface.cancel()}.show()
    }

}