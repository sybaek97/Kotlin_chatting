package com.crepass.kotlin_chatting.chatlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.crepass.kotlin_chatting.Key.Companion.DB_CHAT_ROOMS
import com.crepass.kotlin_chatting.R
import com.crepass.kotlin_chatting.chatdetail.ChatActivity
import com.crepass.kotlin_chatting.databinding.FragmentChatlistBinding
import com.crepass.kotlin_chatting.userlist.UserFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class ChatListFragment :Fragment(R.layout.fragment_chatlist){

    private lateinit var binding: FragmentChatlistBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentChatlistBinding.bind(view)


        val chatListAdapter=ChatListAdapter{chatRoomItem->


            val intent= Intent(context, ChatActivity::class.java)
            intent.putExtra(UserFragment.EXTRA_OTHER_USER_ID, chatRoomItem.otherUserId)
            intent.putExtra(UserFragment.EXTRA_CHAT_ROOM_ID,  chatRoomItem.chatRoomId)

            startActivity(intent)
        }



        binding.chatListRecyclerView.apply{
            layoutManager=LinearLayoutManager(context)
                adapter=chatListAdapter
        }
        val currentUserId=Firebase.auth.currentUser?.uid?:return
        val chatRoomsDB=Firebase.database.reference.child(DB_CHAT_ROOMS).child(currentUserId)

        chatRoomsDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatRoomList=snapshot.children.map {
                    it.getValue(ChatRoomItem::class.java)
                }
                chatListAdapter.submitList(chatRoomList)

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }


}