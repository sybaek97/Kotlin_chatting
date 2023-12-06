package com.crepass.kotlin_chatting.chatdetail

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.crepass.kotlin_chatting.databinding.ItemChatBinding
import com.crepass.kotlin_chatting.databinding.ItemChatroomBinding
import com.crepass.kotlin_chatting.userlist.UserItem


class ChatAdapter : ListAdapter<ChatItem, ChatAdapter.ViewHolder>(differ) {

    var otherUserItem: UserItem? = null


    inner class ViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatItem) {
            Log.e("messageE",otherUserItem?.userId.toString())
            if (item.userId == otherUserItem?.userId) {//상대방이 보낸 경우
                binding.usernameText.isVisible=true
                binding.usernameText.text = otherUserItem?.username
                binding.messageTextView.text = item.message
                binding.messageTextView.gravity = Gravity.START
            } else {//내가 보낸 경우
                binding.usernameText.isVisible=false
                binding.messageTextView.text = item.message
                binding.messageTextView.gravity = Gravity.END
            }

            binding.usernameText.text = otherUserItem?.username//todo
            binding.messageTextView.text = item.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<ChatItem>() {
            override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem.chatId == newItem.chatId
            }

            override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }

        }
    }


}