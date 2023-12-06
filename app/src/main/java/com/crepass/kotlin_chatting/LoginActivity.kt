package com.crepass.kotlin_chatting

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.crepass.kotlin_chatting.Key.Companion.DB_USERS
import com.crepass.kotlin_chatting.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.messaging.messaging

class LoginActivity : AppCompatActivity() {


    private lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)


        binding.siginUpButton.setOnClickListener {//회원가입
            val email=binding.emailEditText.text.toString()
            val password=binding.passwordEditText.text.toString()

            if(email.isEmpty()||password.isEmpty()){
                Toast.makeText(this,"이메일 또는 패스워드가 입력되지 않았습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Firebase.auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this) {task->//this는 함수 자체를 받는다
                    if(task.isSuccessful){
                        //회원가입 성공
                        Toast.makeText(this,"회원가입에 성공했습니다. 로그인 해주세요.",Toast.LENGTH_SHORT).show()
                    }else{
                        //회원가입 실패
                        Toast.makeText(this,"회원가입에 실패했습니다.",Toast.LENGTH_SHORT).show()
                    }

                }
        }
        binding.siginInButton.setOnClickListener {//로그인
            val email=binding.emailEditText.text.toString()
            val password=binding.passwordEditText.text.toString()

            if(email.isEmpty()||password.isEmpty()){
                Toast.makeText(this,"이메일 또는 패스워드가 입력되지 않았습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Firebase.auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){task->
                    val currentUser=Firebase.auth.currentUser
                    if(task.isSuccessful&&currentUser!=null){
                        val userId=currentUser.uid

                        Firebase.messaging.token.addOnCompleteListener{
                            val token=it.result

                            val user= mutableMapOf<String,Any>()
                            user["userId"]=userId
                            user["username"]=email
                            user["fcmToken"]=token


                            Firebase.database.reference.child(DB_USERS).child(userId).updateChildren(user)
                            val intent=Intent(this,MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }else{
                        Log.e("LoginActivity",task.exception.toString())
                        Toast.makeText(this,"로그인에 실패했습니다.",Toast.LENGTH_SHORT).show()
                    }
                }

        }



    }


}