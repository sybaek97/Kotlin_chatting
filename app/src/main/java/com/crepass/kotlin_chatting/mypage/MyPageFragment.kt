package com.crepass.kotlin_chatting.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.crepass.kotlin_chatting.Key.Companion.DB_USERS
import com.crepass.kotlin_chatting.LoginActivity
import com.crepass.kotlin_chatting.R
import com.crepass.kotlin_chatting.databinding.FragmentMypageBinding
import com.crepass.kotlin_chatting.databinding.ItemChatroomBinding
import com.crepass.kotlin_chatting.userlist.UserItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MyPageFragment:Fragment(R.layout.fragment_mypage) {

    private lateinit var binding: FragmentMypageBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentMypageBinding.bind(view)

        val currentUserId=Firebase.auth.currentUser?.uid?:""
        val currentUserDB=Firebase.database.reference.child(DB_USERS).child(currentUserId)

        currentUserDB.get().addOnSuccessListener{//useritem에 있는 아이템들을 반환
            val currentUser=it.getValue(UserItem::class.java)?:return@addOnSuccessListener

            binding.usernameEditText.setText( currentUser.username)
            binding.descriptionEditText.setText( currentUser.description)





        }


        binding.applyButton.setOnClickListener {
            val username=binding.usernameEditText.text.toString()
            val description=binding.descriptionEditText.text.toString()
            if(username.isEmpty()){
                Toast.makeText(context,"유저 이름은 빈 값으로 두실 수 없습니다",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            val user= mutableMapOf<String,Any>()
            user["username"]=username
            user["description"]=description
            currentUserDB.updateChildren(user)//업데이트 하기

            //todo 파이어베이스 realtime database update

        }
        binding.siginOutButton.setOnClickListener {

            Firebase.auth.signOut()
            startActivity(Intent(context, LoginActivity::class.java))
            activity?.finish()

        }

    }

}