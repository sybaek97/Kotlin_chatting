package com.crepass.kotlin_chatting.chatdetail

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crepass.kotlin_chatting.Key.Companion.DB_CHATS
import com.crepass.kotlin_chatting.Key.Companion.DB_CHAT_ROOMS
import com.crepass.kotlin_chatting.Key.Companion.DB_USERS
import com.crepass.kotlin_chatting.R
import com.crepass.kotlin_chatting.databinding.ActivityChatdetailBinding
import com.crepass.kotlin_chatting.userlist.UserItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatdetailBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var linearlayoutManager: LinearLayoutManager

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var otherUserFcmToken: String = ""
    private var myUserId: String = ""
    private var myUserName: String = ""
    private var isInit = false

    //chatRoomID putExtra
    // otherUserId
    private val chatItemList = mutableListOf<ChatItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatdetailBinding.inflate(layoutInflater)

        setContentView(binding.root)


        chatRoomId = intent.getStringExtra("chatRoomId") ?: return
        otherUserId = intent.getStringExtra("otherUserId") ?: return
        myUserId = Firebase.auth.currentUser?.uid ?: ""
        chatAdapter = ChatAdapter()
        linearlayoutManager = LinearLayoutManager(applicationContext)
        Firebase.database.reference.child(DB_USERS).child(myUserId).get()
            .addOnSuccessListener {
                val myUserItem = it.getValue(UserItem::class.java)
                myUserName = myUserItem?.username ?: ""

                getOtherUserData()
            }






        binding.chatRecyclerView.apply {
            layoutManager=linearlayoutManager
//                .apply {
//                reverseLayout=true//데이터를 쌓을때 반대로 쌓아준다.
//            }
            adapter = chatAdapter
        }
        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                linearlayoutManager.smoothScrollToPosition(
                    binding.chatRecyclerView,
                    null,
                    chatAdapter.itemCount
                )

            }
        })

        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()

            if (isInit == false) {
                return@setOnClickListener
            }

            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "메세지를 입력하세요", Toast.LENGTH_SHORT)
                return@setOnClickListener
            }

            val newChatItem = ChatItem(
                message = message,
                userId = myUserId
            )
            Firebase.database.reference.child(DB_CHATS).child(chatRoomId).push().apply {
                newChatItem.chatId = key
                setValue(newChatItem)
            }

            val updates: MutableMap<String, Any> = hashMapOf(
//상대방한테 데이터 넣어주기
                "${DB_CHAT_ROOMS}/$myUserId/$otherUserId/lastMessage" to message,
                "${DB_CHAT_ROOMS}/$otherUserId/$myUserId/lastMessage" to message,
                "${DB_CHAT_ROOMS}/$otherUserId/$myUserId/chatRoomId" to chatRoomId,
                "${DB_CHAT_ROOMS}/$otherUserId/$myUserId/otherUserId" to myUserId,
                "${DB_CHAT_ROOMS}/$otherUserId/$myUserId/otherUserName" to myUserName,

                )
            Firebase.database.reference.updateChildren(updates)

            val client = OkHttpClient()
            val root = JSONObject()
            val notification = JSONObject()
            notification.put("title", getString(R.string.app_name))
            notification.put("body", message)
            root.put("to", otherUserFcmToken)//상대방의 fcm토큰값을 넣어줘야 됨
            root.put("priority", "high")
            root.put("notification", notification)

            val requestBody =
                root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request =
                Request.Builder().post(requestBody).url("https://fcm.googleapis.com/fcm/send")
                    .header("Authorization", "key=${getString(R.string.fcm_server_key)}").build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.stackTraceToString()
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.e("ChatActivity", response.toString())
                }

            })

            binding.messageEditText.text.clear()
        }

    }

    private fun getOtherUserData() {
        Firebase.database.reference.child(DB_USERS).child(otherUserId).get()
            .addOnSuccessListener {
                val otherUserItem = it.getValue(UserItem::class.java)
                otherUserFcmToken = otherUserItem?.fcmToken.orEmpty()
                chatAdapter.otherUserItem = otherUserItem

                isInit = true
                getChatData()

            }

    }

    private fun getChatData() {
        //채팅 가져오기
        Firebase.database.reference.child(DB_CHATS).child(chatRoomId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                    val chatItem = snapshot.getValue(ChatItem::class.java)
                    chatItem ?: return

                    chatItemList.add(chatItem)
                    chatAdapter.submitList(chatItemList.toMutableList())//리스트를 새로 만들어야 ui가 업데이트 됨
//                    binding.chatRecyclerView.scrollToPosition(chatItemList.size-1)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}

            })
    }
}


