package com.crepass.kotlin_chatting.userlist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.crepass.kotlin_chatting.Key.Companion.DB_CHAT_ROOMS
import com.crepass.kotlin_chatting.Key.Companion.DB_USERS
import com.crepass.kotlin_chatting.R
import com.crepass.kotlin_chatting.chatdetail.ChatActivity
import com.crepass.kotlin_chatting.chatlist.ChatRoomItem
import com.crepass.kotlin_chatting.databinding.FragmentUserlistBinding
import com.crepass.kotlin_chatting.databinding.ItemUserBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.util.UUID

class UserFragment :Fragment(R.layout.fragment_userlist){

    private lateinit var binding: FragmentUserlistBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentUserlistBinding.bind(view)


        val userListAdapter=UserAdapter{otherUser-> //상대방 유저
            val myUserId=Firebase.auth.currentUser?.uid?:""
            val chatRooDB=Firebase.database.reference.child(DB_CHAT_ROOMS).child(myUserId).child(otherUser.userId?:"")

            chatRooDB.get().addOnSuccessListener {
                var chatRoomId=""
                if(it.value!=null){
                    //데이터가 존재
                    val chatRoom=it.getValue(ChatRoomItem::class.java)

                    chatRoomId=chatRoom?.chatRoomId?:""

                }else{
                    chatRoomId=UUID.randomUUID().toString()//랜덤된 아이디 생성
                    val newChatRoom=ChatRoomItem(
                        chatRoomId=chatRoomId,
                        otherUserName=otherUser.username,
                        otherUserId = otherUser.userId
                    )
                    chatRooDB.setValue(newChatRoom)
                }

                val intent=Intent(context,ChatActivity::class.java)
                intent.putExtra(EXTRA_OTHER_USER_ID,otherUser.userId)
                intent.putExtra(EXTRA_CHAT_ROOM_ID,chatRoomId)

                startActivity(intent)

            }


        }
        binding.userListRecyclerView.apply{
            layoutManager=LinearLayoutManager(context)
                adapter=userListAdapter
        }
        val currentUserId=Firebase.auth.currentUser?.uid ?:""
        Firebase.database.reference.child(DB_USERS)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.e("current",currentUserId)

                    val userItemList= mutableListOf<UserItem>()
                    snapshot.children.forEach{//맵 말고 foreach를 쓰는 이유 : 나도 떠버림
                        val user=it.getValue(UserItem::class.java)
                        Log.e("유저 아이디", user.toString())
                        user?:return
                        if(user.userId!=currentUserId){
                            userItemList.add(user)
                        }

                    }
                    userListAdapter.submitList(userItemList)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })//한번만 받아오면 되기때문에 요걸써줌


    }

    companion object {
        const val EXTRA_CHAT_ROOM_ID="chatRoomId"
        const val EXTRA_OTHER_USER_ID="otherUserId"
    }

}